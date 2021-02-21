package com.hooware.allowancetracker.overview

import java.io.Serializable
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
data class TransactionDataItem(
    var name: String?,
    var details: String?,
    var amount: Double?,
    var date: String?,
    val id: String = UUID.randomUUID().toString()
) : Serializable