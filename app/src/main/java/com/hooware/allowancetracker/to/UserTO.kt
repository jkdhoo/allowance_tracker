package com.hooware.allowancetracker.to

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize

/**
 * data class acts as a data mapper between the DB and the UI
 */
@Keep
@Parcelize
data class UserTO(
    var firstName: String = "",
    var middleName: String = "",
    var firebaseId: String = "",
    var colorPreference: Int
) : Parcelable, BaseObservable()