package com.hooware.allowancetracker.data.local

import android.content.Context
import androidx.room.Room
import com.hooware.allowancetracker.data.local.children.ChildrenDao
import com.hooware.allowancetracker.data.local.children.ChildrenDatabase
import com.hooware.allowancetracker.data.local.quotes.QuoteDao
import com.hooware.allowancetracker.data.local.quotes.QuoteDatabase
import com.hooware.allowancetracker.data.local.transactions.TransactionsDao
import com.hooware.allowancetracker.data.local.transactions.TransactionsDatabase


/**
 * Singleton class that is used to create a reminder db
 */
object LocalDB {

    /**
     * static method that creates a reminder class and returns the DAO of the reminder
     */
    fun createTransactionsDao(context: Context): TransactionsDao {
        return Room.databaseBuilder(
            context.applicationContext,
            TransactionsDatabase::class.java, "transactions.db"
        ).build().transactionsDao()
    }

    /**
     *  static method that creates a child class and returns the DAO of the child
     */
    fun createChildrenDao(context: Context): ChildrenDao {
        return Room.databaseBuilder(
            context.applicationContext,
            ChildrenDatabase::class.java, "children.db"
        ).build().childrenDao()
    }

    /**
     *  static method that creates a child class and returns the DAO of the child
     */
    fun createQuoteDao(context: Context): QuoteDao {
        return Room.databaseBuilder(
            context.applicationContext,
            QuoteDatabase::class.java, "quote.db"
        ).build().quoteDao()
    }

}