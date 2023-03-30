package edu.vt.cs5254.dreamcatcher

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import edu.vt.cs5254.dreamcatcher.databinding.FragmentReflectionDialogBinding
import java.util.*

class ReflectionDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val currentDat: Date = Date()

        val formattedDate =
            DateFormat.format("yyyy-MM-dd 'at' hh:mm:ss a", currentDat)

        val binding = FragmentReflectionDialogBinding.inflate(layoutInflater)
        val positiveListener = DialogInterface.OnClickListener { _, _ ->
            val resultText = binding.reflectionText.text.toString() + " -> " + formattedDate
            if(resultText.isNotBlank()) {
                setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(BUNDLE_KEY to resultText)
                )
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.reflection_dialog_title)
            .setPositiveButton(R.string.reflection_dialog_positive, positiveListener)
            .setNegativeButton(R.string.reflection_dialog_negative, null)
            .show()
    }

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY_REFLECTION"
        const val BUNDLE_KEY = "BUNDLE_KEY_REFLECTION"
    }
}