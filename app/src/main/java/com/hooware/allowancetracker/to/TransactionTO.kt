package com.hooware.allowancetracker.to

import android.os.Parcelable
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

/**
 * Immutable model class for a Reminder. In order to compile with Room
 *
 * @param _name         title of the reminder
 * @param _details   description of the reminder
 * @param _amount      location name of the reminder
 * @param _date      latitude of the reminder location
 * @param _id          id of the reminder
 */

@Parcelize
class TransactionTO(
    var details: String = "",
    var amount: String = "",
    var id: String = UUID.randomUUID().toString(),
    var date: String = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Calendar.getInstance().time),
) : Parcelable, BaseObservable()