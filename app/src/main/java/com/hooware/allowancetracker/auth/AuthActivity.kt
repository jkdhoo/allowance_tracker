package com.hooware.allowancetracker.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.databinding.ActivityAuthBinding
import com.hooware.allowancetracker.overview.OverviewActivity
import com.hooware.allowancetracker.utils.AuthType
import timber.log.Timber

/**
 * Authentication activity
 */
class AuthActivity : AppCompatActivity() {

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Timber.i("${it.resultCode}")
        if (it.resultCode == Activity.RESULT_OK) {
            processAuthResult()
        } else {
            finish()
        }
    }

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

    private fun processAuthResult() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid ?: run {
            finish()
            return
        }
        val app = application as AllowanceApp
        app.firebaseUID.value = userId
        SetAuthType.execute(userId, app)
        when (app.authType.value) {
            AuthType.LEVI, AuthType.LAA, AuthType.MOM, AuthType.DAD -> {
                Timber.i("Recognized, routing to Overview")
                val intent = Intent(this, OverviewActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.xml.slide_up_from_bottom, R.xml.slide_up_and_out)
            }
            else -> finish()
        }
    }
}