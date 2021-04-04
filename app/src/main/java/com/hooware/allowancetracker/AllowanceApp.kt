package com.hooware.allowancetracker

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.hooware.allowancetracker.auth.AuthActivity
import com.hooware.allowancetracker.auth.FirebaseUserLiveData
import com.hooware.allowancetracker.data.local.DataSource
import com.hooware.allowancetracker.data.local.LocalDB
import com.hooware.allowancetracker.data.local.LocalRepository
import com.hooware.allowancetracker.data.local.children.ChildrenDao
import com.hooware.allowancetracker.data.local.quotes.QuoteDao
import com.hooware.allowancetracker.data.local.transactions.TransactionsDao
import com.hooware.allowancetracker.overview.OverviewViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class AllowanceApp : Application() {

    enum class AuthenticationState  {
        AUTHENTICATED, UNAUTHENTICATED
    }

    enum class AuthenticationType {
        ADMINISTRATOR, ADULT, CHILD, UNKNOWN
    }

    lateinit var authenticationState: LiveData<AuthenticationState>

    private var _authenticationType = MutableLiveData<AuthenticationType>()
    val authenticationType: LiveData<AuthenticationType>
        get() = _authenticationType

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        Timber.i("Tree Planted")

        @Suppress("USELESS_CAST") // Needed to cast the LocalRepository as a DataSource for the viewModels
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel { OverviewViewModel(this@AllowanceApp, get() as DataSource) }
            viewModel { AuthActivity.AuthViewModel(this@AllowanceApp) }
            viewModel { SplashViewModel(this@AllowanceApp, get() as DataSource) }
            single { LocalRepository(get() as TransactionsDao, get() as ChildrenDao, get() as QuoteDao) as DataSource }
            single { LocalDB.createChildrenDao(this@AllowanceApp) }
            single { LocalDB.createTransactionsDao(this@AllowanceApp) }
            single { LocalDB.createQuoteDao(this@AllowanceApp) }
        }

        startKoin {
            androidLogger()
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
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updated = task.result
                Timber.i("Config params updated: $updated")
            } else {
                Timber.i("Config param update failed.")
            }
        }

        setAuthenticationStateAndType()
    }

    private fun setAuthenticationStateAndType() {
        authenticationState = FirebaseUserLiveData().map { user ->
            setAuthenticationType(user)
            if (user != null) {
                AuthenticationState.AUTHENTICATED
            } else {
                AuthenticationState.UNAUTHENTICATED
            }
        }
    }

    private fun setAuthenticationType(user: FirebaseUser?) {
        val administrators = firebaseConfigRetriever("administrator_uid")
        val parents = firebaseConfigRetriever("parent_uid")
        val children = firebaseConfigRetriever("child_uid")

        if (user != null) {
            _authenticationType.value = when {
                administrators.contains(user.uid) -> { AuthenticationType.ADMINISTRATOR }
                parents.contains(user.uid) -> { AuthenticationType.ADULT }
                children.contains(user.uid) -> { AuthenticationType.CHILD }
                else -> { AuthenticationType.UNKNOWN }
            }
        } else {
            _authenticationType.value = AuthenticationType.UNKNOWN
        }
    }

    fun firebaseConfigRetriever(param: String): String {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        return remoteConfig.getString(param)
    }
}