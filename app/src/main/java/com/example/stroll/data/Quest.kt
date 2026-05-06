package com.example.stroll.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val startLocationName: String,
    val destLocationName: String,
    val distanceKm: Double,
    val difficulty: String,
    val imageUri: String 
)