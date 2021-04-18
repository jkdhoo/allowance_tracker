package com.hooware.allowancetracker.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseViewModel
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.to.ChildTO
import com.hooware.allowancetracker.to.TransactionTO
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

class TransactionsViewModel(application: AllowanceApp) : BaseViewModel(application) {

    private val kidsDatabase = Firebase.database.reference.child("kids").ref

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

    fun loadTransactions(child: ChildTO) {
        _showLoading.value = true
        _transactionsList.value = child.transactions?.values?.toList() ?: mutableListOf()
        _transactionsEmpty.value = _transactionsList.value.isNullOrEmpty()
        _transactionsLoaded.value = true
        _showLoading.value = false
    }

    private fun validateEnteredTransaction(transaction: TransactionTO): TransactionTO? {
        val amount = transaction.amount

        return try {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            format.maximumFractionDigits = 2
            transaction.amount = format.format(amount.toDouble()).toString()
            transaction
        } catch (ex: Exception) {
            Timber.i(ex)
            showSnackBarInt.value = R.string.err_invalid_format_amount
            null
        }
    }

    fun validateAndSaveTransaction(transaction: TransactionTO, child: ChildTO) {
        _showLoading.value = true
        Timber.i(transaction.details)
        val adjustedTransactionTO = validateEnteredTransaction(transaction) ?: run {
            _showLoading.value = false
            return
        }
        kidsDatabase.child(child.id).child("transactions").child(adjustedTransactionTO.id).setValue(adjustedTransactionTO)
            .addOnSuccessListener {
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_saved
                navigationCommand.value = NavigationCommand.Back
            }
            .addOnFailureListener {
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_save_error
            }
    }

    fun deleteTransaction(child: ChildTO, transaction: TransactionTO) {
        _showLoading.value = true
        kidsDatabase.child(child.id).child("transactions").child(transaction.id).removeValue()
            .addOnSuccessListener {
                _showLoading.value = false
                navigationCommand.value = NavigationCommand.Back
                showSnackBarInt.value = R.string.transaction_deleted
            }
            .addOnFailureListener {
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_delete_error
            }
    }

    fun resetTransactions() {
        _transactionsList.value = listOf()
        _transactionsLoaded.value = false
    }

    fun setFirebaseUID(userId: String) {
        firebaseUID.value = userId
    }
}