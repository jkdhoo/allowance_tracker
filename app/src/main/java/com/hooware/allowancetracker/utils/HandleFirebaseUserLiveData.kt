package com.hooware.allowancetracker.utils

import android.app.Activity
import android.content.Intent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.auth.AuthActivity
import com.hooware.allowancetracker.overview.OverviewActivity
import timber.log.Timber

object HandleFirebaseUserLiveData {

    fun execute(activity: Activity?, user: FirebaseUser?) {
        if (activity == null) run {
            Timber.i("Activity null, firebase user not updated")
            return
        }
        if (user == null) {
            Timber.i("Not authenticated. Authenticating...")
            val intent = Intent(activity, AuthActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        } else {
            Timber.i("Authenticated - ${user.uid}")
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.i("Fetching FCM registration token failed: ${task.exception}")
                    return@OnCompleteListener
                }
                val app = activity.application as AllowanceApp
                app.fcmToken.value = task.result
                HandleSaveFCMToken.execute(user, task.result)
            })
            when (activity) {
                is AuthActivity -> {
                    Timber.i("Authenticated, sending to Overview: ${user.displayName}")
                    val overviewActivityIntent = Intent(activity, OverviewActivity::class.java)
                    activity.startActivity(overviewActivityIntent)
                    activity.finishAffinity()
                }
            }
        }
    }
}