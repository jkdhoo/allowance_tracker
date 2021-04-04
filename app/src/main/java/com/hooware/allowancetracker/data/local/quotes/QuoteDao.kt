package com.hooware.allowancetracker.data.local.quotes

import androidx.room.*
import com.hooware.allowancetracker.network.QuoteResponseTO

/**
 * Data Access Object for the children table.
 */
@Dao
interface QuoteDao {
    /**
     * @return all quotes.
     */
    @Query("SELECT * FROM quote")
    suspend fun getQuote(): QuoteResponseTO

    /**
     * Insert a quote in the database. If the quote already exists, replace.
     *
     * @param quote the quote to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuote(quote: QuoteResponseTO)

    /**
     * Delete quote.
     */
    @Query("DELETE FROM quote")
    suspend fun deleteQuote()

}