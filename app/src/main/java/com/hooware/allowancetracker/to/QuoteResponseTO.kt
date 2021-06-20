package com.hooware.allowancetracker.to

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class QuoteResponseTO(var quote: String = "", var author: String = ""): Parcelable