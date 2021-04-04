package com.hooware.allowancetracker.data.local.transactions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hooware.allowancetracker.data.to.TransactionTO

/**
 * Data Access Object for the transactions table.
 */
@Dao
interface TransactionsDao {
    /**
     * @return all transactions.
     */
    @Query("SELECT * FROM transactions")
    suspend fun getTransactions(): List<TransactionTO>

    /**
     * @param transactionId the id of the transaction
     * @return the transaction object with the transactionId
     */
    @Query("SELECT * FROM transactions where entry_id = :transactionId")
    suspend fun getTransactionById(transactionId: String): TransactionTO?

    /**
     * @param childId the childId of the child in the transaction
     * @return the transaction object with the transactionId
     */
    @Query("SELECT * FROM transactions where name = :childId")
    suspend fun getTransactionsByChild(childId: String): List<TransactionTO>

    /**
     * Insert a transaction in the database. If the transaction already exists, replace it.
     *
     * @param transaction the transaction to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTransaction(transaction: TransactionTO)

    /**
     * Delete all transactions.
     */
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    /**
     * Delete all transactions by child.
     */
    @Query("DELETE FROM transactions where name = :name")
    suspend fun deleteAllTransactionsByChild(name: String)

}