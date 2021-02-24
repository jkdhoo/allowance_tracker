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
     * Insert a child in the database. If the child already exists, abort.
     *
     * @param child the child to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuote(quote: QuoteResponseTO)

    /**
     * Delete quote.
     */
    @Query("DELETE FROM quote")
    suspend fun deleteQuote()

}