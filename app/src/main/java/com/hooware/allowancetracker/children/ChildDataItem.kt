package com.hooware.allowancetracker.children

import java.io.Serializable
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
data class ChildDataItem(
    var name: String?,
    var age: String?,
    var birthday: String?,
    val id: String = UUID.randomUUID().toString()
) : Serializable