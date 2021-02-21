package com.hooware.allowancetracker.children

import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseRecyclerViewAdapter

//Use data binding to show the reminder on the item
class ChildrenListAdapter(callBack: (selectedChild: ChildDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ChildDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_child
}