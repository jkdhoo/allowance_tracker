package com.hooware.allowancetracker.transactions

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
@Parcelize
data class TransactionDataItem(
    var name: String?,
    var details: String?,
    var amount: String?,
    var date: String?,
    val id: String = UUID.randomUUID().toString()
) : Parcelable