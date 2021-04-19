package com.hooware.allowancetracker.to

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.HashMap

/**
 * data class acts as a data mapper between the DB and the UI
 */
@Keep
@Parcelize
class ChildTO(
    var name: String = "",
    var age: String = "",
    var birthday: String = "",
    var id: String = UUID.randomUUID().toString(),
    var transactions: HashMap<String, TransactionTO>? = null,
    var totalAllowance: String = "$0.0"
) : Parcelable, BaseObservable()