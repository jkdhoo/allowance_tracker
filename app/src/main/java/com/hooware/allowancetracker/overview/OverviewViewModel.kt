package com.hooware.allowancetracker.overview

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.auth.FirebaseUserLiveData
import com.hooware.allowancetracker.base.BaseViewModel
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.children.ChildDataItem
import com.hooware.allowancetracker.data.local.DataSource
import com.hooware.allowancetracker.data.to.ChildTO
import com.hooware.allowancetracker.data.to.ResultTO
import com.hooware.allowancetracker.data.to.TransactionTO
import kotlinx.coroutines.launch
import timber.log.Timber

class OverviewViewModel(val app: Application, private val dataSource: DataSource) :
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

    // lists that hold the data to be displayed on the UI
    val transactionsList = MutableLiveData<List<TransactionDataItem>>()
    val childrenList = MutableLiveData<List<ChildDataItem>>()

    fun clearAllTransactions() {
        showTransactionsLoading.value = true
        viewModelScope.launch {
            dataSource.deleteAllTransactions()
            showTransactionsLoading.value = false
            showToast.value = getApplication<Application>().getString(R.string.transactions_cleared)
        }
        loadTransactions()
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
    fun loadTransactions() {
        showTransactionsLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getTransactions()
            showTransactionsLoading.postValue(false)
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
        showNoTransactionData.value = transactionsList.value == null || transactionsList.value!!.isEmpty()
    }

    private fun invalidateShowNoChildData() {
        showNoChildData.value = childrenList.value == null || childrenList.value!!.isEmpty()
    }

    fun validateAndSaveReminder(child: ChildDataItem) {
        if (validateEnteredChild(child)) {
            saveChild(child)
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

    private fun validateEnteredChild(child: ChildDataItem): Boolean {
        if (child.name.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_name
            return false
        }
        return true
    }
}