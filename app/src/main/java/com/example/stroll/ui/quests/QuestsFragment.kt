package com.example.stroll.ui.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stroll.R
import com.example.stroll.data.AppDatabase
import com.example.stroll.data.Quest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuestsFragment : Fragment() {

    private lateinit var rvQuests: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var database: AppDatabase
    private lateinit var questAdapter: QuestAdapter
    private var questList = mutableListOf<Quest>()

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

        setupRecyclerView()
        loadQuests()
    }

    private fun setupRecyclerView() {
        rvQuests.layoutManager = LinearLayoutManager(requireContext())
        
        questAdapter = QuestAdapter(questList) { questToDelete, position ->
            deleteQuest(questToDelete, position)
        }
        rvQuests.adapter = questAdapter
    }

    private fun loadQuests() {
        lifecycleScope.launch(Dispatchers.IO) {
            val questsFromDb = database.questDao().getAllQuests()
            
            withContext(Dispatchers.Main) {
                questList.clear()
                questList.addAll(questsFromDb)
                questAdapter.notifyDataSetChanged()
                
                tvEmptyState.visibility = if (questList.isEmpty()) View.VISIBLE else View.GONE
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
            }
        }
    }
}