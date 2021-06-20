package com.hooware.allowancetracker.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.BuildConfig
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.FirebaseUserLiveData
import com.hooware.allowancetracker.overview.OverviewActivity
import org.json.JSONObject
import timber.log.Timber

class NotificationHandler : FirebaseMessagingService() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"
        private const val NEW_MESSAGE = "New Message!"
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
        if (title == NEW_MESSAGE) {
            val isUserOnOverview = IsUserOnOverview.execute(application as AllowanceApp)
            if (isUserOnOverview) return
        }
        showNotification(body = body, title = title)
    }

    private fun showNotification(body: String, title: String) {
        Timber.i("Showing notification")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val intent = OverviewActivity.newIntent(applicationContext)

        val stackBuilder = TaskStackBuilder.create(this)
            .addParentStack(OverviewActivity::class.java)
            .addNextIntent(intent)
        val notificationPendingIntent = stackBuilder
            .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)

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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        HandleSaveFCMToken.execute(application as AllowanceApp, FirebaseUserLiveData().value, token)
    }
}