package com.example.stroll.ui.achievements

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stroll.R
import com.example.stroll.data.Achievement
import com.example.stroll.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AchievementsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AchievementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_achievements)

        recyclerView = findViewById(R.id.recyclerAchievements)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val database = AppDatabase.getDatabase(this)

        lifecycleScope.launch(Dispatchers.IO) {

            val dao = database.achievementDao()

            // якщо база порожня — додаємо стартові досягнення
            if (dao.getAllAchievements().isEmpty()) {

                dao.insertAll(
                    listOf(
                        Achievement(
                            "first_steps",
                            "First Steps",
                            "Walk 1000 steps",
                            R.drawable.baseline_settings_24,
                            true,
                            1000,
                            1000,
                            System.currentTimeMillis()
                        ),

                        Achievement(
                            "walker",
                            "Walker",
                            "Walk 10000 steps",
                            R.drawable.baseline_settings_24,
                            false,
                            4200,
                            10000,
                            0L
                        )
                    )
                )
            }

            val achievements = dao.getAllAchievements()

            withContext(Dispatchers.Main) {
                adapter = AchievementsAdapter(achievements)
                recyclerView.adapter = adapter
            }
        }
    }
}