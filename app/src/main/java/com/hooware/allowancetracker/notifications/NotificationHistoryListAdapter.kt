package com.hooware.allowancetracker.notifications

import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseRecyclerViewAdapter
import com.hooware.allowancetracker.to.NotificationItemTO
import com.hooware.allowancetracker.to.NotificationSaveItemTO

class NotificationHistoryListAdapter : BaseRecyclerViewAdapter<NotificationItemTO>() {
    override fun getLayoutRes(viewType: Int) = R.layout.it_notification_history
}

