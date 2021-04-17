package com.hooware.allowancetracker.to

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
@Parcelize
class ChildTO(
    var name: String? = "",
    var age: String? = "",
    var birthday: String? = "",
    val id: String = UUID.randomUUID().toString()
) : Parcelable