package com.hooware.allowancetracker.base

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.utils.SingleLiveEvent

/**
 * Base class for View Models to declare the common LiveData objects in one place
 */
abstract class BaseViewModel(application: AllowanceApp) : AndroidViewModel(application) {

    val app = application

    val navigationCommand: SingleLiveEvent<NavigationCommand> = SingleLiveEvent()
    val showErrorMessage: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBar: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()
    val showToast: SingleLiveEvent<String> = SingleLiveEvent()
    val showChildrenLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val showTransactionsLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val showNoTransactionData: MutableLiveData<Boolean> = MutableLiveData()
    val showNoChildData: MutableLiveData<Boolean> = MutableLiveData()
    val authenticationState = app.authenticationState
    val authenticationType = app.authenticationType

}