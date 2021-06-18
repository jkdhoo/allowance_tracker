package com.hooware.allowancetracker.notifications

import com.hooware.allowancetracker.AllowanceApp

object IsUserOnOverview {

    fun execute(application: AllowanceApp): Boolean {
        return application.isOverviewShowing.value ?: false
    }
}