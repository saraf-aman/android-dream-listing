package edu.vt.cs5254.dreamcatcher

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import edu.vt.cs5254.dreamcatcher.databinding.FragmentPhotoDialogBinding
import java.io.File

class PhotoDialogFragment : DialogFragment(){

    private val args: PhotoDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = FragmentPhotoDialogBinding.inflate(layoutInflater)

        with(binding.photoDetail) {
            with(binding.root) {
                if (tag != args.dreamPhotoFilename) {
                    val photoFile =
                        File(requireContext().applicationContext.filesDir, args.dreamPhotoFilename)
                    if (photoFile.exists()) {
                        doOnLayout() { measuredView ->
                            val scaledBM = getScaledBitmap(
                                photoFile.path,
                                measuredView.width,
                                measuredView.height
                            )
                            setImageBitmap(scaledBM)
                            tag = args.dreamPhotoFilename
                        }
                    } else {
                        setImageBitmap(null)
                        tag = null
                    }
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .show()
    }
}