package com.example.stroll.ui.profile

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.stroll.R
import com.example.stroll.data.AppDatabase
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var tabStatistics: TextView
    private lateinit var tabSettings: TextView
    private lateinit var layoutStatistics: LinearLayout
    private lateinit var layoutSettings: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ініціалізація вкладок
        tabStatistics = view.findViewById(R.id.tab_statistics)
        tabSettings = view.findViewById(R.id.tab_settings)
        layoutStatistics = view.findViewById(R.id.layout_statistics)
        layoutSettings = view.findViewById(R.id.layout_settings)

        setupTabs()
        loadUserData(view)
        loadStatistics(view)
        setupSaveButton(view)
    }

    private fun setupTabs() {
        tabStatistics.setOnClickListener {
            layoutStatistics.visibility = View.VISIBLE
            layoutSettings.visibility = View.GONE
            tabStatistics.setBackgroundResource(R.drawable.bg_tab_active)
            tabStatistics.setTextColor(Color.parseColor("#0D1B2A"))
            tabSettings.setBackgroundColor(Color.TRANSPARENT)
            tabSettings.setTextColor(Color.parseColor("#6C757D"))
        }

        tabSettings.setOnClickListener {
            layoutSettings.visibility = View.VISIBLE
            layoutStatistics.visibility = View.GONE
            tabSettings.setBackgroundResource(R.drawable.bg_tab_active)
            tabSettings.setTextColor(Color.parseColor("#0D1B2A"))
            tabStatistics.setBackgroundColor(Color.TRANSPARENT)
            tabStatistics.setTextColor(Color.parseColor("#6C757D"))
        }
    }

    private fun loadUserData(view: View) {
        val prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val name = prefs.getString("name", "Walker") ?: "Walker"
        val goal = prefs.getInt("step_goal", 10000)

        // Встановлюємо в шапку
        view.findViewById<TextView>(R.id.tv_profile_name).text = name
        view.findViewById<TextView>(R.id.tv_avatar_initial).text = name.firstOrNull()?.uppercase() ?: "W"

        // Встановлюємо в поля налаштувань
        view.findViewById<EditText>(R.id.et_username).setText(name)
        view.findViewById<EditText>(R.id.et_step_goal).setText(goal.toString())
    }

    private fun loadStatistics(view: View) {
        val database = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch(Dispatchers.IO) {
            val allHistory = database.statDao().getAllHistory()

            var totalSteps = 0
            var totalCals = 0
            var totalDist = 0.0
            val daysTracked = allHistory.size

            for (stat in allHistory) {
                totalSteps += stat.steps
                totalCals += stat.calories
                totalDist += stat.distance
            }

            val avgSteps = if (daysTracked > 0) totalSteps / daysTracked else 0

            withContext(Dispatchers.Main) {
                // Оновлюємо текстові поля
                view.findViewById<TextView>(R.id.tv_total_steps).text = totalSteps.toString()
                view.findViewById<TextView>(R.id.tv_total_km).text = String.format("%.1f", totalDist)
                view.findViewById<TextView>(R.id.tv_avg_steps).text = avgSteps.toString()
                view.findViewById<TextView>(R.id.tv_total_calories).text = totalCals.toString()
                view.findViewById<TextView>(R.id.tv_days_tracked).text = daysTracked.toString()

                // Оновлюємо графік (беремо останні 7 днів, або менше)
                updateChart(view, allHistory.take(7).map { it.steps }.reversed())
            }
        }
    }

    private fun updateChart(view: View, stepsList: List<Int>) {
        val bars = listOf(
            view.findViewById<View>(R.id.bar_1),
            view.findViewById<View>(R.id.bar_2),
            view.findViewById<View>(R.id.bar_3),
            view.findViewById<View>(R.id.bar_4),
            view.findViewById<View>(R.id.bar_5),
            view.findViewById<View>(R.id.bar_6),
            view.findViewById<View>(R.id.bar_7)
        )

        // Знаходимо максимальне значення для пропорції графіка (або мінімум 1000 щоб не було ділення на 0)
        val maxSteps = stepsList.maxOrNull()?.coerceAtLeast(1000) ?: 10000
        val maxDpHeight = 140f // Максимальна висота графіка в dp
        val density = resources.displayMetrics.density

        // Заповнюємо стовпчики з кінця (bar_7 - це сьогодні)
        val startIndex = 7 - stepsList.size
        for (i in 0 until 7) {
            val layoutParams = bars[i].layoutParams

            if (i >= startIndex) {
                val stepValue = stepsList[i - startIndex]
                // Розраховуємо висоту
                val heightInDp = (stepValue.toFloat() / maxSteps) * maxDpHeight
                layoutParams.height = (heightInDp * density).toInt().coerceAtLeast(10) // мінімум 10px
            } else {
                layoutParams.height = (10 * density).toInt() // порожній стовпчик
            }
            bars[i].layoutParams = layoutParams
        }
    }

    private fun setupSaveButton(view: View) {
        val btnSave = view.findViewById<MaterialButton>(R.id.btn_save)
        val etName = view.findViewById<EditText>(R.id.et_username)
        val etGoal = view.findViewById<EditText>(R.id.et_step_goal)

        btnSave.setOnClickListener {
            val newName = etName.text.toString()
            val newGoal = etGoal.text.toString().toIntOrNull() ?: 10000

            if (newName.isNotEmpty()) {
                val prefs = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("name", newName)
                    .putInt("step_goal", newGoal)
                    .apply()

                // Оновлюємо шапку відразу
                view.findViewById<TextView>(R.id.tv_profile_name).text = newName
                view.findViewById<TextView>(R.id.tv_avatar_initial).text = newName.firstOrNull()?.uppercase() ?: "W"

                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}