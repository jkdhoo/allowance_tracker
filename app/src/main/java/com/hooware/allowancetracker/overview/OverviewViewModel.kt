package com.hooware.allowancetracker.overview

import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.AuthType
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseViewModel
import com.hooware.allowancetracker.to.ChildTO
import com.hooware.allowancetracker.network.Network
import com.hooware.allowancetracker.network.QuoteResponseTO
import com.hooware.allowancetracker.network.parseQuoteJsonResult
import com.hooware.allowancetracker.transactions.TransactionDataItem
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

class OverviewViewModel(application: AllowanceApp) : BaseViewModel(application) {

    val childName = MutableLiveData<String>()
    val childAge = MutableLiveData<String>()
    val childBirthday = MutableLiveData<String>()
    val transactionDescription = MutableLiveData<String>()
    val transactionAmount = MutableLiveData<String>()
    var editChildDetails = MutableLiveData<Boolean>()

    private var _quoteResponseTO = MutableLiveData<QuoteResponseTO>()
    val quoteResponseTO: LiveData<QuoteResponseTO>
        get() = _quoteResponseTO

    private var _kidsList = MutableLiveData<List<ChildTO>>()
    val kidsList: LiveData<List<ChildTO>>
        get() = _kidsList

    private val quoteDatabase = Firebase.database.reference.child("quote").ref
    private val kidsDatabase = Firebase.database.reference.child("kids").ref

    private var _quoteLoaded = MutableLiveData<Boolean>()
    val quoteLoaded: LiveData<Boolean>
        get() = _quoteLoaded

    private var _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean>
        get() = _showLoading

    private var _kidsLoaded = MutableLiveData<Boolean>()
    val kidsLoaded: LiveData<Boolean>
        get() = _kidsLoaded

    var firebaseUID = MutableLiveData<String>()

    init {
        _quoteLoaded.value = false
        _showLoading.value = true
        _kidsLoaded.value = false
        quoteDatabase.keepSynced(true)
        kidsDatabase.keepSynced(true)
    }

    fun loadQuote() {
        viewModelScope.launch {
            val quoteFinal: QuoteResponseTO
            if (quoteLoaded.value == false) {
                try {
                    val responseString = Network.quote.getQuoteAsync("en", "inspire").await()
                    val responseJSON = JSONObject(responseString)
                    quoteFinal = parseQuoteJsonResult(responseJSON)
                    quoteDatabase.setValue(quoteFinal)
                        .addOnSuccessListener {
                            _quoteResponseTO.value = quoteFinal
                            _quoteLoaded.value = true
                            Timber.i("Quote saved and loaded: $quoteFinal")
                        }
                        .addOnFailureListener {
                            _quoteResponseTO.value = quoteFinal
                            _quoteLoaded.value = true
                            Timber.i("Quote not saved but loaded: $quoteFinal")
                        }
                } catch (e: Exception) {
                    quoteDatabase.get()
                        .addOnSuccessListener {
                            _quoteResponseTO.value = it.getValue(QuoteResponseTO::class.java)
                            _quoteLoaded.value = true
                            Timber.i("Network error($e), local quote loaded")
                        }
                        .addOnFailureListener {
                            _quoteResponseTO.value = getDefaultQuote()
                            _quoteLoaded.value = true
                            Timber.i("Network error($e), default quote loaded")
                        }
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
        viewModelScope.launch {
            if (kidsLoaded.value == false) {
                kidsDatabase.get()
                    .addOnSuccessListener {
                        var tempList = it.getValue<HashMap<String, ChildTO>>()?.values?.toList()?.filter { child ->
                            child.id == firebaseUID.value
                        } ?: listOf()
                        if (tempList.isNullOrEmpty()) {
                            tempList = getDefaultKids()
                        }
                        _kidsList.value = tempList
                        _kidsLoaded.value = true
                    }
                    .addOnFailureListener {
                        _kidsList.value = getDefaultKids()
                        _kidsLoaded.value = true
                    }
            }
        }
    }

    private fun getDefaultKids(): List<ChildTO> {
        val levi = ChildTO(
            name = "Levi",
            birthday = "09/28/2007",
            age = "13",
            id = "ohi9UvJ040cPyUusqxWTU3DSwj72"
        )
        val laa = ChildTO(
            name = "Laavingonn",
            birthday = "08/02/2010",
            age = "10",
            id = "27d7e2c6-213e-4554-b9d7-db32b9f0c3b6"
        )
        return listOf(levi, laa)
    }

    private fun validateEnteredTransaction(transaction: TransactionDataItem): Boolean {
        if (transaction.amount == null) {
            showSnackBarInt.value = R.string.err_enter_amount
            return false
        }
        return true
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
            _showLoading.value = false
        }
    }

//    private fun saveTransaction(transaction: TransactionDataItem, child: ChildTO) {
//        showTransactionsLoading.value = true
//        viewModelScope.launch {
//            dataSource.saveTransaction(
//                TransactionTO(
//                    transaction.name,
//                    transaction.details,
//                    transaction.amount,
//                    transaction.date
//                )
//            )
//            showTransactionsLoading.postValue(false)
//            showToast.value = app.getString(R.string.transaction_saved)
//        }
//        sendNotification(this.app.applicationContext, child, transaction)
//        navigationCommand.value = NavigationCommand.Back
//    }

//    fun validateAndSaveTransaction(transaction: TransactionDataItem, child: ChildTO) {
//        if (validateEnteredTransaction(transaction)) {
//            saveTransaction(transaction, child)
//        }
//    }

//    fun clearAllTransactions(id: String) {
//        showTransactionsLoading.value = true
//        viewModelScope.launch {
//            dataSource.deleteAllTransactions()
//            showTransactionsLoading.value = false
//            showToast.value = getApplication<Application>().getString(R.string.transactions_cleared)
//        }
//        loadTransactions(id)
//    }

//    fun clearAllChildren() {
//        showChildrenLoading.value = true
//        viewModelScope.launch {
//            dataSource.deleteAllChildren()
//            showChildrenLoading.postValue(false)
//            showToast.value = getApplication<Application>().getString(R.string.children_cleared)
//        }
//        loadChildren()
//    }

//    /**
//     * Get all the transactions from the DataSource and add them to the transactionsList to be shown on the UI,
//     * or show error if any
//     */
//    fun loadTransactions(id: String) {
//        showTransactionsLoading.value = true
//        viewModelScope.launch {
//            //interacting with the dataSource has to be through a coroutine
//            val result = dataSource.getTransactionsByChild(id)
//            Timber.i("Getting transactions")
//            showTransactionsLoading.value = false
//            Timber.i("Stopping load")
//            when (result) {
//                is ResultTO.Success<*> -> {
//                    val dataList = ArrayList<TransactionDataItem>()
//                    dataList.addAll((result.data as List<TransactionTO>).map { transaction ->
//                        //map the transaction data from the DB to the be ready to be displayed on the UI
//                        TransactionDataItem(
//                            transaction.name,
//                            transaction.details,
//                            transaction.amount,
//                            transaction.date,
//                            transaction.id
//                        )
//                    })
//                    transactionsList.value = dataList
//                }
//                is ResultTO.Error ->
//                    showSnackBar.value = result.message!!
//            }
//
//            //check if no data has to be shown
//            invalidateShowNoTransactionData()
//        }
//    }

//    /**
//     * Get all the children from the DataSource and add them to the childrenList to be shown on the UI,
//     * or show error if any
//     */
//    fun loadChildren() {
//        showChildrenLoading.value = true
//        viewModelScope.launch {
//            //interacting with the dataSource has to be through a coroutine
//            val result = dataSource.getChildren()
//            Timber.i("Getting children")
//            showChildrenLoading.value = false
//            Timber.i("Stopping load")
//            when (result) {
//                is ResultTO.Success<*> -> {
//                    val dataList = ArrayList<ChildTO>()
//                    dataList.addAll((result.data as? List<ChildTO> ?: listOf()).map { child ->
//                        //map the reminder data from the DB to the be ready to be displayed on the UI
//                        ChildTO(
//                            child.name,
//                            child.age,
//                            child.birthday,
//                            child.id
//                        )
//                    })
//                    childrenList.value = dataList
//                }
//                is ResultTO.Error ->
//                    showSnackBar.value = result.message!!
//            }
//
//            //check if no data has to be shown
//            invalidateShowNoChildData()
//        }
//    }

//    /**
//     * Inform the user that there's not any data if the remindersList is empty
//     */
//    private fun invalidateShowNoTransactionData() {
//        showNoTransactionData.value =
//            transactionsList.value == null || transactionsList.value!!.isEmpty()
//    }
//
//    private fun invalidateShowNoChildData() {
//        showNoChildData.value = childrenList.value == null || childrenList.value!!.isEmpty()
//    }

//    fun validateAndSaveChild(child: ChildTO) {
//        if (validateEnteredChild(child)) {
//            saveChild(child)
//        }
//    }

//    fun validateAndUpdateChild(child: ChildTO) {
//        if (validateEnteredChild(child)) {
//            updateChild(child)
//        }
//    }

//    private fun saveChild(child: ChildTO) {
//        showChildrenLoading.value = true
//        viewModelScope.launch {
//            dataSource.saveChild(
//                ChildTO(
//                    child.name,
//                    child.age,
//                    child.birthday
//                )
//            )
//            showChildrenLoading.postValue(false)
//            showToast.value = app.getString(R.string.child_saved)
//        }
//        navigationCommand.value = NavigationCommand.Back
//    }

//    private fun updateChild(child: ChildTO) {
//        showChildrenLoading.value = true
//        viewModelScope.launch {
//            dataSource.updateChild(
//                ChildTO(
//                    child.name,
//                    child.age,
//                    child.birthday,
//                    child.id,
//                )
//            )
//            showChildrenLoading.postValue(false)
//            showToast.value = app.getString(R.string.child_updated)
//        }
//        editChildDetails.value = false
//    }

//    private fun validateEnteredChild(child: ChildTO): Boolean {
//        if (child.name.isNullOrEmpty()) {
//            showSnackBarInt.value = R.string.err_enter_name
//            return false
//        }
//        return true
//    }
        private var _authType = MutableLiveData<AuthType>()
    val authType: LiveData<AuthType>
        get() = _authType

    fun setAuthType(user: FirebaseUser) {
        val parent = firebaseConfigRetriever("parent_uid")
        val levi = firebaseConfigRetriever("levi_uid")
        val laa = firebaseConfigRetriever("laa_uid")
        when {
            parent.contains(user.uid) -> _authType.value = AuthType.PARENT
            levi.contains(user.uid) -> _authType.value = AuthType.LEVI
            laa.contains(user.uid) -> _authType.value = AuthType.LAA
        }
        Timber.i("Auth Type: ${authType.value}")
    }

    fun firebaseConfigRetriever(param: String): String {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        return remoteConfig.getString(param)
    }
}