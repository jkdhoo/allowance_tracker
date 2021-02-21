package com.hooware.allowancetracker.auth

import android.app.Application
import androidx.lifecycle.map
import com.hooware.allowancetracker.base.BaseViewModel

class AuthViewModel(app: Application) : BaseViewModel(app) {

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}
