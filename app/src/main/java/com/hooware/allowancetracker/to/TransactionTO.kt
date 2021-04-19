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
 * @param details   description of the reminder
 * @param amount      location name of the reminder
 * @param date      latitude of the reminder location
 * @param id          id of the reminder
 */

@Keep
@Parcelize
class TransactionTO(
    var details: String = "",
    var amount: String = "",
    var id: String = UUID.randomUUID().toString(),
    var date: String = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Calendar.getInstance().time),
) : Parcelable, BaseObservable()