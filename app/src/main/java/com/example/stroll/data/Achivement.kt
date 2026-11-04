package com.example.stroll.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    var title: String,
    var description: String,
    var imageResId: Int,
    var isUnlocked: Boolean = false,
    var currentProgress: Int = 0,
    var maxProgress: Int = 1,
    var unlockedTime: Long = 0L,
    var isSynced: Boolean = false
)