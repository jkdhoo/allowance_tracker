package com.hooware.allowancetracker.to

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

/**
 * Immutable model class for a Reminder. In order to compile with Room
 *
 * @param details   details of the transaction
 * @param spending  amount available to spend
 * @param total     total amount of transaction
 * @param savings   amount set aside for savings (25%)
 * @param date      date of transaction in milliseconds
 * @param id        id of transaction
 * @param timestamp timestamp of transaction
 */
@Keep
@Parcelize
class TransactionTO(
    var details: String = "",
    var spending: Double = 0.0,
    var savings: Double = 0.0,
    var total: Double = 0.0,
    var id: String = UUID.randomUUID().toString(),
    var date: Long = System.currentTimeMillis(),
    var timestamp: String = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
) : Parcelable, BaseObservable()