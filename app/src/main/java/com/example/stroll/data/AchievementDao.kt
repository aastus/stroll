package com.example.stroll.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<Achievement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(
        achievement: Achievement
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        achievements: List<Achievement>
    )
}