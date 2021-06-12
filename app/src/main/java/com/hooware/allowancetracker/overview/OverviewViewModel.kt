package com.hooware.allowancetracker.overview

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
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
import com.hooware.allowancetracker.utils.RetrieveChildAgeFromBirthday
import com.hooware.allowancetracker.utils.toTimestamp
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.util.*


class OverviewViewModel(application: AllowanceApp) : BaseViewModel(application) {
    val app = application

    private val quoteDatabase = Firebase.database.reference.child("quote").ref
    val kidsDatabase = Firebase.database.reference.child("kids").ref
    val notificationDatabase = Firebase.database.reference.child("notifications").ref
    val chatDatabase = Firebase.database.reference.child("chat").ref

    private var _quoteResponseTO = MutableLiveData<QuoteResponseTO>()
    val quoteResponseTO: LiveData<QuoteResponseTO>
        get() = _quoteResponseTO

    private var _kidsList = MutableLiveData<List<ChildTO>>()
    val kidsList: LiveData<List<ChildTO>>
        get() = _kidsList

    private var _chatList = MutableLiveData<List<Triple<String, String, String>>>()
    private val chatList: LiveData<List<Triple<String, String, String>>>
        get() = _chatList

    private var _screenLoaded = MutableLiveData<Boolean>()
    val screenLoaded: LiveData<Boolean>
        get() = _screenLoaded

    private var _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean>
        get() = _showLoading

    private var _displayQuoteImage = MutableLiveData<Boolean>()
    val displayQuoteImage: LiveData<Boolean>
        get() = _displayQuoteImage

    private var _insertChatContent = MutableLiveData<Boolean>()
    val insertChatContent: LiveData<Boolean>
        get() = _insertChatContent

    private val quoteLoaded = MutableLiveData<Boolean>()
    private val kidsLoaded = MutableLiveData<Boolean>()
    private val chatLoaded = MutableLiveData<Boolean>()

    private val firebaseUID = MutableLiveData<String>()

    private var chatName = ""

    init {
        kidsDatabase.keepSynced(true)
        quoteDatabase.keepSynced(true)
        chatDatabase.keepSynced(true)
        notificationDatabase.keepSynced(true)
    }

    fun resume() {
        quoteLoaded.value = false
        kidsLoaded.value = false
        chatLoaded.value = false
        _kidsList.value = mutableListOf()
        _chatList.value = mutableListOf()
        _showLoading.value = true
        _displayQuoteImage.value = false
        _insertChatContent.value = false
        loadQuote()
        loadKids()
        loadChat()
    }

    fun loadQuote() {
        if (quoteLoaded.value == true) {
            Timber.i("Quote already loaded")
            showLoadingCheck()
            return
        }
        viewModelScope.launch {
            val quoteFinal: QuoteResponseTO
                try {
                    val responseString = Network.quote.getQuoteAsync("en", "inspire").await()
                    val responseJSON = JSONObject(responseString)
                    quoteFinal = parseQuoteJsonResult(responseJSON)
                    _quoteResponseTO.value = quoteFinal
                    quoteLoaded.value = true
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
                            quoteLoaded.value = true
                            showLoadingCheck()
                            Timber.i("Network error($e), local quote loaded")
                        }
                        .addOnFailureListener {
                            _quoteResponseTO.value = getDefaultQuote()
                            quoteLoaded.value = true
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

    fun loadChat() {
        if (chatLoaded.value == true) {
            Timber.i("Chat already loaded")
            showLoadingCheck()
            return
        }
        viewModelScope.launch {
            chatDatabase.get()
                .addOnSuccessListener { chatDB ->
                    val chatList = mutableListOf<Triple<String, String, String>>()
                    chatDB.children.forEach { chatChild ->
                        val messageId = chatChild.key.toString()
                        var chatName = ""
                        var chatMessage = ""
                        chatChild.children.forEach { chatContainer ->
                            chatName = chatContainer.key.toString()
                            chatMessage = chatContainer.value.toString()
                        }
                        if (chatName.isEmpty() || chatMessage.isEmpty()) return@addOnSuccessListener
                        val chat = Triple(messageId, chatName, chatMessage)
                        chatList.add(chat)
                    }
                    _chatList.value = chatList.sortedBy { it.first }
                    Timber.i("Chat saved and loaded")
                    chatLoaded.value = true
                    showLoadingCheck()
                }
        }
    }

    fun loadKids() {
        if (kidsLoaded.value == true) {
            Timber.i("Kids already loaded")
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
                                childTO.age = RetrieveChildAgeFromBirthday.execute(app, childTO.birthday)
                                kidsList = mutableListOf()
                                kidsList.add(childTO)
                                _kidsList.value = kidsList
                                kidsLoaded.value = true
                                return@addOnSuccessListener
                            }
                            childTO.age = RetrieveChildAgeFromBirthday.execute(app, childTO.birthday)
                            kidsList.add(childTO)
                        }
                        _kidsList.value = kidsList
                        kidsLoaded.value = true
                        Timber.i("Kids saved and loaded")
                        showLoadingCheck()
                    }
                    .addOnFailureListener {
                        _kidsList.value = emptyList()
                        kidsLoaded.value = true
                        Timber.i("Kids failed to load")
                        showLoadingCheck()
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
        if (kidsLoaded.value == true && quoteLoaded.value == true && chatLoaded.value == true) {
            _displayQuoteImage.value = true
            _insertChatContent.value = true
            _showLoading.value = false
        } else {
            _displayQuoteImage.value = false
            _insertChatContent.value = false
            _showLoading.value = true
        }
    }

    private fun firebaseConfigRetriever(param: String): String {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        return remoteConfig.getString(param)
    }

    fun setFirebaseUID(userId: String) {
        firebaseUID.value = userId
        chatName = when {
            firebaseConfigRetriever("laa_uid").contains(userId) -> "Laa"
            firebaseConfigRetriever("levi_uid").contains(userId) -> "Levi"
            firebaseConfigRetriever("mom_uid").contains(userId) -> "Mom"
            firebaseConfigRetriever("dad_uid").contains(userId) -> "Dad"
            else -> ""
        }
    }

    fun reset() {
        _quoteResponseTO.value = QuoteResponseTO()
        _kidsList.value = mutableListOf()
        _chatList.value = mutableListOf()
        quoteLoaded.value = false
        _showLoading.value = true
        _insertChatContent.value = false
        _displayQuoteImage.value = false
        kidsLoaded.value = false
        chatLoaded.value = false
        firebaseUID.value = ""
    }

    fun saveChatItem(message: String) {
        chatDatabase.child(System.currentTimeMillis().toString()).child(chatName).setValue(message)
    }

    fun insertChatContent(layout: LinearLayout, fragment: Fragment) {
        layout.removeAllViews()
        chatList.value?.forEach { chatItem ->
            val view = fragment.layoutInflater.inflate(R.layout.it_chat, null)
            val name = view.findViewById(R.id.name) as TextView
            name.text = chatItem.second
            name.setTextColor(chooseColorByName(name, chatItem.second))

            val timestamp = view.findViewById(R.id.timestamp) as TextView
            timestamp.text = chatItem.first.toLong().toTimestamp
            timestamp.setTextColor(chooseColorByName(name, chatItem.second))

            val chat = view.findViewById(R.id.chat) as TextView
            chat.text = chatItem.third
            chat.setTextColor(chooseColorByName(name, chatItem.second))

            layout.addView(view)
        }
        layout.invalidate()
        val scrollView = layout.parent as? NestedScrollView ?: return
        scrollView.postDelayed({ scrollView.fullScroll(ScrollView.FOCUS_DOWN) }, 400)
    }

    private fun chooseColorByName(view: TextView, name: String) : Int {
        return when (name) {
            "Laa" -> view.context.resources.getColor(R.color.laa, view.context.resources.newTheme())
            "Levi" -> view.context.resources.getColor(R.color.levi, view.context.resources.newTheme())
            "Mom" -> view.context.resources.getColor(R.color.mom, view.context.resources.newTheme())
            "Dad" -> view.context.resources.getColor(R.color.dad, view.context.resources.newTheme())
            "System" -> view.context.resources.getColor(R.color.black, view.context.resources.newTheme())
            else -> 0
        }
    }
}