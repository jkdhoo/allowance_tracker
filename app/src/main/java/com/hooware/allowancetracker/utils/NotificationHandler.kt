package com.hooware.allowancetracker.utils

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber
import java.lang.Exception

class NotificationHandler : FirebaseMessagingService() {
//    override fun getStartCommandIntent(p0: Intent?): Intent {
//        return super.getStartCommandIntent(p0)
//    }
//
//    override fun handleIntent(intent: Intent?) {
//        super.handleIntent(intent)
//    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.i("${remoteMessage.data}")
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    override fun onMessageSent(p0: String) {
        super.onMessageSent(p0)
    }

    override fun onSendError(p0: String, p1: Exception) {
        super.onSendError(p0, p1)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}