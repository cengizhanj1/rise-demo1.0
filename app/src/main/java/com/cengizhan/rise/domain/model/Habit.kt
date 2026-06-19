package com.cengizhan.rise.domain.model

data class Habit(
    val title: String,
    val category: String,
    val isCompletedToday: Boolean = false
)