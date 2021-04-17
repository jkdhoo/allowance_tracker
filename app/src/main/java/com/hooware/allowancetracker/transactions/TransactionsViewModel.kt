package com.hooware.allowancetracker.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseViewModel
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.to.ChildTO
import com.hooware.allowancetracker.to.TransactionTO
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.NumberFormat

class TransactionsViewModel(application: AllowanceApp) : BaseViewModel(application) {

    private val transactionsDatabase = Firebase.database.reference.child("transactions").ref

    private var _transactionsLoaded = MutableLiveData<Boolean>()
    val transactionsLoaded: LiveData<Boolean>
        get() = _transactionsLoaded

    private var _transactionsList = MutableLiveData<List<TransactionTO>>()
    val transactionsList: LiveData<List<TransactionTO>>
        get() = _transactionsList

    private var _transactionsEmpty = MutableLiveData<Boolean>()
    val transactionsEmpty: LiveData<Boolean>
        get() = _transactionsEmpty

    private var _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean>
        get() = _showLoading

    var firebaseUID = MutableLiveData<String>()

    var child = MutableLiveData<ChildTO>()

    init {
        _transactionsLoaded.value = false
        transactionsDatabase.keepSynced(true)
    }

    fun loadTransactions(id: String) {
        _showLoading.value = true
        viewModelScope.launch {
            if (transactionsLoaded.value == false) {
                transactionsDatabase.get()
                    .addOnSuccessListener {
                        _transactionsList.value = it.getValue<HashMap<String, TransactionTO>>()?.values?.toList()?.filter { transactionTO ->
                            transactionTO.name == id
                        } ?: listOf()
                        _transactionsLoaded.value = true
                        _transactionsEmpty.value = _transactionsList.value.isNullOrEmpty()
                        _showLoading.value = false
                    }
                    .addOnFailureListener {
                        _transactionsList.value = listOf()
                        _transactionsLoaded.value = true
                        _transactionsEmpty.value = true
                        _showLoading.value = false
                    }
            }
        }
    }

    private fun validateEnteredTransaction(transaction: TransactionTO?, child: ChildTO): TransactionTO? {
        if (transaction?.amount == null) {
            showSnackBarInt.value = R.string.err_enter_amount
            return null
        }
        return try {
            val format = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 2
            transaction.amount = format.format(transaction.amount.toDouble()).toString()
            transaction.name = child.id
            transaction
        } catch (ex: Exception) {
            Timber.i(ex)
            showSnackBarInt.value = R.string.err_invalid_format_amount
            null
        }
    }

    fun validateAndSaveTransaction(transaction: TransactionTO?, child: ChildTO) {
        _showLoading.value = true
        Timber.i("${transaction?.details}")
        val adjustedTransactionTO = validateEnteredTransaction(transaction, child) ?: run {
            _showLoading.value = false
            return
        }
        transactionsDatabase.child(adjustedTransactionTO.id).setValue(adjustedTransactionTO)
            .addOnSuccessListener {
                _showLoading.value = false
                navigationCommand.value = NavigationCommand.Back
                showSnackBarInt.value = R.string.transaction_saved
            }
            .addOnFailureListener {
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_save_error
            }
    }

    fun deleteTransaction(transactionID: String) {
        _showLoading.value = true
        transactionsDatabase.child(transactionID).removeValue()
            .addOnSuccessListener {
                _showLoading.value = false
                navigationCommand.value = NavigationCommand.Back
                showSnackBarInt.value = R.string.transaction_deleted
            }
    }

    fun resetTransactions() {
        _transactionsList.value = listOf()
        _transactionsLoaded.value = false
    }
}