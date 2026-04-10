package com.example.stroll.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStat(
    @PrimaryKey val date: String,
    var steps: Int,
    var calories: Int,
    var distance: Double,
    var activeMinutes: Int
)