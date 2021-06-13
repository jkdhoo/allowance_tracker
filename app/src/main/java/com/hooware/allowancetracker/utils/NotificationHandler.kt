package com.hooware.allowancetracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hooware.allowancetracker.BuildConfig
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.overview.OverviewActivity
import org.json.JSONObject
import timber.log.Timber

class NotificationHandler : FirebaseMessagingService() {

    private companion object {
        private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.i("${remoteMessage.data}")
        val data = try {
            JSONObject(remoteMessage.data.toString())
        } catch (ex: Exception) {
            Timber.i("Message data not JSON")
            return
        }
        val body = data.getString("body") ?: return
        val title = data.getString("title") ?: return
        sendNotification(body = body, title = title)
    }

    private fun sendNotification(body: String, title: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            val name = getString(R.string.app_name)
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = OverviewActivity.newIntent(applicationContext)

        //create a pending intent that opens ReminderDescriptionActivity when the user clicks on the notification
        val stackBuilder = TaskStackBuilder.create(this)
            .addParentStack(OverviewActivity::class.java)
            .addNextIntent(intent)
        val notificationPendingIntent = stackBuilder
            .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)

//    build the notification object with the data to be shown
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dollar_sign)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(getUniqueId(), notification)
    }

    private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())
}