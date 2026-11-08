package com.ims134.doclone.repo

import androidx.room3.ColumnTypeConverters
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.Update
import com.ims134.doclone.model.Clone
import kotlinx.coroutines.flow.Flow


@Dao
interface CloneStore {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClone(clone: Clone): Long

    @Delete
    suspend fun deleteClone(clone: Clone): Int

    @Update
    suspend fun updateClone(clone: Clone): Int

    @Query("SELECT * FROM Clone ORDER BY packageName")
    fun getClones(): Flow<List<Clone>>

    @Query("SELECT * FROM Clone WHERE packageName = :pkg ORDER BY label")
    fun getClones(pkg: String): Flow<List<Clone>>

    @Query("DELETE FROM Clone WHERE id = :id")
    suspend fun deleteCloneById(id: Long): Int

    @Query("DELETE FROM Clone")
    suspend fun deleteAllClones(): Int
}


@Database(entities = [Clone::class], version = 1, exportSchema = false)
@ColumnTypeConverters(Converters::class)
abstract class CloneDb : RoomDatabase()
{
    abstract val dao : CloneStore
}
