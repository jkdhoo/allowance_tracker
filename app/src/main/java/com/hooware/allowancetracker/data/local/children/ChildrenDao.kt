package com.hooware.allowancetracker.data.local.children

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hooware.allowancetracker.data.to.ChildTO

/**
 * Data Access Object for the children table.
 */
@Dao
interface ChildrenDao {
    /**
     * @return all children.
     */
    @Query("SELECT * FROM children")
    suspend fun getChildren(): List<ChildTO>

    /**
     * @param childId the id of the child
     * @return the child object with the childId
     */
    @Query("SELECT * FROM children where entry_id = :childId")
    suspend fun getChildById(childId: String): ChildTO?

    /**
     * Insert a child in the database. If the child already exists, abort.
     *
     * @param child the child to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChild(child: ChildTO)

    /**
     * Delete all children.
     */
    @Query("DELETE FROM children")
    suspend fun deleteAllChildren()

    /**
     * Delete child.
     */
    @Query("DELETE FROM children where entry_id = :childId")
    suspend fun deleteChild(childId: String)

}