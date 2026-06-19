package com.cengizhan.rise.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cengizhan.rise.data.local.dao.CoachMessageDao
import com.cengizhan.rise.data.local.dao.HabitDao
import com.cengizhan.rise.data.local.entity.CoachMessageEntity
import com.cengizhan.rise.data.local.entity.HabitEntity

@Database(
    entities = [HabitEntity::class, CoachMessageEntity::class],
    version = 5,
    exportSchema = false
)
abstract class RiseDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun coachMessageDao(): CoachMessageDao
}
