package com.example.stroll.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStat(stat: DailyStat)

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    suspend fun getStatByDate(date: String): DailyStat?

    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    suspend fun getAllHistory(): List<DailyStat>
}