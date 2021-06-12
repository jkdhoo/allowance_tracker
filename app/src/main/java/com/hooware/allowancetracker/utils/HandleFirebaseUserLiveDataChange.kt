package com.hooware.allowancetracker.utils

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.hooware.allowancetracker.auth.AuthActivity
import com.hooware.allowancetracker.overview.OverviewViewModel
import timber.log.Timber

object HandleFirebaseUserLiveDataChange {
    fun execute(user: FirebaseUser?, activity: Activity?, viewModel: ViewModel?) {
        if (activity == null || viewModel == null) run {
            Timber.i("Activity or viewModel null, firebase user not updated")
            return
        }
        if (user == null) {
            Timber.i("Not authenticated. Authenticating...")
            val intent = Intent(activity, AuthActivity::class.java)
            when (viewModel) {
                is OverviewViewModel -> viewModel.reset()
                else -> {
                    Timber.i("viewModel not recognized, firebase user not updated")
                    return
                }
            }
            activity.startActivity(intent)
            activity.finish()
        } else {
            Timber.i("Authenticated - ${user.uid}")
            when (viewModel) {
                is OverviewViewModel -> viewModel.setFirebaseUID(user.uid)
            }
        }
    }
}