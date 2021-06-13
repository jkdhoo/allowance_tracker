package com.hooware.allowancetracker.to

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import kotlinx.parcelize.Parcelize

/**
 * data class acts as a data holder for the application session
 */
@Keep
@Parcelize
data class FCMRequestInputTO(
    var body: String = "",
    var title: String = ""
) : Parcelable, BaseObservable()