package com.example.stroll.ui.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stroll.R
import com.example.stroll.data.AppDatabase
import com.example.stroll.data.Quest
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuestsFragment : Fragment() {

    private lateinit var rvQuests: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var database: AppDatabase
    private lateinit var questAdapter: QuestAdapter
    private var questList = mutableListOf<Quest>()

    // Active quest card views
    private lateinit var cardActiveQuest: MaterialCardView
    private lateinit var tvActiveQuestName: TextView
    private lateinit var tvActiveQuestDesc: TextView
    private lateinit var tvActiveProgressText: TextView
    private lateinit var progressActive: ProgressBar
    private lateinit var tvActivePercent: TextView
    private lateinit var tvActiveXp: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvQuests = view.findViewById(R.id.rv_quests)
        tvEmptyState = view.findViewById(R.id.tv_empty_state)
        database = AppDatabase.getDatabase(requireContext())

        // Active quest card
        cardActiveQuest = view.findViewById(R.id.card_active_quest)
        tvActiveQuestName = view.findViewById(R.id.tv_active_quest_name)
        tvActiveQuestDesc = view.findViewById(R.id.tv_active_quest_desc)
        tvActiveProgressText = view.findViewById(R.id.tv_active_progress_text)
        progressActive = view.findViewById(R.id.progress_active)
        tvActivePercent = view.findViewById(R.id.tv_active_percent)
        tvActiveXp = view.findViewById(R.id.tv_active_xp)

        setupRecyclerView()
        seedDefaultQuestsIfNeeded()
    }

    private fun setupRecyclerView() {
        rvQuests.layoutManager = LinearLayoutManager(requireContext())

        questAdapter = QuestAdapter(
            questList,
            onDeleteClick = { questToDelete, position -> deleteQuest(questToDelete, position) },
            onActionClick = { quest, position -> handleQuestAction(quest, position) }
        )
        rvQuests.adapter = questAdapter
    }

    private fun seedDefaultQuestsIfNeeded() {
        lifecycleScope.launch(Dispatchers.IO) {
            val count = database.questDao().getQuestCount()
            if (count == 0) {
                val defaults = listOf(
                    Quest(
                        name = "Morning Walker",
                        description = "Walk 5,000 steps before noon",
                        startLocationName = "",
                        destLocationName = "",
                        distanceKm = 3.5,
                        difficulty = "easy",
                        imageUri = "",
                        targetSteps = 5000,
                        xpReward = 100,
                        status = "available",
                        isDefault = true
                    ),
                    Quest(
                        name = "Daily Champion",
                        description = "Reach 10,000 steps today",
                        startLocationName = "",
                        destLocationName = "",
                        distanceKm = 7.0,
                        difficulty = "medium",
                        imageUri = "",
                        targetSteps = 10000,
                        xpReward = 250,
                        status = "available",
                        isDefault = true
                    ),
                    Quest(
                        name = "Marathon Master",
                        description = "Walk 20,000 steps in a single day",
                        startLocationName = "",
                        destLocationName = "",
                        distanceKm = 14.0,
                        difficulty = "hard",
                        imageUri = "",
                        targetSteps = 20000,
                        xpReward = 500,
                        status = "available",
                        isDefault = true
                    )
                )
                defaults.forEach { database.questDao().insertQuest(it) }
            }
            withContext(Dispatchers.Main) {
                loadQuests()
            }
        }
    }

    private fun loadQuests() {
        lifecycleScope.launch(Dispatchers.IO) {
            val questsFromDb = database.questDao().getAllQuests()
            val activeQuest = database.questDao().getActiveQuest()

            withContext(Dispatchers.Main) {
                // Update active quest card
                if (activeQuest != null) {
                    cardActiveQuest.visibility = View.VISIBLE
                    tvActiveQuestName.text = activeQuest.name
                    tvActiveQuestDesc.text = activeQuest.description

                    val currentKm = activeQuest.currentSteps / 1000.0
                    val targetKm = activeQuest.distanceKm
                    tvActiveProgressText.text = String.format("%.1f / %.1f km", currentKm, targetKm)

                    val percent = if (targetKm > 0) ((currentKm / targetKm) * 100).toInt().coerceAtMost(100) else 0
                    progressActive.progress = percent
                    tvActivePercent.text = "${percent}% complete"
                    tvActiveXp.text = "\uD83C\uDFC6 ${activeQuest.xpReward} XP"
                } else {
                    cardActiveQuest.visibility = View.GONE
                }

                // Update quest list
                questList.clear()
                questList.addAll(questsFromDb)
                questAdapter.notifyDataSetChanged()

                tvEmptyState.visibility = if (questList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun handleQuestAction(quest: Quest, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            when (quest.status) {
                "available", "paused" -> {
                    // Pause any currently active quest first
                    val activeQuest = database.questDao().getActiveQuest()
                    if (activeQuest != null && activeQuest.id != quest.id) {
                        database.questDao().updateQuestStatus(activeQuest.id, "paused")
                    }
                    // Activate this quest
                    database.questDao().updateQuestStatus(quest.id, "active")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Quest \"${quest.name}\" activated!", Toast.LENGTH_SHORT).show()
                    }
                }
                "active" -> {
                    // Pause this quest
                    database.questDao().updateQuestStatus(quest.id, "paused")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Quest \"${quest.name}\" paused", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                loadQuests()
            }
        }
    }

    private fun deleteQuest(quest: Quest, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.questDao().deleteQuest(quest)

            withContext(Dispatchers.Main) {
                questList.removeAt(position)
                questAdapter.notifyItemRemoved(position)

                if (questList.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                }

                // Refresh active quest card if we deleted the active one
                loadQuests()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::database.isInitialized) {
            loadQuests()
        }
    }
}