package edu.vt.cs5254.dreamcatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.ListItemDreamEntryBinding

class DreamEntryHolder(private val binding: ListItemDreamEntryBinding): RecyclerView.ViewHolder(binding.root) {
    lateinit var boundEntry: DreamEntry
        private set

    fun bind(entry: DreamEntry) {
        boundEntry = entry

        when (entry.kind) {
            DreamEntryKind.CONCEIVED -> {
                binding.dreamEntryButton.visibility = View.VISIBLE
                binding.dreamEntryButton.text = entry.kind.toString()
                binding.dreamEntryButton.setBackgroundWithContrastingText("#edca82")
            }
            DreamEntryKind.FULFILLED -> {
                binding.dreamEntryButton.visibility = View.VISIBLE
                binding.dreamEntryButton.text = entry.kind.toString()
                binding.dreamEntryButton.setBackgroundWithContrastingText("#097770")
            }
            DreamEntryKind.DEFERRED -> {
                binding.dreamEntryButton.visibility = View.VISIBLE
                binding.dreamEntryButton.text = entry.kind.toString()
                binding.dreamEntryButton.setBackgroundWithContrastingText("#e0cdbe")
            }
            DreamEntryKind.REFLECTION -> {
                binding.dreamEntryButton.visibility = View.VISIBLE
                binding.dreamEntryButton.setBackgroundWithContrastingText("#a9c0a6")
                binding.dreamEntryButton.isAllCaps = false
                binding.dreamEntryButton.text = entry.text
            }
        }
    }
}

class DreamEntryAdapter (private val dreamEntries: List<DreamEntry>):
    RecyclerView.Adapter<DreamEntryHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : DreamEntryHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemDreamEntryBinding.inflate(inflater, parent, false)
        return DreamEntryHolder(binding)
    }

    override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
        holder.bind(dreamEntries[position])
    }

    override fun getItemCount(): Int = dreamEntries.size
}