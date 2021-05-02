package com.hooware.allowancetracker.transactions

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import com.hooware.allowancetracker.base.BaseViewModel
import com.hooware.allowancetracker.base.NavigationCommand
import com.hooware.allowancetracker.to.ChildTO
import com.hooware.allowancetracker.to.TransactionTO
import com.hooware.allowancetracker.utils.currencyFormatter
import timber.log.Timber
import java.text.DecimalFormat

class TransactionsViewModel(application: AllowanceApp) : BaseViewModel(application) {

    private val kidsDatabase = Firebase.database.reference.child("kids").ref

    private var _transactionsLoaded = MutableLiveData<Boolean>()
    val transactionsLoaded: LiveData<Boolean>
        get() = _transactionsLoaded

    private var _transactionsList = MutableLiveData<MutableList<TransactionTO>>()
    val transactionsList: LiveData<MutableList<TransactionTO>>
        get() = _transactionsList

    private var _transactionsEmpty = MutableLiveData<Boolean>()
    val transactionsEmpty: LiveData<Boolean>
        get() = _transactionsEmpty

    private var _savingsOwedUpdated = MutableLiveData<Boolean>()
    val savingsOwedUpdated: LiveData<Boolean>
        get() =_savingsOwedUpdated

    private var _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean>
        get() = _showLoading

    var firebaseUID = MutableLiveData<String>()

    var child = MutableLiveData<ChildTO>()

    init {
        _transactionsLoaded.value = false
        _savingsOwedUpdated.value = true
        _transactionsList.value = mutableListOf()
        _transactionsEmpty.value = true
        _showLoading.value = true
        kidsDatabase.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Timber.i("onChildAdded: ${dataSnapshot.key!!}")
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Timber.i("onChildChanged: ${dataSnapshot.key}")
//                Timber.i("Previous Child: $previousChildName")
//                if (child.value?.id == dataSnapshot.key) {
//                    child.value = dataSnapshot.getValue(ChildTO::class.java)
//                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Timber.i("onChildRemoved: ${dataSnapshot.key!!}")
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Timber.i("onChildMoved: ${dataSnapshot.key!!}")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.i("postComments:onCancelled ${databaseError.toException()}")
            }
        })
    }

    fun loadTransactions() {
        _showLoading.value = true
        _transactionsLoaded.value = false
        _transactionsList.value = child.value?.transactions?.values?.sortedByDescending { it.date }?.toMutableList() ?: mutableListOf()
        _transactionsEmpty.value = _transactionsList.value.isNullOrEmpty()
        _transactionsLoaded.value = true
        _showLoading.value = false
    }

    private fun validateEnteredTransaction(transaction: TransactionTO): TransactionTO? {
        try {
            transaction.total = transaction.total.currencyFormatter() ?: run {
                showSnackBarInt.value = R.string.err_invalid_format_amount
                return null
            }
        } catch (ex: Exception) {
            showSnackBarInt.value = R.string.err_invalid_format_amount
            return null
        }
        if (transaction.total == 0.0) {
            showSnackBarInt.value = R.string.err_enter_amount
            return null
        }
        return transaction
    }

    fun validateAndSaveTransaction(transaction: TransactionTO) {
        _showLoading.value = true
        val childId = child.value?.id ?: return
        val adjustedTransactionTO = validateEnteredTransaction(transaction) ?: run {
            _showLoading.value = false
            return
        }
        if (adjustedTransactionTO.total > 0) {
            adjustedTransactionTO.spending = (adjustedTransactionTO.total * .8).currencyFormatter() ?: run {
                showSnackBarInt.value = R.string.err_invalid_format_amount
                return
            }
            adjustedTransactionTO.savings = (adjustedTransactionTO.total * .2).currencyFormatter() ?: run {
                showSnackBarInt.value = R.string.err_invalid_format_amount
                return
            }
            kidsDatabase.child(childId).child("savingsOwed").setValue(child.value?.savingsOwed)
        }
        kidsDatabase.child(childId).child("transactions").child(adjustedTransactionTO.id).setValue(adjustedTransactionTO)
            .addOnSuccessListener {
                child.value?.transactions?.put(adjustedTransactionTO.id, adjustedTransactionTO)
                if (adjustedTransactionTO.total > 0) {
                    child.value?.totalSpending = child.value?.totalSpending?.plus(adjustedTransactionTO.spending) ?: 0.0
                    kidsDatabase.child(childId).child("totalSpending").setValue(child.value?.totalSpending)
                    child.value?.savingsOwed = child.value?.savingsOwed?.plus(adjustedTransactionTO.savings) ?: 0.0
                    kidsDatabase.child(childId).child("savingsOwed").setValue(child.value?.savingsOwed)
                }
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_saved
                navigationCommand.value = NavigationCommand.Back
            }
            .addOnFailureListener {
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_save_error
            }
    }

    fun deleteTransaction(transaction: TransactionTO) {
        _showLoading.value = true
        val childId = child.value?.id ?: return
        kidsDatabase.child(childId).child("transactions").child(transaction.id).removeValue()
            .addOnSuccessListener {
                child.value?.transactions?.remove(transaction.id)
                if (transaction.total > 0) {
                    child.value?.savingsOwed = child.value?.savingsOwed?.minus(transaction.savings) ?: 0.0
                    kidsDatabase.child(childId).child("savingsOwed").setValue(child.value?.savingsOwed)
                    child.value?.totalSpending = child.value?.totalSpending?.minus(transaction.spending) ?: 0.0
                    kidsDatabase.child(childId).child("totalSpending").setValue(child.value?.totalSpending)
                }
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_deleted
                navigationCommand.value = NavigationCommand.Back
            }
            .addOnFailureListener {
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_delete_error
            }
    }

    fun resetTransactions() {
        _transactionsList.value = mutableListOf()
        _transactionsLoaded.value = false
    }

    fun setFirebaseUID(userId: String) {
        firebaseUID.value = userId
    }

    fun resetSavingsOwed() {
        _showLoading.value = true
        val childId = child.value?.id ?: return
        child.value?.savingsOwed = 0.0
        kidsDatabase.child(childId).child("savingsOwed").setValue(child.value?.savingsOwed)
            .addOnSuccessListener {
                _showLoading.value = false
                _savingsOwedUpdated.value = true
                showSnackBarInt.value = R.string.savings_reset
            }
            .addOnFailureListener {
                _showLoading.value = false
                showSnackBarInt.value = R.string.savings_reset_error
            }
    }

    fun setCurrency(newValue: Double): String {
        val format = DecimalFormat("#,##0.00")
        return format.format(newValue)
    }

    fun resetSavingsOwedUpdated() {
        _savingsOwedUpdated.value = false
    }

    fun reset() {
        _transactionsLoaded.value = false
        _savingsOwedUpdated.value = true
        _transactionsList.value = mutableListOf()
        _transactionsEmpty.value = true
        _showLoading.value = true
    }
}