package com.example.stroll.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface QuestDao {
    @Insert
    suspend fun insertQuest(quest: Quest)

    @Query("SELECT * FROM quests ORDER BY id DESC")
    suspend fun getAllQuests(): List<Quest>

    @Delete
    suspend fun deleteQuest(quest: Quest)
}