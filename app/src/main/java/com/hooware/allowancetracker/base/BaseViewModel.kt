package com.hooware.allowancetracker.base

import androidx.lifecycle.AndroidViewModel
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.utils.SingleLiveEvent

/**
 * Base class for View Models to declare the common LiveData objects in one place
 */
abstract class BaseViewModel(application: AllowanceApp) : AndroidViewModel(application) {

//    val app = application

    val navigationCommand: SingleLiveEvent<NavigationCommand> = SingleLiveEvent()
    val showErrorMessage: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBar: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()
    val showToast: SingleLiveEvent<String> = SingleLiveEvent()

//    val database: LiveData<DatabaseReference>

//    val showChildrenLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
//    val showTransactionsLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
//    val showNoChildData: MutableLiveData<Boolean> = MutableLiveData()
//    val databaseReady = database.databaseReady
//    val quoteAuthor = database.quoteAuthor
//    val quoteQuote = database.quoteQuote
//    val quoteBackgroundImage = database.quoteBackgroundImage
//    val kidsList = database.kidsList
//    val transactionList = database.transactionList
//    val showNoTransactionData = database.showNoTransactionData

//    private var _finalKidsList = MutableLiveData<List<ChildTO>>()
//    val finalKidsList: LiveData<List<ChildTO>>
//        get() = _finalKidsList

//    private var _authType = MutableLiveData<AuthType>()
//    val authType: LiveData<AuthType>
//        get() = _authType
//
//    fun setAuthType(user: FirebaseUser) {
//        val parent = firebaseConfigRetriever("parent_uid")
//        val levi = firebaseConfigRetriever("levi_uid")
//        val laa = firebaseConfigRetriever("laa_uid")
//        when {
//            parent.contains(user.uid) -> _authType.value = AuthType.PARENT
//            levi.contains(user.uid) -> _authType.value = AuthType.LEVI
//            laa.contains(user.uid) -> _authType.value = AuthType.LAA
//        }
//        Timber.i("Auth Type: ${authType.value}")
//    }
//
//    fun firebaseConfigRetriever(param: String): String {
//        val remoteConfig = FirebaseRemoteConfig.getInstance()
//        return remoteConfig.getString(param)
//    }

//    fun createKidsList(authType: AuthType) {
//        Timber.i("Filtering by: $authType")
//        when (authType) {
//            AuthType.PARENT -> {
//                Timber.i("Filtering by $authType")
//                _finalKidsList.value = kidsList.value
//            }
//            AuthType.LEVI -> {
//                Timber.i("Filtering by $authType")
//                _finalKidsList.value = kidsList.value?.filter {
//                    it.name == "Levi"
//                }
//            }
//            AuthType.LAA -> {
//                Timber.i("Filtering by $authType")
//                _finalKidsList.value = kidsList.value?.filter {
//                    it.name == "Laavingonn"
//                }
//            }
//        }
//    }
}