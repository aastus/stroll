package com.example.stroll.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val startLocationName: String,
    val destLocationName: String,
    val distanceKm: Double,
    val difficulty: String,
    val imageUri: String,
    val targetSteps: Int = 5000,
    val currentSteps: Int = 0,
    val xpReward: Int = 100,
    val status: String = "available",
    val isDefault: Boolean = false
)