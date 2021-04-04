package com.hooware.allowancetracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hooware.allowancetracker.auth.AuthActivity
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.databinding.ActivitySplashBinding
import com.hooware.allowancetracker.overview.OverviewActivity
import timber.log.Timber
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.splashViewModel = viewModel
        viewModel.initialize()
        Timber.i("Waiting for Splash to complete, then navigate.")
        viewModel.splashReady.observe(this, { readyToNavigate ->
            if (readyToNavigate) {
                setAuthObserver()
            }
        })

        viewModel.logAuthState()
    }

    private fun setAuthObserver() {
        Timber.i("Check Authentication State")
        viewModel.authenticationState.observe(this, { authState ->
            val intent: Intent = when (authState) {
                AllowanceApp.AuthenticationState.AUTHENTICATED -> {
                    Timber.i("Authenticated, routing to Overview")
                    Intent(applicationContext, OverviewActivity::class.java)
                        .putExtra("quoteResponse", viewModel.quoteResponse.value)
                }
                else -> {
                    Timber.i("Not authenticated, routing to Authentication")
                    Intent(applicationContext, AuthActivity::class.java)
                        .putExtra("quoteResponse", viewModel.quoteResponse.value)
                }
            }
            startActivity(intent)
            finish()
        })
    }
}