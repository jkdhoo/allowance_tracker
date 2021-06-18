package com.hooware.allowancetracker.notifications

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.to.NotificationSaveItemTO

object SaveNotificationIfNecessary {

    private val notificationDatabase = Firebase.database.reference.child("notifications").ref
    private val notificationHistoryDatabase = Firebase.database.reference.child("notificationHistory").ref
    private const val NEW_MESSAGE = "New Message!"

    fun execute(application: AllowanceApp, to: String, body: String, title: String) {
        if (title == NEW_MESSAGE) return
        notificationDatabase.get().addOnSuccessListener { db ->
            val toUID = db.children.firstOrNull { item ->
                item.value == to
            }
            notificationHistoryDatabase.child(System.currentTimeMillis().toString()).setValue(
                NotificationSaveItemTO(
                    to = toUID?.key.toString(),
                    from = application.firebaseUID.value,
                    body = body,
                    title = title,
                )
            )
        }
    }
}