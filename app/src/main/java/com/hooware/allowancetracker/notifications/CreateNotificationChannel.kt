package com.hooware.allowancetracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R

object CreateNotificationChannel {

    fun execute(application: AllowanceApp) {

        val notificationManager: NotificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        notificationManager.deleteNotificationChannel(NotificationHandler.NOTIFICATION_CHANNEL_ID)
        if (notificationManager.getNotificationChannel(NotificationHandler.NOTIFICATION_CHANNEL_ID) == null) {
            val name = application.getString(R.string.channel_name)
            val descriptionText = application.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NotificationHandler.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            channel.enableLights(true)
            channel.lightColor = Color.GREEN
            notificationManager.createNotificationChannel(channel)
        }
    }
}