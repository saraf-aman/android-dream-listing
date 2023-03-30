package edu.vt.cs5254.dreamcatcher

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamListBinding
import kotlinx.coroutines.launch

class DreamListFragment: Fragment() {

    private var _binding: FragmentDreamListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "FragmentDreamListBinding is null"
        }
    private val listVM: DreamListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDreamListBinding.inflate(inflater, container, false)

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.fragment_dream_list, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.new_dream -> {
                            showNewDream()
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner
        )

        binding.noDreamAddButton.setOnClickListener {
            showNewDream()
        }

        getItemTouchHelper().attachToRecyclerView(binding.dreamRecyclerView)

        binding.dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                listVM.dreams.collect { dreams ->
                    if(dreams.isEmpty()) {
                        binding.noDreamAddButton.visibility = View.VISIBLE
                        binding.noDreamText.visibility = View.VISIBLE
                    } else {
                        binding.noDreamAddButton.visibility = View.GONE
                        binding.noDreamText.visibility = View.GONE
                    }
                    binding.dreamRecyclerView.adapter = DreamAdapter(dreams) { dreamId ->
                        findNavController().navigate(DreamListFragmentDirections.showDreamDetail(dreamId))
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showNewDream()
    {
        viewLifecycleOwner.lifecycleScope.launch {
            val newDream = Dream()
            listVM.addDream(newDream)
            findNavController().navigate(DreamListFragmentDirections.showDreamDetail(newDream.id))
        }
    }

    private fun getItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val dreamHolder = viewHolder as DreamHolder
                    val dream = dreamHolder.boundDream
                    listVM.deleteDream(dream)
                }
            }
        })
    }

}