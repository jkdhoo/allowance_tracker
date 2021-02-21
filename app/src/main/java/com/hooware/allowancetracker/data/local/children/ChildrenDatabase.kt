package com.hooware.allowancetracker.data.local.children

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hooware.allowancetracker.data.to.ChildTO

/**
 * The Room Database that contains the transactions table.
 */
@Database(entities = [ChildTO::class], version = 1, exportSchema = false)
abstract class ChildrenDatabase : RoomDatabase() {

    abstract fun childrenDao(): ChildrenDao
}