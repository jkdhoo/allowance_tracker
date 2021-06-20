package com.hooware.allowancetracker.utils

import androidx.preference.PreferenceManager
import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object HasAlreadyLoadedQuote {

    private const val LAST_QUOTE_LOADED = "Last Quote Loaded"
    private const val TWO_HOURS_IN_MILLI = 7200000L

    fun execute(application: AllowanceApp): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(application)
        val editor = prefs.edit()
        val lastQuoteLoadTime = prefs.getLong(LAST_QUOTE_LOADED, 0L)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastQuoteLoadTime < TWO_HOURS_IN_MILLI) {
            Timber.i("Quote already loaded, skipping")
            return true
        }
        editor.putLong(LAST_QUOTE_LOADED, currentTime)
        editor.apply()
        return false
    }
}