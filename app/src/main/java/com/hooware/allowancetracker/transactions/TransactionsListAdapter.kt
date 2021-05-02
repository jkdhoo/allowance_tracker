package com.hooware.allowancetracker.transactions

import android.view.View
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseRecyclerViewAdapter
import com.hooware.allowancetracker.to.TransactionTO

class TransactionsListAdapter(callBack: (selectedTransaction: TransactionTO, view: View) -> Unit) : BaseRecyclerViewAdapter<TransactionTO>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_transaction
}

