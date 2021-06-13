package com.hooware.allowancetracker.auth

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.databinding.ActivityAuthBinding
import com.hooware.allowancetracker.utils.HandleFirebaseUserLiveData
import timber.log.Timber

/**
 * Authentication activity
 */
class AuthActivity : AppCompatActivity() {

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAuthBinding = DataBindingUtil.setContentView(this, R.layout.activity_auth)
        binding.lifecycleOwner = this
        binding.authButton.setOnClickListener { launchSignInFlow() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startForResult.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.login_logo_dollar_sign)
                .setAvailableProviders(providers)
                .setTheme(R.style.Theme_AllowanceTracker_Auth)
                .build()
        )
    }

    override fun onResume() {
        super.onResume()
        FirebaseUserLiveData().observe(this, { user ->
            if (user != null) {
                HandleFirebaseUserLiveData.execute(this, user)
            } else {
                Timber.i("Unauthenticated")
            }
        })
    }

    override fun onPause() {
        FirebaseUserLiveData().removeObservers(this)
        super.onPause()
    }
}
