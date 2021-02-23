package com.hooware.allowancetracker.transactions

import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseRecyclerViewAdapter

//Use data binding to show the reminder on the item
class TransactionsListAdapter(callBack: (selectedTransaction: TransactionDataItem) -> Unit) :
    BaseRecyclerViewAdapter<TransactionDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_transaction
}