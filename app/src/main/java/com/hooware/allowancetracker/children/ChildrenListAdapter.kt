package com.hooware.allowancetracker.children

import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseRecyclerViewAdapter
import com.hooware.allowancetracker.to.ChildTO

//Use data binding to show the reminder on the item
class ChildrenListAdapter(callBack: (selectedChild: ChildTO) -> Unit) :
    BaseRecyclerViewAdapter<ChildTO>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_child
}