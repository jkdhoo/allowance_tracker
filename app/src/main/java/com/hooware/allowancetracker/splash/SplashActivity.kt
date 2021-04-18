package com.hooware.allowancetracker.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.utils.AuthType
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.AuthActivity
import com.hooware.allowancetracker.auth.FirebaseUserLiveData
import com.hooware.allowancetracker.databinding.ActivitySplashBinding
import com.hooware.allowancetracker.overview.OverviewActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.lifecycleOwner = this
        Timber.i("Wait for Splash to complete, then check authentication state.")

        viewModel.splashReady.observe(this, { readyToNavigate ->
            if (readyToNavigate) {
                observeFirebaseUserLiveData()
            }
        })

        viewModel.initialize()
    }

    private fun observeFirebaseUserLiveData() {
        FirebaseUserLiveData().observe(this, { user ->
            if (user == null) {
                Timber.i("Not authenticated. Authenticating...")
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                viewModel.setAuthType(user)
                when (viewModel.authType.value) {
                    AuthType.LEVI, AuthType.LAA, AuthType.PARENT -> {
                        Timber.i("Recognized, routing to Overview")
                        val intent = Intent(this, OverviewActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else -> finish()
                }
            }
        })
    }
}