package com.hooware.allowancetracker.notifications

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hooware.allowancetracker.AllowanceApp
import timber.log.Timber

object HandleSaveFCMToken {
    private val notificationDatabase = Firebase.database.reference.child("notifications").ref
    fun execute(application: AllowanceApp, user: FirebaseUser?, token: String) {
        val currentFirebaseUID = user?.uid ?: run {
            Timber.i("Firebase UID null, FCM Token not saved")
            return
        }
        when (notificationDatabase.child(currentFirebaseUID).key) {
            null -> {
                notificationDatabase.setValue(currentFirebaseUID).addOnSuccessListener {
                    saveToken(application, currentFirebaseUID, token)
                }
            }
            else -> saveToken(application, currentFirebaseUID, token)
        }
    }

    private fun saveToken(application: AllowanceApp, currentFirebaseUID: String, token: String) {
        application.fcmToken.value = token
        notificationDatabase.child(currentFirebaseUID).setValue(token).addOnSuccessListener {
            Timber.i("FCM Token Saved")
        }
    }
}