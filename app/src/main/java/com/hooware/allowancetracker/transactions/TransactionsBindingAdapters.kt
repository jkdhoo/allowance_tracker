package com.hooware.allowancetracker.transactions

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.hooware.allowancetracker.to.TransactionTO
import com.hooware.allowancetracker.utils.fadeIn
import com.hooware.allowancetracker.utils.fadeOut

object TransactionsBindingAdapters {

    /**
     * Use binding adapter to set the recycler view data using livedata object
     */
    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveDataTransactions")
    @JvmStatic
    fun <T> setRecyclerViewData(recyclerView: RecyclerView, transactions: LiveData<List<TransactionTO>>?) {
        transactions?.value?.let { transactionsList ->
            (recyclerView.adapter as? TransactionsListAdapter<T>)?.apply {
                clear()
                addData(transactionsList)
            }
        }
    }

    /**
     * Use this binding adapter to show and hide the views using boolean variables
     */
    @BindingAdapter("android:fadeVisible")
    @JvmStatic
    fun setFadeVisible(view: View, visible: Boolean? = true) {
        if (view.tag == null) {
            view.tag = true
            view.visibility = if (visible == true) View.VISIBLE else View.GONE
        } else {
            view.animate().cancel()
            if (visible == true) {
                if (view.visibility == View.GONE)
                    view.fadeIn()
            } else {
                if (view.visibility == View.VISIBLE)
                    view.fadeOut()
            }
        }
    }
}