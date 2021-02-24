package com.hooware.allowancetracker.overview

import android.app.Application
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.FirebaseUserLiveData
import com.hooware.allowancetracker.base.BaseViewModel
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.children.ChildDataItem
import com.hooware.allowancetracker.data.local.DataSource
import com.hooware.allowancetracker.data.to.ChildTO
import com.hooware.allowancetracker.data.to.ResultTO
import com.hooware.allowancetracker.data.to.TransactionTO
import com.hooware.allowancetracker.network.Network
import com.hooware.allowancetracker.network.QuoteResponseTO
import com.hooware.allowancetracker.network.parseQuoteJsonResult
import com.hooware.allowancetracker.transactions.TransactionDataItem
import com.hooware.allowancetracker.utils.sendNotification
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

class OverviewViewModel(val app: AllowanceApp, private val dataSource: DataSource) :
    BaseViewModel(app) {

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    val childName = MutableLiveData<String>()
    val childAge = MutableLiveData<String>()
    val childBirthday = MutableLiveData<String>()
    val transactionDescription = MutableLiveData<String>()
    val transactionAmount = MutableLiveData<String>()
    val transactionDate = MutableLiveData<String>()
    var editChildDetails = MutableLiveData<Boolean>()
    var intentChild = MutableLiveData<ChildDataItem>()
    var intentTransaction = MutableLiveData<TransactionDataItem>()

    // lists that hold the data to be displayed on the UI
    val transactionsList = MutableLiveData<List<TransactionDataItem>>()
    val childrenList = MutableLiveData<List<ChildDataItem>>()

    private var _quoteLoaded = MutableLiveData<Boolean>()
    val quoteLoaded: LiveData<Boolean>
        get() = _quoteLoaded

    private var _quoteResponse = MutableLiveData<QuoteResponseTO>()
    val quoteResponse: LiveData<QuoteResponseTO>
        get() = _quoteResponse

    init {
        _quoteLoaded.value = false
        val defaultQuote = app.firebaseConfigRetriever("default_quote")
        val defaultAuthor = app.firebaseConfigRetriever("default_author")
        val defaultBackgroundImage =
            app.firebaseConfigRetriever("default_background")
        _quoteResponse.value = QuoteResponseTO(
            defaultQuote,
            defaultAuthor,
            defaultBackgroundImage
        )
        viewModelScope.launch {
            dataSource.saveQuote(_quoteResponse.value!!)
        }
    }

    fun clearAllTransactions(id: String) {
        showTransactionsLoading.value = true
        viewModelScope.launch {
            dataSource.deleteAllTransactions()
            showTransactionsLoading.value = false
            showToast.value = getApplication<Application>().getString(R.string.transactions_cleared)
        }
        loadTransactions(id)
    }

    fun clearAllChildren() {
        showChildrenLoading.value = true
        viewModelScope.launch {
            dataSource.deleteAllChildren()
            showChildrenLoading.postValue(false)
            showToast.value = getApplication<Application>().getString(R.string.children_cleared)
        }
        loadChildren()
    }

    /**
     * Get all the transactions from the DataSource and add them to the transactionsList to be shown on the UI,
     * or show error if any
     */
    fun loadTransactions(id: String) {
        showTransactionsLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getTransactionsByChild(id)
            Timber.i("Getting transactions")
            showTransactionsLoading.value = false
            Timber.i("Stopping load")
            when (result) {
                is ResultTO.Success<*> -> {
                    val dataList = ArrayList<TransactionDataItem>()
                    dataList.addAll((result.data as List<TransactionTO>).map { transaction ->
                        //map the transaction data from the DB to the be ready to be displayed on the UI
                        TransactionDataItem(
                            transaction.name,
                            transaction.details,
                            transaction.amount,
                            transaction.date,
                            transaction.id
                        )
                    })
                    transactionsList.value = dataList
                }
                is ResultTO.Error ->
                    showSnackBar.value = result.message!!
            }

            //check if no data has to be shown
            invalidateShowNoTransactionData()
        }
    }

    /**
     * Get all the children from the DataSource and add them to the childrenList to be shown on the UI,
     * or show error if any
     */
    fun loadChildren() {
        showChildrenLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getChildren()
            Timber.i("Getting children")
            showChildrenLoading.value = false
            Timber.i("Stopping load")
            when (result) {
                is ResultTO.Success<*> -> {
                    val dataList = ArrayList<ChildDataItem>()
                    dataList.addAll((result.data as List<ChildTO>).map { child ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ChildDataItem(
                            child.name,
                            child.age,
                            child.birthday,
                            child.id
                        )
                    })
                    childrenList.value = dataList
                }
                is ResultTO.Error ->
                    showSnackBar.value = result.message!!
            }

            //check if no data has to be shown
            invalidateShowNoChildData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoTransactionData() {
        showNoTransactionData.value =
            transactionsList.value == null || transactionsList.value!!.isEmpty()
    }

    private fun invalidateShowNoChildData() {
        showNoChildData.value = childrenList.value == null || childrenList.value!!.isEmpty()
    }

    fun validateAndSaveChild(child: ChildDataItem) {
        if (validateEnteredChild(child)) {
            saveChild(child)
        }
    }

    fun validateAndSaveTransaction(transaction: TransactionDataItem, child: ChildDataItem) {
        if (validateEnteredTransaction(transaction)) {
            saveTransaction(transaction, child)
        }
    }

    fun validateAndUpdateChild(child: ChildDataItem) {
        if (validateEnteredChild(child)) {
            updateChild(child)
        }
    }

    private fun saveChild(child: ChildDataItem) {
        showChildrenLoading.value = true
        viewModelScope.launch {
            dataSource.saveChild(
                ChildTO(
                    child.name,
                    child.age,
                    child.birthday
                )
            )
            showChildrenLoading.postValue(false)
            showToast.value = app.getString(R.string.child_saved)
        }
        navigationCommand.value = NavigationCommand.Back
    }

    private fun saveTransaction(transaction: TransactionDataItem, child: ChildDataItem) {
        showTransactionsLoading.value = true
        viewModelScope.launch {
            dataSource.saveTransaction(
                TransactionTO(
                    transaction.name,
                    transaction.details,
                    transaction.amount,
                    transaction.date
                )
            )
            showTransactionsLoading.postValue(false)
            showToast.value = app.getString(R.string.transaction_saved)
        }
        sendNotification(this.app.applicationContext, child, transaction)
        navigationCommand.value = NavigationCommand.Back
    }

    private fun updateChild(child: ChildDataItem) {
        showChildrenLoading.value = true
        viewModelScope.launch {
            dataSource.updateChild(
                ChildTO(
                    child.name,
                    child.age,
                    child.birthday,
                    child.id,
                )
            )
            showChildrenLoading.postValue(false)
            showToast.value = app.getString(R.string.child_updated)
        }
        editChildDetails.value = false
    }

    private fun validateEnteredChild(child: ChildDataItem): Boolean {
        if (child.name.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_name
            return false
        }
        return true
    }

    private fun validateEnteredTransaction(transaction: TransactionDataItem): Boolean {
        if (transaction.amount == null) {
            showSnackBarInt.value = R.string.err_enter_amount
            return false
        }
        return true
    }

    fun refreshQuotes(view: ImageView) {
        viewModelScope.launch {
            addQuoteCard(view, quoteResponse.value!!.backgroundImage)
            if (quoteLoaded.value == false) {
                try {
                    val responseString = Network.quote.getQuoteAsync("en", "inspire").await()
                    val responseJSON = JSONObject(responseString)
                    _quoteResponse.value = parseQuoteJsonResult(responseJSON)
                    Timber.i("Quote of the day retrieved.")
                    dataSource.saveQuote(_quoteResponse.value!!)
                    _quoteLoaded.value = true
                } catch (e: Exception) {
                    Timber.i("Network call failed, loading saved quote")
                    Timber.i(e)
                    when (val storedQuoteResult = dataSource.getQuote()) {
                        is ResultTO.Success<*> -> {
                            _quoteResponse.value = storedQuoteResult.data as QuoteResponseTO
                        }
                    }
                }
            }
            addQuoteCard(view, quoteResponse.value!!.backgroundImage)
        }
    }

    private fun addQuoteCard(imgView: ImageView, imgUrl: String?) {
        imgUrl?.let {
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            Glide.with(imgView.context)
                .load(imgUri)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                )
                .into(imgView)
        }
    }
}