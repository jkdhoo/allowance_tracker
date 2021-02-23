package com.hooware.allowancetracker.data.local

import com.hooware.allowancetracker.data.to.ChildTO
import com.hooware.allowancetracker.data.to.TransactionTO
import com.hooware.allowancetracker.data.to.ResultTO

/**
 * Main entry point for accessing transactions data.
 */
interface DataSource {
    //TransactionsDao
    suspend fun getTransactions(): ResultTO<List<TransactionTO>>
    suspend fun getTransactionsByChild(id: String): ResultTO<List<TransactionTO>>
    suspend fun saveTransaction(transaction: TransactionTO)
    suspend fun getTransaction(id: String): ResultTO<TransactionTO>
    suspend fun deleteAllTransactions()

    //ChildrenDao
    suspend fun getChildren(): ResultTO<List<ChildTO>>
    suspend fun saveChild(child: ChildTO)
    suspend fun updateChild(child: ChildTO)
    suspend fun getChild(id: String): ResultTO<ChildTO>
    suspend fun deleteAllChildren()
}