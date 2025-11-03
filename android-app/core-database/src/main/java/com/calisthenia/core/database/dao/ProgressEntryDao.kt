package com.calisthenia.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calisthenia.core.database.entity.ProgressEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressEntryDao {

    @Query("SELECT * FROM progress_entry ORDER BY timestamp DESC")
    fun observeEntries(): Flow<List<ProgressEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ProgressEntryEntity)

    @Query("DELETE FROM progress_entry WHERE id = :id")
    suspend fun deleteById(id: String)
}
