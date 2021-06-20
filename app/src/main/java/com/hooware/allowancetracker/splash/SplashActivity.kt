package com.hooware.allowancetracker.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.AuthActivity
import com.hooware.allowancetracker.databinding.ActivitySplashBinding
import com.hooware.allowancetracker.overview.OverviewActivity
import com.hooware.allowancetracker.utils.AuthType
import com.hooware.allowancetracker.auth.SetAuthType
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.lifecycleOwner = this
        Timber.i("Wait for Splash to complete, then check authentication state.")
        viewModel.initialize()
    }

    override fun onResume() {
        super.onResume()
        viewModel.splashReady.observe(this, { readyToNavigate ->
            if (readyToNavigate) {
                val app = application as AllowanceApp
                val firebaseAuth = FirebaseAuth.getInstance()
                val userId = firebaseAuth.currentUser?.uid ?: run {
                    app.firebaseUID.value = null
                    Timber.i("Not authenticated. Authenticating...")
                    val intent = Intent(this, AuthActivity::class.java)
                    startActivity(intent)
                    finish()
                    return@observe
                }
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
        })
    }

    override fun onPause() {
        viewModel.splashReady.removeObservers(this)
        super.onPause()
    }
}