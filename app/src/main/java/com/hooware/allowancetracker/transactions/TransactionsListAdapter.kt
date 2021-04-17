package com.hooware.allowancetracker.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.to.TransactionTO

class TransactionsListAdapter<T>(private val callback: (selectedTransaction: TransactionTO) -> Unit) :
    RecyclerView.Adapter<TransactionsViewHolder<TransactionTO>>() {

    private var _items: MutableList<TransactionTO> = mutableListOf()

    /**
     * Returns the _items data
     */
    private val items: List<TransactionTO>
        get() = this._items

    override fun getItemCount() = _items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder<TransactionTO> {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding = DataBindingUtil
            .inflate<ViewDataBinding>(layoutInflater, getLayoutRes(viewType), parent, false)

        binding.lifecycleOwner = getLifecycleOwner()

        return TransactionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder<TransactionTO>, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            callback.invoke(item)
        }
    }

    private fun getItem(position: Int) = _items[position]

    /**
     * Adds data to the actual Dataset
     *
     * @param items to be merged
     */
    fun addData(items: List<TransactionTO>) {
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

    @LayoutRes
    fun getLayoutRes(viewType: Int) = R.layout.it_transaction

    private fun getLifecycleOwner(): LifecycleOwner? {
        return null
    }
}

