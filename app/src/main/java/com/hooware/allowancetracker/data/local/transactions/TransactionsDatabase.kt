package com.hooware.allowancetracker.data.local.transactions

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hooware.allowancetracker.data.to.TransactionTO

/**
 * The Room Database that contains the transactions table.
 */
@Database(entities = [TransactionTO::class], version = 1, exportSchema = false)
abstract class TransactionsDatabase : RoomDatabase() {

    abstract fun transactionsDao(): TransactionsDao
}