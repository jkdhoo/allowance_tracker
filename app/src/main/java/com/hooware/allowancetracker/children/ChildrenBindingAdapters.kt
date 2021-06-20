package com.hooware.allowancetracker.children

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.hooware.allowancetracker.base.BaseRecyclerViewAdapter
import java.text.DecimalFormat

object ChildrenBindingAdapters {

    /**
     * Use binding adapter to set the recycler view data using livedata object
     */
    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveDataKids")
    @JvmStatic
    fun <T> setRecyclerViewData(recyclerView: RecyclerView, children: LiveData<MutableList<T>>?) {
        children?.value?.let { childrenList ->
            (recyclerView.adapter as? BaseRecyclerViewAdapter<T>)?.apply {
                clear()
                addData(childrenList)
            }
        }
    }

    @BindingAdapter("android:liveDataChat")
    @JvmStatic
    fun setRecyclerViewChatData(recyclerView: RecyclerView, chat: LiveData<MutableList<Triple<String, String, String>>>?) {
        chat?.value?.let { chatList ->
            (recyclerView.adapter as? ChatListAdapter)?.apply {
                clear()
                addData(chatList)
            }
        }
    }

    @BindingAdapter("android:currency")
    @JvmStatic
    fun setCurrency(view: TextView, newValue: Double?) {
        if (newValue != null) {
            val format = DecimalFormat("#,##0.00")
            "$${format.format(newValue)}".also { view.text = it }
        }
    }
}