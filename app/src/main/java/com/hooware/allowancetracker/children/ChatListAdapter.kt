package com.hooware.allowancetracker.children

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.databinding.ItChatBinding

class ChatListAdapter : RecyclerView.Adapter<ChatViewHolder>() {

    @LayoutRes
    fun getLayoutRes(viewType: Int) = R.layout.it_chat

    private var _items: MutableList<Triple<String, String, String>> = mutableListOf()

    /**
     * Returns the _items data
     */
    private val items: MutableList<Triple<String, String, String>>
        get() = this._items

    override fun getItemCount() = _items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding = DataBindingUtil
            .inflate<ViewDataBinding>(layoutInflater, getLayoutRes(viewType), parent, false)

        binding.lifecycleOwner = getLifecycleOwner()
        return ChatViewHolder(binding as ItChatBinding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    private fun getItem(position: Int) = _items[position]

    /**
     * Adds data to the actual Dataset
     *
     * @param items to be merged
     */
    fun addData(items: MutableList<Triple<String, String, String>>) {
        _items.addAll(items)
        notifyDataSetChanged()
    }

    /**
     * Clears the _items data
     */
    fun clear() {
        _items.clear()
        notifyDataSetChanged()
    }

    private fun getLifecycleOwner(): LifecycleOwner? {
        return null
    }
}