package com.hooware.allowancetracker.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.databinding.ActivityAuthBinding
import com.hooware.allowancetracker.overview.OverviewActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * Authentication activity
 */
class AuthActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModel()

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAuthBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_auth
        )
        binding.authViewModel = viewModel
        binding.lifecycleOwner = this
        binding.authButton.setOnClickListener { launchSignInFlow() }
        FirebaseUserLiveData().observe(this, { user ->
            if (user != null) {
                Timber.i("Authenticated, sending to Overview: ${user.displayName}")
                val overviewActivityIntent = Intent(applicationContext, OverviewActivity::class.java)
                startActivity(overviewActivityIntent)
            } else {
                Timber.i("Unauthenticated")
            }
        })
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
}
