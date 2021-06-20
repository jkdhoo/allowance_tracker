package com.hooware.allowancetracker.splash

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.base.BaseViewModel
import timber.log.Timber

class SplashViewModel(application: AllowanceApp) : BaseViewModel(application) {

    val app = application

    private companion object {
        private const val SPLASH_TIME_IN_MILLI = 1000L
    }

    private var _splashReady = MutableLiveData<Boolean>()
    val splashReady: LiveData<Boolean>
        get() = _splashReady

    private val splashTimerComplete = MutableLiveData<Boolean>()

    init {
        _splashReady.value = false
        splashTimerComplete.value = false
        Timber.i("Initialized")
    }

    fun initialize() {
        startSplashTimer()
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
}
