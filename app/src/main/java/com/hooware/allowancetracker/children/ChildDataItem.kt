package com.hooware.allowancetracker.children

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
@Parcelize
data class ChildDataItem(
    var name: String?,
    var age: String?,
    var birthday: String?,
    val id: String = UUID.randomUUID().toString()
) : Parcelable