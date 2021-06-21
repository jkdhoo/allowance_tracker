package com.hooware.allowancetracker.to

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatTO(val message: String, val time: String, val name: String) : Parcelable