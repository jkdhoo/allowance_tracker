package com.hooware.allowancetracker.utils

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber

object HandleSaveFCMToken {
    private val notificationDatabase = Firebase.database.reference.child("notifications").ref
    fun execute(user: FirebaseUser?, token: String) {
        val currentFirebaseUID = user?.uid ?: run {
            Timber.i("Firebase UID null, FCM Token not saved")
            return
        }
        when (notificationDatabase.child(currentFirebaseUID).key) {
            null -> {
                notificationDatabase.setValue(currentFirebaseUID).addOnSuccessListener {
                    saveToken(currentFirebaseUID, token)
                }
            }
            else -> saveToken(currentFirebaseUID, token)
        }
    }

    private fun saveToken(currentFirebaseUID: String, token: String) {
        notificationDatabase.child(currentFirebaseUID).setValue(token).addOnSuccessListener {
            Timber.i("FCM Token Saved")
        }
    }
}