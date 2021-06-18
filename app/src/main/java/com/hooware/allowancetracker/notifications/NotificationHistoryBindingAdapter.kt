package com.hooware.allowancetracker.notifications

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.hooware.allowancetracker.base.BaseRecyclerViewAdapter
import com.hooware.allowancetracker.to.TransactionTO
import com.hooware.allowancetracker.utils.fadeIn
import com.hooware.allowancetracker.utils.fadeOut

object NotificationHistoryBindingAdapter {

    /**
     * Use binding adapter to set the recycler view data using livedata object
     */
    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveDataNotificationHistory")
    @JvmStatic
    fun <T> setRecyclerViewData(recyclerView: RecyclerView, transactions: LiveData<MutableList<T>>) {
        transactions.value?.let { transactionsList ->
            (recyclerView.adapter as? BaseRecyclerViewAdapter<T>)?.apply {
                clear()
                addData(transactionsList)
            }
        }
    }
}