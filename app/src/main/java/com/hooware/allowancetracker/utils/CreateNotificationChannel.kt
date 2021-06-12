package com.hooware.allowancetracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R

object CreateNotificationChannel {

    fun execute(application: AllowanceApp) {
        val name = application.getString(R.string.channel_name)
        val descriptionText = application.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(application.getString(R.string.channel_id), name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}