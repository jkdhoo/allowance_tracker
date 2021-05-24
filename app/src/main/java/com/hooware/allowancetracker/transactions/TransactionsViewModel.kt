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
import com.hooware.allowancetracker.utils.currencyFormatter


class TransactionsViewModel(application: AllowanceApp) : BaseViewModel(application) {

    private val kidsDatabase = Firebase.database.reference.child("kids").ref

    private var _transactionsLoaded = MutableLiveData<Boolean>()
    val transactionsLoaded: LiveData<Boolean>
        get() = _transactionsLoaded

    private var _transactionsList = MutableLiveData<MutableList<TransactionTO>>()
    val transactionsList: LiveData<MutableList<TransactionTO>>
        get() = _transactionsList

    private var _showTransactionsEmpty = MutableLiveData<Boolean>()
    val showTransactionsEmpty: LiveData<Boolean>
        get() = _showTransactionsEmpty

    private var _savingsOwedUpdated = MutableLiveData<Boolean>()
    val savingsOwedUpdated: LiveData<Boolean>
        get() =_savingsOwedUpdated

    private var _totalSpendingUpdated = MutableLiveData<Boolean>()
    val totalSpendingUpdated: LiveData<Boolean>
        get() = _totalSpendingUpdated

    private var _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean>
        get() = _showLoading

    var firebaseUID = MutableLiveData<String>()

    var child = MutableLiveData<ChildTO>()

    var disableSavings = MutableLiveData<Boolean>()

    var saveTransactionTotalHolder = MutableLiveData<String>()

    init {
        _transactionsLoaded.value = false
        _savingsOwedUpdated.value = false
        _transactionsList.value = mutableListOf()
        _showTransactionsEmpty.value = false
    }

    fun loadTransactions() {
        _showLoading.value = true
        _transactionsList.value = child.value?.transactions?.values?.sortedByDescending { it.date }?.toMutableList() ?: mutableListOf()
        _showTransactionsEmpty.value = _transactionsList.value.isNullOrEmpty()
        _transactionsLoaded.value = true
        _savingsOwedUpdated.value = true
        _showLoading.value = false
    }

    private fun validateEnteredTransaction(transaction: TransactionTO): TransactionTO? {
        try {
            transaction.total = saveTransactionTotalHolder.value?.toDouble() ?: return null
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
        if (disableSavings.value == true) {
            validateAndSaveTransactionNoSavings(transaction)
            return
        }
        _showLoading.value = true
        val childTO = child.value ?: return
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
        } else {
            adjustedTransactionTO.spending = adjustedTransactionTO.total
        }
        childTO.transactions?.put(adjustedTransactionTO.id, adjustedTransactionTO)
        kidsDatabase.child(childTO.id).child("transactions").child(adjustedTransactionTO.id).setValue(adjustedTransactionTO)
            .addOnSuccessListener {
                if (adjustedTransactionTO.total > 0) {
                    childTO.totalSpending = childTO.totalSpending.plus(adjustedTransactionTO.spending)
                    kidsDatabase.child(childTO.id).child("totalSpending").setValue(childTO.totalSpending)
                    childTO.savingsOwed = childTO.savingsOwed.plus(adjustedTransactionTO.savings)
                    kidsDatabase.child(childTO.id).child("savingsOwed").setValue(childTO.savingsOwed)
                } else {
                    childTO.totalSpending = childTO.totalSpending.plus(adjustedTransactionTO.spending)
                    kidsDatabase.child(childTO.id).child("totalSpending").setValue(childTO.totalSpending)
                }
                child.value = childTO
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_saved
                navigationCommand.value = NavigationCommand.Back
            }
            .addOnFailureListener {
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_save_error
            }
    }

    private fun validateAndSaveTransactionNoSavings(transaction: TransactionTO) {
        _showLoading.value = true
        val childTO = child.value ?: return
        val adjustedTransactionTO = validateEnteredTransaction(transaction) ?: run {
            _showLoading.value = false
            return
        }
        if (adjustedTransactionTO.total > 0) {
            adjustedTransactionTO.spending = (adjustedTransactionTO.total).currencyFormatter() ?: run {
                showSnackBarInt.value = R.string.err_invalid_format_amount
                return
            }
            adjustedTransactionTO.savings = (adjustedTransactionTO.total * 0).currencyFormatter() ?: run {
                showSnackBarInt.value = R.string.err_invalid_format_amount
                return
            }
        } else {
            adjustedTransactionTO.spending = adjustedTransactionTO.total
        }
        childTO.transactions?.put(adjustedTransactionTO.id, adjustedTransactionTO)
        kidsDatabase.child(childTO.id).child("transactions").child(adjustedTransactionTO.id).setValue(adjustedTransactionTO)
            .addOnSuccessListener {
                if (adjustedTransactionTO.total > 0) {
                    childTO.totalSpending = childTO.totalSpending.plus(adjustedTransactionTO.spending)
                    kidsDatabase.child(childTO.id).child("totalSpending").setValue(childTO.totalSpending)
                } else {
                    childTO.totalSpending = childTO.totalSpending.plus(adjustedTransactionTO.spending)
                    kidsDatabase.child(childTO.id).child("totalSpending").setValue(childTO.totalSpending)
                }
                child.value = childTO
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
        val childTO = child.value ?: run {
            _showLoading.value = false
            showSnackBarInt.value = R.string.transaction_delete_error
            return
        }
        childTO.transactions?.remove(transaction.id) ?: run {
            _showLoading.value = false
            showSnackBarInt.value = R.string.transaction_delete_error
            return
        }
        kidsDatabase.child(childTO.id).child("transactions").child(transaction.id).removeValue()
            .addOnSuccessListener {
                if (transaction.total > 0) {
                    childTO.savingsOwed = childTO.savingsOwed.minus(transaction.savings)
                    kidsDatabase.child(childTO.id).child("savingsOwed").setValue(childTO.savingsOwed)
                        .addOnSuccessListener {
                            _savingsOwedUpdated.value = true
                        }
                    childTO.totalSpending = childTO.totalSpending.minus(transaction.spending)
                    kidsDatabase.child(childTO.id).child("totalSpending").setValue(childTO.totalSpending)
                        .addOnSuccessListener {
                            _totalSpendingUpdated.value = true
                        }
                } else {
                    childTO.totalSpending = childTO.totalSpending.minus(transaction.spending)
                    kidsDatabase.child(childTO.id).child("totalSpending").setValue(childTO.totalSpending)
                        .addOnSuccessListener {
                            _totalSpendingUpdated.value = true
                        }
                }
                child.value = childTO
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_deleted
                navigationCommand.value = NavigationCommand.Back
            }
            .addOnFailureListener {
                _showLoading.value = false
                showSnackBarInt.value = R.string.transaction_delete_error
            }
    }

    fun setFirebaseUID(userId: String) {
        firebaseUID.value = userId
    }

    fun resetSavingsOwed() {
        _showLoading.value = true
        val childTO = child.value ?: return
        childTO.savingsOwed = 0.0
        kidsDatabase.child(childTO.id).child("savingsOwed").setValue(childTO.savingsOwed)
            .addOnSuccessListener {
                child.value = childTO
                _savingsOwedUpdated.value = true
                _showLoading.value = false
                showSnackBarInt.value = R.string.savings_reset
            }
            .addOnFailureListener {
                _showLoading.value = false
                showSnackBarInt.value = R.string.savings_reset_error
            }
    }

    fun resetSavingsOwedUpdated() {
        _savingsOwedUpdated.value = false
    }

    fun resetTotalSpendingUpdated() {
        _totalSpendingUpdated.value = false
    }

    fun resetTransactionsLoaded() {
        _transactionsLoaded.value = false
    }

//    private fun sendNewTransactionNotification() {
//        val topic = "1" //topic must match with what the receiver subscribed to
//
//        val notificationTitle = "Test"
//        val notificationMessage = "Test"
//
//        val notification = JSONObject()
//        val notificationBody = JSONObject()
//        try {
//            notificationBody.put("title", notificationTitle)
//            notificationBody.put("message", notificationMessage)
//            notification.put("to", topic)
//            notification.put("data", notificationBody)
//        } catch (e: JSONException) {
//            Timber.i("onCreate: ${e.message}")
//        }
//        sendNotification(notification)
//    }

//    private fun sendNotification(notification: JSONObject) {
//        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
//            object : Listener<JSONObject?>() {
//                fun onResponse(response: JSONObject) {
//                    Log.i(TAG, "onResponse: $response")
//                    edtTitle.setText("")
//                    edtMessage.setText("")
//                }
//            },
//            object : ErrorListener() {
//                fun onErrorResponse(error: VolleyError?) {
//                    Toast.makeText(this@MainActivity, "Request error", Toast.LENGTH_LONG).show()
//                    Log.i(TAG, "onErrorResponse: Didn't work")
//                }
//            }) {
//            @get:Throws(AuthFailureError::class)
//            val headers: Map<String, String>?
//                get() {
//                    val params: MutableMap<String, String> = HashMap()
//                    params["Authorization"] = serverKey
//                    params["Content-Type"] = CMSAttributes.contentType
//                    return params
//                }
//        }
//        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest)
//    }
}