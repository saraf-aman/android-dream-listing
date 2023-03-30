package edu.vt.cs5254.dreamcatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.ListItemDreamBinding
import java.util.*

class DreamHolder(private val binding: ListItemDreamBinding): RecyclerView.ViewHolder(binding.root) {

    lateinit var boundDream: Dream
        private set

    fun bind(dream: Dream, onDreamClicked: (dreamId: UUID) -> Unit) {
        boundDream = dream
        val rCount = dream.entries.count { it.kind == DreamEntryKind.REFLECTION }

        binding.listItemTitle.text = dream.title
        if(binding.listItemTitle.text.isBlank()) {
            binding.listItemTitle.setText(R.string.no_dream_title)
        }
        binding.listItemReflectionCount.text =
            binding.root.context.getString(R.string.reflection_count, rCount)

        binding.root.setOnClickListener {
            onDreamClicked(dream.id)
        }

        if(dream.isFulfilled) {
            binding.listItemImage.setImageResource(R.drawable.dream_fulfilled_icon)
            binding.listItemImage.visibility = View.VISIBLE
        }
        else if(dream.isDeferred) {
            binding.listItemImage.setImageResource(R.drawable.dream_deferred_icon)
            binding.listItemImage.visibility = View.VISIBLE
        }
        else {
            binding.listItemImage.visibility = View.GONE
        }
    }
}

class DreamAdapter(private val dreams: List<Dream>, private val onDreamClicked: (dreamId: UUID) -> Unit) : RecyclerView.Adapter<DreamHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemDreamBinding.inflate(inflater, parent, false)
        return DreamHolder(binding)
    }

    override fun onBindViewHolder(holder: DreamHolder, position: Int) {
        holder.bind(dreams[position], onDreamClicked)
    }

    override fun getItemCount(): Int {
        return dreams.size
    }
}
