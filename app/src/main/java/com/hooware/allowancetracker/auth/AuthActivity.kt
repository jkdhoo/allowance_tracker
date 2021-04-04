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
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseViewModel
import com.hooware.allowancetracker.databinding.ActivityAuthBinding
import com.hooware.allowancetracker.network.QuoteResponseTO
import com.hooware.allowancetracker.overview.OverviewActivity
import timber.log.Timber
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Authentication activity
 */
class AuthActivity : AppCompatActivity() {

    class AuthViewModel(application: AllowanceApp) : BaseViewModel(application) {
        fun logAuthState() {
            Timber.i("Authentication State: ${authenticationState.value}")
            Timber.i("Authentication Type: $authenticationType")
        }
    }

    private val viewModel: AuthViewModel by viewModel()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                Timber.i("Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                Timber.i("Sign in unsuccessful")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isFreshAuthStart = intent.getBooleanExtra("fresh_auth_start", true)
        val binding: ActivityAuthBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_auth
        )
        binding.authViewModel = viewModel
        binding.authButton.setOnClickListener { launchSignInFlow() }
        Timber.i("Observe Authentication State")
        viewModel.authenticationState.observe(this, { authenticationState ->
            when (authenticationState) {
                AllowanceApp.AuthenticationState.AUTHENTICATED -> {
                    if (isFreshAuthStart) {
                        Timber.i("Authenticated, routing to Reminders")
                        val overviewActivityIntent = Intent(applicationContext, OverviewActivity::class.java)
                        val bundle = this.intent.extras
                        if (bundle != null) {
                            val quoteResponse: QuoteResponseTO? = bundle.getParcelable("quoteResponse")
                            overviewActivityIntent.putExtra("quoteResponse", quoteResponse)
                        }
                        startActivity(overviewActivityIntent)
                        finishAffinity()
                    }
                }
                else -> {
                    Timber.i("Unauthenticated")
                    if (!isFreshAuthStart) {
                        Snackbar.make(
                            binding.root, this.getString(R.string.login_unsuccessful_msg),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    isFreshAuthStart = true
                }
            }
        })

        viewModel.logAuthState()
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
