package com.hooware.allowancetracker.data.to

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Immutable model class for a Reminder. In order to compile with Room
 *
 * @param title         name of the child
 * @param description   age of the child
 * @param location      birthday of the child
 * @param entry_id      entry_id of the child
 */

@Entity(tableName = "children")
data class ChildTO(
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "age") var age: String?,
    @ColumnInfo(name = "birthday") var birthday: String?,
    @PrimaryKey @NonNull @ColumnInfo(name = "entry_id") val id: String = UUID.randomUUID()
        .toString()
)