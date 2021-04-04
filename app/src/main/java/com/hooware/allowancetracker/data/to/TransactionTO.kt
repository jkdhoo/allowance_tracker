package com.hooware.allowancetracker.data.to

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Immutable model class for a Reminder. In order to compile with Room
 *
 * @param name         title of the reminder
 * @param details   description of the reminder
 * @param amount      location name of the reminder
 * @param date      latitude of the reminder location
 * @param id          id of the reminder
 */

@Entity(tableName = "transactions")
data class TransactionTO(
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "details") var details: String?,
    @ColumnInfo(name = "amount") var amount: String?,
    @ColumnInfo(name = "date") val date: String?,
    @PrimaryKey @ColumnInfo(name = "entry_id") val id: String = UUID.randomUUID().toString()
)
