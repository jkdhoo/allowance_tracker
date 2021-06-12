package com.hooware.allowancetracker.utils

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.hooware.allowancetracker.overview.OverviewViewModel
import timber.log.Timber

object HandleSaveFCMToken {
    fun execute(user: FirebaseUser?, token: String, viewModel: ViewModel?) {
        val currentFirebaseUID = user?.uid ?: run {
            Timber.i("Firebase UID null, FCM Token not saved")
            return
        }
        when (viewModel) {
            is OverviewViewModel -> addFCMToken(currentFirebaseUID, token, viewModel)
            else -> {
                Timber.i("viewModel not recognized, FCM Token not saved")
                return
            }
        }
    }

    private fun addFCMToken(uid: String, token: String, viewModel: ViewModel) {
        (viewModel as? OverviewViewModel)?.notificationDatabase?.child(uid)?.setValue(token)
        Timber.i("FCMToken saved")
    }
}