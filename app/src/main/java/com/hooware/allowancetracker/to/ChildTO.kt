package com.hooware.allowancetracker.to

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
@Keep
@Parcelize
class ChildTO(
    var name: String = "",
    var birthday: String = "",
    var id: String = UUID.randomUUID().toString(),
    var savingsOwed: Double = 0.0,
    var age: String = "0",
    var transactions: HashMap<String, TransactionTO>? = null,
    var totalSpending: Double = 0.0
) : Parcelable, BaseObservable()