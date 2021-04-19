package com.hooware.allowancetracker

import android.app.Application
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.hooware.allowancetracker.auth.AuthViewModel
import com.hooware.allowancetracker.overview.OverviewViewModel
import com.hooware.allowancetracker.splash.SplashViewModel
import com.hooware.allowancetracker.transactions.TransactionsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class AllowanceApp : Application() {

    override fun onCreate() {
        super.onCreate()

            Timber.plant(Timber.DebugTree())
            Timber.i("Timber initialized.")

        initKoin()
        initFirebase()
    }

    private fun initKoin() {
        val myModule = module {
            viewModel { OverviewViewModel(this@AllowanceApp) }
            viewModel { AuthViewModel(this@AllowanceApp) }
            viewModel { SplashViewModel(this@AllowanceApp) }
            viewModel { TransactionsViewModel(this@AllowanceApp) }
        }

        startKoin {
            androidLogger()
            androidContext(this@AllowanceApp)
            modules(listOf(myModule))
        }
    }

    private fun initFirebase() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(60)
            .setFetchTimeoutInSeconds(60)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updated = task.result
                Timber.i("Firebase Config params updated: $updated")
            } else {
                Timber.i("Firebase Config param update failed.")
            }
        }
    }
}