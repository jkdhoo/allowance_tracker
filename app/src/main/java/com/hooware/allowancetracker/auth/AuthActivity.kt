package com.hooware.allowancetracker.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.databinding.ActivityAuthenticationBinding
import com.hooware.allowancetracker.overview.OverviewActivity
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the OverviewActivity.
 */
class AuthActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by inject()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                Timber.i("Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                Timber.i("Sign in unsuccessful")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isNotLoggingOut = intent.getBooleanExtra("fresh_auth_start", true)
        val binding: ActivityAuthenticationBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_authentication
        )
        binding.authViewModel = viewModel
        binding.authButton.setOnClickListener { launchSignInFlow() }
        Timber.i("Observe Authentication State")
        viewModel.authenticationState.observe(this, { authenticationState ->
            when (authenticationState) {
                AuthViewModel.AuthenticationState.AUTHENTICATED -> {
                    if (isNotLoggingOut) {
                        Timber.i("Authenticated, routing to Reminders")
                        val reminderActivityIntent =
                            Intent(applicationContext, OverviewActivity::class.java)
                        startActivity(reminderActivityIntent)
                    }
                }
                AuthViewModel.AuthenticationState.INVALID_AUTHENTICATION -> {
                    Timber.i("Unauthenticated")
                    Snackbar.make(
                        binding.root, this.getString(R.string.login_unsuccessful_msg),
                        Snackbar.LENGTH_LONG
                    ).show()
                    isNotLoggingOut = true
                }
                else -> {
                    Timber.i("Unable to act on authentication state $authenticationState")
                    isNotLoggingOut = true
                }
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
