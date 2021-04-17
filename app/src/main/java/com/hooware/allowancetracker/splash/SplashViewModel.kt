package com.hooware.allowancetracker.splash

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.AuthType
import com.hooware.allowancetracker.base.BaseViewModel
import timber.log.Timber

class SplashViewModel(application: AllowanceApp) : BaseViewModel(application) {

    private companion object {
        private const val SPLASH_TIME_IN_MILLI = 2000L
    }

    private var _splashReady = MutableLiveData<Boolean>()
    val splashReady: LiveData<Boolean>
        get() = _splashReady

    private val splashTimerComplete = MutableLiveData<Boolean>()

    private var _authType = MutableLiveData<AuthType>()
    val authType: LiveData<AuthType>
        get() = _authType

    init {
        _splashReady.value = false
        splashTimerComplete.value = false
    }

    fun initialize() {
        startSplashTimer()
    }

    fun setAuthType(user: FirebaseUser) {
        val parent = firebaseConfigRetriever("parent_uid")
        val levi = firebaseConfigRetriever("levi_uid")
        val laa = firebaseConfigRetriever("laa_uid")
        when {
            parent.contains(user.uid) -> _authType.value = AuthType.PARENT
            levi.contains(user.uid) -> _authType.value = AuthType.LEVI
            laa.contains(user.uid) -> _authType.value = AuthType.LAA
        }
        Timber.i("Auth Type: ${authType.value}")
    }

    private fun startSplashTimer() {
        val splashTime = SPLASH_TIME_IN_MILLI
        val handler = Handler(Looper.getMainLooper())
        if (splashTimerComplete.value == false) {
            handler.postDelayed({
                splashTimerComplete.value = true
                verifySplashComplete()
            }, splashTime)
        }
    }

    private fun verifySplashComplete() {
        if (splashTimerComplete.value == true) {
            _splashReady.value = true
        }
    }

    private fun firebaseConfigRetriever(param: String): String {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        return remoteConfig.getString(param)
    }
}
