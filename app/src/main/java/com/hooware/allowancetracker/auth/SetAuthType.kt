package com.hooware.allowancetracker.auth

import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.utils.AuthType
import com.hooware.allowancetracker.utils.FirebaseConfigRetriever
import timber.log.Timber

object SetAuthType {

    fun execute(userId: String, application: AllowanceApp) {
        Timber.i("Authenticated - $userId, setting AuthType")
        val mom = FirebaseConfigRetriever.execute("mom_uid")
        val dad = FirebaseConfigRetriever.execute("dad_uid")
        val levi = FirebaseConfigRetriever.execute("levi_uid")
        val laa = FirebaseConfigRetriever.execute("laa_uid")
        when {
            dad.contains(userId) -> application.authType.value = AuthType.DAD
            mom.contains(userId) -> application.authType.value = AuthType.MOM
            levi.contains(userId) -> application.authType.value = AuthType.LEVI
            laa.contains(userId) -> application.authType.value = AuthType.LAA
            else -> application.authType.value = AuthType.UNKNOWN
        }
    }
}