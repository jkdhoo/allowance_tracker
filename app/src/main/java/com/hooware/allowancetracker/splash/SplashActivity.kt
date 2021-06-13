package com.hooware.allowancetracker.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.FirebaseUserLiveData
import com.hooware.allowancetracker.databinding.ActivitySplashBinding
import com.hooware.allowancetracker.overview.OverviewActivity
import com.hooware.allowancetracker.utils.AuthType
import com.hooware.allowancetracker.utils.HandleFirebaseUserLiveData
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModel()
    private lateinit var app: AllowanceApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.lifecycleOwner = this
        app = application as AllowanceApp
        Timber.i("Wait for Splash to complete, then check authentication state.")

        viewModel.initialize()
    }

    override fun onResume() {
        super.onResume()
        viewModel.splashReady.observe(this, { readyToNavigate ->
            if (readyToNavigate) {
                FirebaseUserLiveData().observe(this, { user ->
                    HandleFirebaseUserLiveData.execute(this, user)
                })
            }
        })

        app.authType.observe(this, { authType ->
            Timber.i("$authType")
            when (authType) {
                AuthType.LEVI, AuthType.LAA, AuthType.MOM, AuthType.DAD -> {
                    Timber.i("Recognized, routing to Overview")
                    val intent = Intent(this, OverviewActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.xml.slide_up_from_bottom, R.xml.slide_up_and_out)
                }
                else -> Timber.i("Unrecognized")
            }
        })
    }

    override fun onPause() {
        viewModel.splashReady.removeObservers(this)
        app.authType.removeObservers(this)
        FirebaseUserLiveData().removeObservers(this)
        super.onPause()
    }
}