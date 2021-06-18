package com.hooware.allowancetracker.notifications

import com.google.firebase.database.DataSnapshot
import com.hooware.allowancetracker.AllowanceApp

object SendNewMessageNotifications {

    fun execute(app: AllowanceApp, snapshot: DataSnapshot, message: String) {
        snapshot.children.forEach { user ->
            if (app.firebaseUID.value != user.key) {
                val body = "${app.authType.value?.simpleName}: $message"
                val title = "New Message!"
                SendFCMNotification.execute(app, user.value.toString(), body = body, title = title)
            }
        }
    }
}