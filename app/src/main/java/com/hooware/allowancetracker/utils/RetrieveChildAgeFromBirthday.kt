package com.hooware.allowancetracker.utils

import com.hooware.allowancetracker.AllowanceApp
import com.hooware.allowancetracker.R
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object RetrieveChildAgeFromBirthday {
    fun execute(application: AllowanceApp, birthday: String): String {
        return try {
            val format = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
            val date = LocalDate.parse(birthday, format)
            Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()).age
        } catch (ex: Exception) {
            Timber.i("Exception trying to retrieve child age from birthday")
            application.getString(R.string.not_available)
        }
    }
}