package com.example.capstoneproject.product_management.data.Room.branch

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BranchDao {
    @Query("SELECT * FROM Branches")
    fun getAll(): Flow<List<Branch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBranch(branch: Branch)

    @Delete
    fun deleteBranch(branch: Branch)
}