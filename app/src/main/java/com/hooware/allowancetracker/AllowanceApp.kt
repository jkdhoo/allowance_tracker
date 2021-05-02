package com.hooware.allowancetracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
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
        createNotificationChannel()
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(getString(R.string.channel_id), name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}