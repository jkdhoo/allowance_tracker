package com.hooware.allowancetracker.to

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class NotificationItemTO(
    var from: String? = "",
    var to: String? = "",
    var title: String? = "",
    var body: String? = "",
    var time: String? = ""
): Parcelable