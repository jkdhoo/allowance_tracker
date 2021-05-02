package com.hooware.allowancetracker.overview

import android.os.Build
import android.widget.ImageView
import androidx.annotation.RequiresApi
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
import com.hooware.allowancetracker.utils.age
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class OverviewViewModel(application: AllowanceApp) : BaseViewModel(application) {

    private val quoteDatabase = Firebase.database.reference.child("quote").ref
    val kidsDatabase = Firebase.database.reference.child("kids").ref
    private val notificationDatabase = Firebase.database.reference.child("notifications").ref

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

    @RequiresApi(Build.VERSION_CODES.O)
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
                                childTO.age = childAge(childTO.birthday)
                                kidsList = mutableListOf()
                                kidsList.add(childTO)
                                _kidsList.value = kidsList
                                kidsLoaded.value = true
                                return@addOnSuccessListener
                            }
                            childTO.age = childAge(childTO.birthday)
                            kidsList.add(childTO)
                        }
                        _kidsList.value = kidsList
                        kidsLoaded.value = true
                    }
                    .addOnFailureListener {
                        _kidsList.value = emptyList()
                        kidsLoaded.value = true
                    }
            }
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

    private fun totalSpending(child: ChildTO): Double {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.maximumFractionDigits = 2
        var spendingTotal = 0.0
        child.transactions?.forEach { transactionTO ->
            val amount = transactionTO.value.spending ?: return@forEach
            spendingTotal += amount
        }
        return spendingTotal
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setFirebaseUID(userId: String) {
        firebaseUID.value = userId
        resetKidsLoaded()
        loadKids()
    }

    private fun resetKidsLoaded() {
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

    fun saveFCMToken(fcmToken: String) {
        val currentFirebaseUID = firebaseUID.value ?: return
        notificationDatabase.child(currentFirebaseUID).setValue(fcmToken)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun childAge(birthday: String): String {
        val format = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
        val date = LocalDate.parse(birthday, format)
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()).age
    }
}