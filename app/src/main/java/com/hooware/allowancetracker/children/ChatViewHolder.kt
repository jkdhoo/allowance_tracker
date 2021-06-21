package com.hooware.allowancetracker.children

import android.widget.TextView
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.databinding.ItChatBinding
import com.hooware.allowancetracker.to.ChatTO
import com.hooware.allowancetracker.utils.toTimestamp

/**
 * View Holder for the Recycler View to bind the data item to the UI
 */
class ChatViewHolder(private val binding: ItChatBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ChatTO) {
        binding.setVariable(BR.item, item)
        binding.name.text = item.name
        binding.name.setTextColor(chooseColorByName(binding.name, item.name))
        binding.timestamp.text = item.time.toLong().toTimestamp
        binding.timestamp.setTextColor(chooseColorByName(binding.timestamp, item.name))
        binding.chat.text = item.message
        binding.chat.setTextColor(chooseColorByName(binding.chat, item.name))
        binding.executePendingBindings()
    }

    private fun chooseColorByName(view: TextView, name: String): Int {
        return when (name) {
            "Laa" -> view.context.resources.getColor(R.color.laa, view.context.resources.newTheme())
            "Levi" -> view.context.resources.getColor(R.color.levi, view.context.resources.newTheme())
            "Mom" -> view.context.resources.getColor(R.color.mom, view.context.resources.newTheme())
            "Dad" -> view.context.resources.getColor(R.color.dad, view.context.resources.newTheme())
            "System" -> view.context.resources.getColor(R.color.black, view.context.resources.newTheme())
            else -> 0
        }
    }
}