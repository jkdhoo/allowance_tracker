package com.hooware.allowancetracker.utils

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

object FirebaseConfigRetriever {

    fun execute(param: String): String {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        return remoteConfig.getString(param)
    }
}