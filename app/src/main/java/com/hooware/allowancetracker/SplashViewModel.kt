package com.hooware.allowancetracker

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hooware.allowancetracker.base.BaseViewModel
import com.hooware.allowancetracker.data.local.DataSource
import com.hooware.allowancetracker.data.to.ResultTO
import com.hooware.allowancetracker.network.Network
import com.hooware.allowancetracker.network.QuoteResponseTO
import com.hooware.allowancetracker.network.parseQuoteJsonResult
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

class SplashViewModel(application: AllowanceApp, private val dataSource: DataSource) : BaseViewModel(application) {

    companion object {
        const val SPLASH_TIME = 2000L
    }

    private var _splashReady = MutableLiveData<Boolean>()
    val splashReady: LiveData<Boolean>
        get() = _splashReady

    private val splashTimerComplete = MutableLiveData<Boolean>()
    private val splashQuoteLoaded = MutableLiveData<Boolean>()

    private var _quoteResponse = MutableLiveData<QuoteResponseTO>()
    val quoteResponse: LiveData<QuoteResponseTO>
        get() = _quoteResponse

    init {
        _splashReady.value = false
        splashQuoteLoaded.value = false
        splashTimerComplete.value = false
    }

    fun initialize() {
        viewModelScope.launch {
            startSplashTimer()
            refreshQuotes()
        }
    }

    private fun startSplashTimer() {
        val splashTime = SPLASH_TIME
        val handler = Handler(Looper.getMainLooper())
        if (splashTimerComplete.value == false) {
            handler.postDelayed({
                splashTimerComplete.value = true
                verifySplashComplete()
            }, splashTime)
        }
    }

    private suspend fun refreshQuotes() {
        if (splashQuoteLoaded.value == false) {
            try {
                val responseString = Network.quote.getQuoteAsync("en", "inspire").await()
                val responseJSON = JSONObject(responseString)
                val quoteFinal = parseQuoteJsonResult(responseJSON)
                dataSource.saveQuote(quoteFinal)
                Timber.i("Quote of the day retrieved, processing.")
                _quoteResponse.value = quoteFinal
                splashQuoteLoaded.value = true
            } catch (e: Exception) {
                Timber.i("Network error($e), checking for saved quote")
                val defaultQuote: QuoteResponseTO
                when (val retrieveQuote = dataSource.getQuote()) {
                    is ResultTO.Success -> {
                        defaultQuote = retrieveQuote.data
                        Timber.i("Saved quote found, processing.")
                    }
                    else -> {
                        defaultQuote = getDefaultQuote()
                        Timber.i("No saved quote found, default quote set, processing.")
                    }
                }
                _quoteResponse.value = defaultQuote
                splashQuoteLoaded.value = true
            }
            verifySplashComplete()
        }
    }

    private fun getDefaultQuote(): QuoteResponseTO {
        val defaultQuoteText = app.firebaseConfigRetriever("default_quote")
        val defaultAuthor = app.firebaseConfigRetriever("default_author")
        val defaultBackgroundImage = app.firebaseConfigRetriever("default_background")
        return QuoteResponseTO(
            defaultQuoteText,
            defaultAuthor,
            defaultBackgroundImage
        )
    }

    private fun verifySplashComplete() {
        if (splashTimerComplete.value!! && splashQuoteLoaded.value!!) {
            _splashReady.value = true
        }
    }

    fun logAuthState() {
        Timber.i("Authentication State: ${authenticationState.value}")
        Timber.i("Authentication Type: $authenticationType")
    }
}
