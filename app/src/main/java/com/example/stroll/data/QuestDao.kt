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

    @Query("SELECT * FROM quests WHERE status = 'active' LIMIT 1")
    suspend fun getActiveQuest(): Quest?

    @Query("UPDATE quests SET status = :status WHERE id = :questId")
    suspend fun updateQuestStatus(questId: Int, status: String)

    @Query("UPDATE quests SET currentSteps = :steps WHERE id = :questId")
    suspend fun updateQuestProgress(questId: Int, steps: Int)

    @Query("SELECT COUNT(*) FROM quests")
    suspend fun getQuestCount(): Int
}