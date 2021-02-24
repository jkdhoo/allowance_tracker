package com.hooware.allowancetracker

import android.app.Application
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.hooware.allowancetracker.auth.AuthViewModel
import com.hooware.allowancetracker.data.local.DataSource
import com.hooware.allowancetracker.data.local.LocalDB
import com.hooware.allowancetracker.data.local.LocalRepository
import com.hooware.allowancetracker.data.local.children.ChildrenDao
import com.hooware.allowancetracker.data.local.quotes.QuoteDao
import com.hooware.allowancetracker.data.local.transactions.TransactionsDao
import com.hooware.allowancetracker.overview.OverviewViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class AllowanceApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        Timber.i("Tree Planted")

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                OverviewViewModel(this@AllowanceApp, get() as DataSource)
            }
            single { AuthViewModel(get()) }
            single { LocalRepository(get() as TransactionsDao, get() as ChildrenDao, get() as QuoteDao) as DataSource }
            single { LocalDB.createChildrenDao(this@AllowanceApp) }
            single { LocalDB.createTransactionsDao(this@AllowanceApp) }
            single { LocalDB.createQuoteDao(this@AllowanceApp) }
        }

        startKoin {
            androidContext(this@AllowanceApp)
            modules(listOf(myModule))
        }

        //Setup FireBase Remote Config
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(60)
            .setFetchTimeoutInSeconds(60)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Timber.i("Config params updated: $updated")
                } else {
                    Timber.i("Config param update failed.")
                }
            }
    }

    fun firebaseConfigRetriever(param: String): String {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        return remoteConfig.getString(param)
    }
}