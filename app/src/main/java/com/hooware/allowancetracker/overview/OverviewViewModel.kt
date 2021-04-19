package com.hooware.allowancetracker.overview

import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseViewModel
import com.hooware.allowancetracker.network.Network
import com.hooware.allowancetracker.network.QuoteResponseTO
import com.hooware.allowancetracker.network.parseQuoteJsonResult
import com.hooware.allowancetracker.to.ChildTO
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

class OverviewViewModel(application: AllowanceApp) : BaseViewModel(application) {

    private val quoteDatabase = Firebase.database.reference.child("quote").ref
    val kidsDatabase = Firebase.database.reference.child("kids").ref

    private var _quoteResponseTO = MutableLiveData<QuoteResponseTO>()
    val quoteResponseTO: LiveData<QuoteResponseTO>
        get() = _quoteResponseTO

    private var _kidsList = MutableLiveData<List<ChildTO>>()
    val kidsList: LiveData<List<ChildTO>>
        get() = _kidsList

    private var _quoteLoaded = MutableLiveData<Boolean>()
    val quoteLoaded: LiveData<Boolean>
        get() = _quoteLoaded

    private val kidsLoaded = MutableLiveData<Boolean>()

    private var _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean>
        get() = _showLoading

    private val firebaseUID = MutableLiveData<String>()

    init {
        kidsDatabase.keepSynced(true)
        quoteDatabase.keepSynced(true)
        _quoteLoaded.value = false
        kidsLoaded.value = false
        _kidsList.value = mutableListOf()
        _showLoading.value = true
        loadQuote()
    }

    private fun loadQuote() {
        if (quoteLoaded.value == true) {
            showLoadingCheck()
        }
        viewModelScope.launch {
            val quoteFinal: QuoteResponseTO
                try {
                    val responseString = Network.quote.getQuoteAsync("en", "inspire").await()
                    val responseJSON = JSONObject(responseString)
                    quoteFinal = parseQuoteJsonResult(responseJSON)
                    _quoteResponseTO.value = quoteFinal
                    _quoteLoaded.value = true
                    showLoadingCheck()
                    quoteDatabase.setValue(quoteFinal)
                        .addOnSuccessListener {
                            Timber.i("Quote saved and loaded: $quoteFinal")
                        }
                        .addOnFailureListener {
                            Timber.i("Quote not saved but loaded: $quoteFinal")
                        }
                } catch (e: Exception) {
                    quoteDatabase.get()
                        .addOnSuccessListener {
                            _quoteResponseTO.value = it.getValue(QuoteResponseTO::class.java)
                            _quoteLoaded.value = true
                            showLoadingCheck()
                            Timber.i("Network error($e), local quote loaded")
                        }
                        .addOnFailureListener {
                            _quoteResponseTO.value = getDefaultQuote()
                            _quoteLoaded.value = true
                            showLoadingCheck()
                            Timber.i("Network error($e), default quote loaded")
                        }
                }
        }
    }

    private fun getDefaultQuote(): QuoteResponseTO {
        return QuoteResponseTO(
            quote = firebaseConfigRetriever("default_quote"),
            author = firebaseConfigRetriever("default_author"),
            backgroundImage = firebaseConfigRetriever("default_backgroundImage")
        )
    }

    fun loadKids() {
        if (kidsLoaded.value == true) {
            showLoadingCheck()
            return
        }
        viewModelScope.launch {
                kidsDatabase.get()
                    .addOnSuccessListener { kidsDB ->
                        var kidsList = mutableListOf<ChildTO>()
                        kidsDB.children.forEach { databaseChild ->
                            val childTO = databaseChild.getValue(ChildTO::class.java) ?: return@forEach
                            if (childTO.id == firebaseUID.value) {
                                childTO.totalAllowance = totalAllowance(childTO)
                                kidsList = mutableListOf()
                                kidsList.add(childTO)
                                _kidsList.value = kidsList
                                kidsLoaded.value = true
                                return@addOnSuccessListener
                            }
                            childTO.totalAllowance = totalAllowance(childTO)
                            kidsList.add(childTO)
                        }
                        if (kidsList.isEmpty()) {
                            kidsList = getDefaultKids()
                        }
                        _kidsList.value = kidsList
                        kidsLoaded.value = true
                    }
                    .addOnFailureListener {
                        _kidsList.value = getDefaultKids()
                        kidsLoaded.value = true
                    }
            }
    }

    private fun getDefaultKids(): MutableList<ChildTO> {
        val levi = ChildTO(
            name = "Levi",
            birthday = "09/28/2007",
            age = "13",
            id = "ohi9UvJ040cPyUusqxWTU3DSwj72",
            totalAllowance = "$0.0"
        )
        val laa = ChildTO(
            name = "Laavingonn",
            birthday = "08/02/2010",
            age = "10",
            id = "27d7e2c6-213e-4554-b9d7-db32b9f0c3b6",
            totalAllowance = "$0.0"
        )
        return mutableListOf(levi, laa)
    }

    fun displayQuoteImage(imgView: ImageView) {
        viewModelScope.launch {
            val imgUrl = quoteResponseTO.value?.backgroundImage ?: firebaseConfigRetriever("default_backgroundImage")
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            Glide.with(imgView.context)
                .load(imgUri)
                .apply(
                    RequestOptions()
                        .error(R.drawable.ic_broken_image)
                )
                .into(imgView)
            Timber.i("Image loaded")
        }
    }

    private fun showLoadingCheck() {
        _showLoading.value = kidsLoaded.value != true && quoteLoaded.value != true
    }

    private fun firebaseConfigRetriever(param: String): String {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        return remoteConfig.getString(param)
    }

    private fun totalAllowance(child: ChildTO): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.maximumFractionDigits = 2
        var allowanceTotal = 0.0
        child.transactions?.forEach { transactionTO ->
            val amount = transactionTO.value.amount
            allowanceTotal = if (amount.startsWith("$")) {
                allowanceTotal + NumberFormat.getInstance(Locale.US).parse(amount.drop(1))?.toDouble()!!
            } else {
                allowanceTotal - NumberFormat.getInstance(Locale.US).parse(amount.drop(2))?.toDouble()!!
            }
        }
        return format.format(allowanceTotal).toString()
    }

    fun setFirebaseUID(userId: String) {
        firebaseUID.value = userId
        resetKidsLoaded()
        loadKids()
    }

    fun resetKidsLoaded() {
        kidsLoaded.value = false
    }

    fun reset() {
        _quoteResponseTO.value = QuoteResponseTO()
        _kidsList.value = mutableListOf()
        _quoteLoaded.value = false
        _showLoading.value = false
        kidsLoaded.value = false
        firebaseUID.value = ""
    }
}