package com.hooware.allowancetracker.data.local

import com.hooware.allowancetracker.data.local.children.ChildrenDao
import com.hooware.allowancetracker.data.local.transactions.TransactionsDao
import com.hooware.allowancetracker.data.to.ChildTO
import com.hooware.allowancetracker.data.to.TransactionTO
import com.hooware.allowancetracker.data.to.ResultTO
import kotlinx.coroutines.*

/**
 * Concrete implementation of a data source as a db.
 *
 * The repository is implemented so that you can focus on only testing it.
 *
 * @param transactionsDao the dao that does the Room db operations
 * @param childrenDao the dao that does the Room db operations
 * @param ioDispatcher a coroutine dispatcher to offload the blocking IO tasks
 */
class LocalRepository(
    private val transactionsDao: TransactionsDao,
    private val childrenDao: ChildrenDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataSource {

    /**
     * Get the transactions list from the local db
     * @return Result the holds a Success with all the transactions or an Error object with the error message
     */
    override suspend fun getTransactions(): ResultTO<List<TransactionTO>> = withContext(ioDispatcher) {
        return@withContext try {
            ResultTO.Success(transactionsDao.getTransactions())
        } catch (ex: Exception) {
            ResultTO.Error(ex.localizedMessage)
        }
    }

    /**
     * Get the children list from the local db
     * @return Result the holds a Success with all the children or an Error object with the error message
     */
    override suspend fun getChildren(): ResultTO<List<ChildTO>> = withContext(ioDispatcher) {
        return@withContext try {
            ResultTO.Success(childrenDao.getChildren())
        } catch (ex: Exception) {
            ResultTO.Error(ex.localizedMessage)
        }
    }

    /**
     * Insert a transaction in the db.
     * @param transaction the transaction to be inserted
     */
    override suspend fun saveTransaction(transaction: TransactionTO) =
        withContext(ioDispatcher) {
            transactionsDao.saveTransaction(transaction)
        }

    /**
     * Insert a child in the db.
     * @param child the child to be inserted
     */
    override suspend fun saveChild(child: ChildTO) =
        withContext(ioDispatcher) {
            childrenDao.saveChild(child)
        }

    /**
     * Get a transaction by its id
     * @param id to be used to get the transaction
     * @return Result holds a Success object with the Transaction or an Error object with the error message
     */
    override suspend fun getTransaction(id: String): ResultTO<TransactionTO> = withContext(ioDispatcher) {
        try {
            val transaction = transactionsDao.getTransactionById(id)
            if (transaction != null) {
                return@withContext ResultTO.Success(transaction)
            } else {
                return@withContext ResultTO.Error("Reminder not found!")
            }
        } catch (e: Exception) {
            return@withContext ResultTO.Error(e.localizedMessage)
        }
    }

    /**
     * Get a child by its id
     * @param id to be used to get the child
     * @return Result holds a Success object with the Child or an Error object with the error message
     */
    override suspend fun getChild(id: String): ResultTO<ChildTO> = withContext(ioDispatcher) {
        try {
            val child = childrenDao.getChildById(id)
            if (child != null) {
                return@withContext ResultTO.Success(child)
            } else {
                return@withContext ResultTO.Error("Reminder not found!")
            }
        } catch (e: Exception) {
            return@withContext ResultTO.Error(e.localizedMessage)
        }
    }

    /**
     * Deletes all the transactions in the db
     */
    override suspend fun deleteAllTransactions() {
        withContext(ioDispatcher) {
            transactionsDao.deleteAllTransactions()
        }
    }

    /**
     * Deletes all the children in the db
     */
    override suspend fun deleteAllChildren() {
        withContext(ioDispatcher) {
            childrenDao.deleteAllChildren()
        }
    }
}