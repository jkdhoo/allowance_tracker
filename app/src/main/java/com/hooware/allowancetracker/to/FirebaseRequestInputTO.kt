package com.hooware.allowancetracker.to

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
class FirebaseRequestInputTO(var to: String, var title: String, var body: String) : Parcelable