package com.hooware.allowancetracker.overview

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseViewModel
import com.hooware.allowancetracker.network.QuoteNetworkService
import com.hooware.allowancetracker.notifications.SendNewMessageNotifications
import com.hooware.allowancetracker.to.ChildTO
import com.hooware.allowancetracker.to.NotificationSaveItemTO
import com.hooware.allowancetracker.to.QuoteResponseTO
import com.hooware.allowancetracker.utils.GlideApp
import com.hooware.allowancetracker.utils.HasAlreadyLoadedQuote
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
    val chatDatabase = Firebase.database.reference.child("chat").ref
    private val notificationDatabase = Firebase.database.reference.child("notifications").ref
    private val notificationHistoryDatabase = Firebase.database.reference.child("notificationHistory").ref

    private var _quoteResponseTO = MutableLiveData<QuoteResponseTO>()
    val quoteResponseTO: LiveData<QuoteResponseTO>
        get() = _quoteResponseTO

    private var _kidsList = MutableLiveData<List<ChildTO>>()
    val kidsList: LiveData<List<ChildTO>>
        get() = _kidsList

    private var _chatList = MutableLiveData<List<Triple<String, String, String>>>()
    val chatList: LiveData<List<Triple<String, String, String>>>
        get() = _chatList

    private var _notificationHistoryList = MutableLiveData<List<Pair<String, NotificationSaveItemTO>>>()
    val notificationHistoryList: LiveData<List<Pair<String, NotificationSaveItemTO>>>
        get() = _notificationHistoryList

    private var _screenLoaded = MutableLiveData<Boolean>()
    val screenLoaded: LiveData<Boolean>
        get() = _screenLoaded

    private var _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean>
        get() = _showLoading

    private var _imageReadyToLoad = MutableLiveData<Boolean>()
    val imageReadyToLoad: LiveData<Boolean>
        get() = _imageReadyToLoad

    private var _insertChatContent = MutableLiveData<Boolean>()
    val insertChatContent: LiveData<Boolean>
        get() = _insertChatContent

    val kidsLoaded = MutableLiveData<Boolean>()
    val chatLoaded = MutableLiveData<Boolean>()
    val imageLoaded = MutableLiveData<Boolean>()
    val notificationHistoryLoaded = MutableLiveData<Boolean>()
    val showNotificationHistoryEmpty = MutableLiveData<Boolean>()

    private val chatName = app.authType.value?.simpleName ?: ""

    init {
        kidsDatabase.keepSynced(true)
        quoteDatabase.keepSynced(true)
        chatDatabase.keepSynced(true)
        notificationDatabase.keepSynced(true)
        notificationHistoryDatabase.keepSynced(true)
    }

    fun setup() {
        _imageReadyToLoad.value = false
        imageLoaded.value = false
        kidsLoaded.value = false
        chatLoaded.value = false
        notificationHistoryLoaded.value = false
        showNotificationHistoryEmpty.value = true
        _notificationHistoryList.value = mutableListOf()
        _kidsList.value = mutableListOf()
        _chatList.value = mutableListOf()
        _showLoading.value = true
        _insertChatContent.value = false
        loadQuote()
    }

    fun loadQuote() {
        when {
            imageLoaded.value == true -> {
                Timber.i("Quote and image already loaded")
                return
            }
            imageReadyToLoad.value == true -> {
                Timber.i("Quote already loaded")
                _imageReadyToLoad.value = true
                return
            }
            HasAlreadyLoadedQuote.execute(app) -> {
                Timber.i("Quote recently retrieved, loading from database")
                getDatabaseQuote()
                return
            }
        }
        viewModelScope.launch {
            val quoteFinal: QuoteResponseTO
                try {
                    val responseString = QuoteNetworkService.quote.getQuoteAsync("en", "inspire").await()
                    val responseJSON = JSONObject(responseString)
                    quoteFinal = parseQuoteJsonResult(responseJSON)
                    quoteDatabase.setValue(quoteFinal)
                        .addOnSuccessListener {
                            Timber.i("Quote saved and loaded: $quoteFinal")
                            _quoteResponseTO.value = quoteFinal
                            _imageReadyToLoad.value = true
                        }
                        .addOnFailureListener {
                            Timber.i("Quote not saved but loaded: $quoteFinal")
                            _quoteResponseTO.value = quoteFinal
                            _imageReadyToLoad.value = true
                        }
                } catch (e: Exception) {
                    getDatabaseQuote()
                }
        }
    }

    private fun getDatabaseQuote() {
        quoteDatabase.get()
            .addOnSuccessListener {
                _quoteResponseTO.value = it.getValue(QuoteResponseTO::class.java)
                _imageReadyToLoad.value = true
                Timber.i("Local quote loaded")
            }
            .addOnFailureListener {
                _quoteResponseTO.value = getDefaultQuote()
                _imageReadyToLoad.value = true
                Timber.i("Default quote loaded")
            }
    }

    private fun getDefaultQuote(): QuoteResponseTO {
        return QuoteResponseTO(
            quote = firebaseConfigRetriever("default_quote"),
            author = firebaseConfigRetriever("default_author")
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
                    Timber.i("Received chatDB, adding items")
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

    fun loadNotificationHistory() {
        if (notificationHistoryLoaded.value == true) {
            Timber.i("Notification history already loaded")
            return
        }
        viewModelScope.launch {
            notificationHistoryDatabase.get()
                .addOnSuccessListener { db ->
                    val historyList = mutableListOf<Pair<String,NotificationSaveItemTO>>()
                    db.children.forEach { notification ->
                        val notificationItem = notification.value as? NotificationSaveItemTO ?: return@forEach
                        historyList.add(Pair(notification.key.toString(), notificationItem))
                    }
                    _notificationHistoryList.value = historyList.sortedBy { it.first }
                    Timber.i("Notification history saved and loaded")
                    showNotificationHistoryEmpty.value = historyList.isNullOrEmpty()
                    notificationHistoryLoaded.value = true
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
                            if (childTO.id == app.firebaseUID.value) {
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
        if (imageLoaded.value == true) {
            Timber.i("Image already loaded")
            return
        }
        viewModelScope.launch {
            val imgUrl = "picsum.photos/400/200"
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            GlideApp.with(imgView.context)
                .load(imgUri)
                .apply(
                    RequestOptions()
                        .error(R.drawable.ic_broken_image)
                )
                .into(imgView)
            Timber.i("Image loaded")
            imageLoaded.value = true
            showLoadingCheck()
        }
    }

    private fun showLoadingCheck() {
        if (kidsLoaded.value == true && imageLoaded.value == true && chatLoaded.value == true) {
            _insertChatContent.value = true
            _showLoading.value = false
        } else {
            _insertChatContent.value = false
            _showLoading.value = true
        }
    }

    private fun firebaseConfigRetriever(param: String): String {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        return remoteConfig.getString(param)
    }

    fun saveChatItem(message: String) {
        chatDatabase.child(System.currentTimeMillis().toString()).child(chatName).setValue(message).addOnSuccessListener {
            showToast.value = "Message sent!"
            notificationDatabase.get().addOnSuccessListener { snapshot ->
                SendNewMessageNotifications.execute(app, snapshot, message)
            }
        }
    }

    fun insertChatContent(layout: LinearLayout, fragment: Fragment) {
        viewModelScope.launch {
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
            val scrollView = layout.parent as? NestedScrollView ?: return@launch
            scrollView.postDelayed({ scrollView.fullScroll(View.FOCUS_DOWN) }, 1000)
        }
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

    private fun parseQuoteJsonResult(jsonResult: JSONObject): QuoteResponseTO {
        val resultContents = jsonResult.getJSONObject("contents")
            .getJSONArray("quotes")
            .getJSONObject(0)
        val resultQuote = resultContents.getString("quote")
        val resultAuthor = resultContents.getString("author")
        return QuoteResponseTO(resultQuote, resultAuthor)
    }

    fun isOverviewShowing(bool: Boolean) {
        app.isOverviewShowing.value = bool
    }

    fun resetNotificationHistoryLoaded() {
        notificationHistoryLoaded.value = false
        showNotificationHistoryEmpty.value = true
    }

    fun reset() {
        _imageReadyToLoad.value = false
        imageLoaded.value = false
        kidsLoaded.value = false
        chatLoaded.value = false
        notificationHistoryLoaded.value = false
        showNotificationHistoryEmpty.value = true
        _notificationHistoryList.value = mutableListOf()
        _kidsList.value = mutableListOf()
        _chatList.value = mutableListOf()
        _showLoading.value = true
        _insertChatContent.value = false
    }
}