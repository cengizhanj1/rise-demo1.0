package com.cengizhan.rise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String,
    val isCompletedToday: Boolean = false,
    val streakCount: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String? = null,
    val reminderTime: String? = null,
    val startDateMillis: Long? = null,
    val isCustomProgram: Boolean = false,
    val milestoneMessages: String? = null
)
