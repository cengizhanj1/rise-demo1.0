package com.cengizhan.rise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cengizhan.rise.data.local.entity.CoachMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachMessageDao {

    @Query("SELECT * FROM coach_messages WHERE createdAtMillis >= :sinceMillis ORDER BY createdAtMillis DESC")
    fun observeMessagesSince(sinceMillis: Long): Flow<List<CoachMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CoachMessageEntity)

    @Query("DELETE FROM coach_messages WHERE createdAtMillis < :beforeMillis")
    suspend fun deleteOlderThan(beforeMillis: Long)

    @Query(
        "SELECT COUNT(*) FROM coach_messages " +
            "WHERE type = :type AND createdAtMillis BETWEEN :startMillis AND :endMillis"
    )
    suspend fun countMessagesForTypeBetween(
        type: String,
        startMillis: Long,
        endMillis: Long
    ): Int

    @Query("SELECT COUNT(*) FROM coach_messages WHERE createdAtMillis BETWEEN :startMillis AND :endMillis")
    suspend fun countMessagesBetween(
        startMillis: Long,
        endMillis: Long
    ): Int
}
