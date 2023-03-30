package edu.vt.cs5254.dreamcatcher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import kotlinx.coroutines.launch
import java.io.File

class DreamDetailFragment : Fragment() {

    private val args: DreamDetailFragmentArgs by navArgs()
    private var _binding: FragmentDreamDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "FragmentDreamDetailBinding is null"
        }
    private val detailVM: DreamDetailViewModel by viewModels() {
        DreamDetailViewModelFactory(args.dreamId)
    }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) {
        binding.dreamPhoto.tag = null
        detailVM.dream.value?.let { updatePhoto(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDreamDetailBinding.inflate(inflater, container, false)

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.fragment_dream_detail, menu)

                    val captureImageIntent = takePhoto.contract.createIntent(
                        requireContext(),
                        Uri.EMPTY
                    )
                    menu.findItem(R.id.take_photo_menu).isVisible = canResolveIntent(captureImageIntent)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.share_dream_menu -> {
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, detailVM.dream.value?.let { getDreamDetail(it) })
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    getString(R.string.share_subject)
                                )
                            }.also { intent ->
                                val chooserIntent = Intent.createChooser(
                                    intent,
                                    getString(R.string.share_dream_detail)
                                )
                                startActivity(chooserIntent)
                            }
                            true
                        }
                        R.id.take_photo_menu -> {
                            detailVM.dream.value?.let {
                                val photoFile = File(
                                    requireContext().applicationContext.filesDir,
                                    it.photoFileName
                                )
                                val photoUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    "edu.vt.cs5254.dreamcatcher.fileprovider",
                                    photoFile
                                )
                                takePhoto.launch(photoUri)
                            }
                            true
                        }
                        R.id.delete_dream_menu -> {
                            viewLifecycleOwner.lifecycleScope.launch {
                                detailVM.dream.collect() { dream ->
                                    dream?.let {detailVM.deleteDream(dream)}
                                    findNavController().navigate(
                                        DreamDetailFragmentDirections.deleteDreamDetailPage()
                                    )
                                }
                            }
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner
        )

        binding.dreamEntryRecycler.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                detailVM.dream.collect() { dream ->
                    dream?.let { updateView(dream) }
                }
            }
        }

        binding.deferredCheckbox.setOnClickListener {
            detailVM.updateDream { oldDream ->
                if(!binding.deferredCheckbox.isChecked) {
                    oldDream.copy().apply {
                        entries = oldDream.entries.filter { DreamEntryKind.DEFERRED != it.kind }
                    }
                } else {
                    oldDream.copy().apply {
                        entries = oldDream.entries + DreamEntry(
                            kind = DreamEntryKind.DEFERRED,
                            dreamId = oldDream.id
                        )
                    }
                }
            }
        }

        binding.fulfilledCheckbox.setOnClickListener {
            detailVM.updateDream { oldDream ->
                if(oldDream.isFulfilled) {
                    oldDream.copy().apply {
                        entries = oldDream.entries.filter { DreamEntryKind.FULFILLED != it.kind }
                    }
                } else {
                    oldDream.copy().apply {
                        entries = oldDream.entries + DreamEntry(
                            kind = DreamEntryKind.FULFILLED,
                            dreamId = oldDream.id
                        )
                    }
                }
            }
        }

        binding.titleText.doOnTextChanged { text, _, _, _ ->
            detailVM.updateDream { oldDream ->
                oldDream.copy(title = text.toString())
                    .apply { entries = oldDream.entries }
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                detailVM.dream.collect { dream ->
                    dream?.let { updateView(it) }
                }
            }
        }

        setFragmentResultListener(
            ReflectionDialogFragment.REQUEST_KEY) { requestKey, bundle ->
                val entryText = bundle.getString(ReflectionDialogFragment.BUNDLE_KEY) ?: ""
            (detailVM.updateDream { oldDream ->
                    oldDream.copy().apply {
                        entries = oldDream.entries + DreamEntry(
                            kind = DreamEntryKind.REFLECTION,
                            text = entryText,
                            dreamId = oldDream.id
                        )
                    }
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateView(dream: Dream) {

        binding.dreamEntryRecycler.adapter = DreamEntryAdapter(dream.entries)

        val formattedDate =
            DateFormat.format("yyyy-MM-dd 'at' hh:mm:ss a", dream.lastUpdated)
        binding.lastUpdatedText.text = getString(R.string.last_updated, formattedDate)

        if (dream.title != binding.titleText.text.toString()) {
            binding.titleText.setText(dream.title)
        }

        if(!binding.fulfilledCheckbox.isChecked) {
            binding.addReflectionButton.show()
        }
        else {
            binding.addReflectionButton.hide()
        }

        binding.addReflectionButton.setOnClickListener {
            findNavController().navigate(
                DreamDetailFragmentDirections.addReflection()
            )
        }

        binding.dreamPhoto.setOnClickListener {
            findNavController().navigate(
                DreamDetailFragmentDirections.showPhotoDetail(dream.photoFileName)
            )
        }

        getItemTouchHelper().attachToRecyclerView(binding.dreamEntryRecycler)

        binding.deferredCheckbox.isChecked = dream.isDeferred
        binding.fulfilledCheckbox.isChecked = dream.isFulfilled
        binding.deferredCheckbox.isEnabled = !dream.isFulfilled
        binding.fulfilledCheckbox.isEnabled = !dream.isDeferred

        updatePhoto(dream)
    }

    private fun getDreamDetail(dream: Dream): String {
        val dreamTitle = getString(R.string.share_title, dream.title)
        val formattedDate =
            DateFormat.format("yyyy-MM-dd 'at' hh:mm:ss a", dream.lastUpdated)
        var reflectionStringList = ""
        dream.entries.forEach { dreamEntry ->
            if(dreamEntry.kind == DreamEntryKind.REFLECTION) {
                reflectionStringList += "\n * " + dreamEntry.text
            }
        }
        val reflections = if(reflectionStringList != "") getString(
            R.string.share_reflection, reflectionStringList
        ) else ""

        val dreamStatus = when {
            dream.isFulfilled -> getString(R.string.share_fulfilled)
            dream.isDeferred -> getString(R.string.share_deferred)
            else -> ""
        }

        return  getString(R.string.share_dream_detail, dreamTitle, formattedDate, reflections, dreamStatus)
    }

    private fun updatePhoto(dream: Dream) {
        with(binding.dreamPhoto) {
            if (tag != dream.photoFileName) {
                val photoFile =
                    File(requireContext().applicationContext.filesDir, dream.photoFileName)
                if (photoFile.exists()) {
                    doOnLayout { measuredView ->
                        val scaledBM = getScaledBitmap(
                            photoFile.path,
                            measuredView.width,
                            measuredView.height
                        )
                        setImageBitmap(scaledBM)
                        tag = dream.photoFileName
                    }
                    this.isEnabled = true;
                } else {
                    setImageBitmap(null)
                    tag = null
                    this.isEnabled = false;
                }
            }
        }
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return  resolvedActivity != null
    }

    private fun getItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dreamEntryHolder = viewHolder as DreamEntryHolder
                val dream = dreamEntryHolder.boundEntry
                if(dream.kind == DreamEntryKind.REFLECTION) {
                    return ItemTouchHelper.LEFT
                }
                return super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val dreamEntryHolder = viewHolder as DreamEntryHolder
                    val dream = dreamEntryHolder.boundEntry
                    detailVM.updateDream { oldDream ->
                        oldDream.copy().apply {
                            entries = oldDream.entries.filter { dream.id != it.id }
                        }
                    }
                }
            }
        })
    }
}