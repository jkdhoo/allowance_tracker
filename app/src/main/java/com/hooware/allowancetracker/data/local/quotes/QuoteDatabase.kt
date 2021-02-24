package com.hooware.allowancetracker.data.local.quotes

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hooware.allowancetracker.network.QuoteResponseTO

/**
 * The Room Database that contains the transactions table.
 */
@Database(entities = [QuoteResponseTO::class], version = 1, exportSchema = false)
abstract class QuoteDatabase : RoomDatabase() {

    abstract fun quoteDao(): QuoteDao
}