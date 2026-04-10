package com.example.stroll.ui.dashboard

import android.content.Context
import android.hardware.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.stroll.R
import com.example.stroll.data.AppDatabase
import com.example.stroll.data.DailyStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var running = false

    // Математика сенсора
    private var initialStepsAtStartOfDay = 0f
    private var isSensorInitialized = false

    // Дата і База
    private lateinit var database: AppDatabase
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private var currentDateCalendar = Calendar.getInstance()
    private var todayString = dateFormat.format(Date())

    // UI Елементи
    private lateinit var tvDateHeader: TextView
    private lateinit var tvStepsTaken: TextView
    private lateinit var tvPercent: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvMinutes: TextView
    private lateinit var btnPrevDay: ImageView
    private lateinit var btnNextDay: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ініціалізуємо БД
        database = AppDatabase.getDatabase(requireContext())

        // Знаходимо всі в'юшки
        tvDateHeader = view.findViewById(R.id.tv_date_header)
        tvStepsTaken = view.findViewById(R.id.tv_stepsTaken)
        tvPercent = view.findViewById(R.id.tv_percent)
        tvDistance = view.findViewById(R.id.tv_distance)
        tvCalories = view.findViewById(R.id.tv_calories)
        tvMinutes = view.findViewById(R.id.tv_minutes)
        btnPrevDay = view.findViewById(R.id.btn_prev_day)
        btnNextDay = view.findViewById(R.id.btn_next_day)

        // Ініціалізуємо сенсор
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        loadInitialOffset() // Завантажуємо точку відліку для сьогодні

        // Кнопки гортання днів
        btnPrevDay.setOnClickListener { changeDay(-1) }
        btnNextDay.setOnClickListener { changeDay(1) }

        // Завантажуємо дані для обраної дати (спочатку це "Сьогодні")
        loadDataForDate(dateFormat.format(currentDateCalendar.time))
    }

    private fun changeDay(offset: Int) {
        currentDateCalendar.add(Calendar.DAY_OF_YEAR, offset)
        val selectedDateStr = dateFormat.format(currentDateCalendar.time)

        // Ховаємо стрілку "вперед", якщо це сьогодні
        btnNextDay.visibility = if (selectedDateStr == todayString) View.INVISIBLE else View.VISIBLE

        tvDateHeader.text = if (selectedDateStr == todayString) "Today" else displayFormat.format(currentDateCalendar.time)

        loadDataForDate(selectedDateStr)
    }

    // Завантаження з SQLite
    private fun loadDataForDate(dateStr: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val stat = database.statDao().getStatByDate(dateStr)

            withContext(Dispatchers.Main) {
                if (stat != null) {
                    updateUI(stat.steps, stat.distance, stat.calories, stat.activeMinutes)
                } else {
                    updateUI(0, 0.0, 0, 0) // Якщо немає даних за той день
                }
            }
        }
    }

    // Реал-тайм оновлення з сенсора
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        // Оновлюємо ТІЛЬКИ якщо ми дивимось на "Сьогоднішній" день
        val isLookingAtToday = dateFormat.format(currentDateCalendar.time) == todayString

        if (running && isLookingAtToday) {
            val currentTotalSensorSteps = event.values[0]

            // Фіксуємо точку відліку при першому запуску за день
            if (!isSensorInitialized) {
                initialStepsAtStartOfDay = currentTotalSensorSteps
                saveInitialOffset(initialStepsAtStartOfDay)
                isSensorInitialized = true
            }

            // Рахуємо чисті кроки за сьогодні
            val todaySteps = (currentTotalSensorSteps - initialStepsAtStartOfDay).toInt()

            // Запобігаємо мінусовим крокам
            val validSteps = if (todaySteps > 0) todaySteps else 0

            // Математика статистики
            val dist = validSteps * 0.0008
            val cals = (validSteps * 0.0005 * 70).toInt() // 70 - вага
            val mins = validSteps / 110

            // Оновлюємо екран
            updateUI(validSteps, dist, cals, mins)

            // ЗБЕРІГАЄМО В БАЗУ ДАНИХ У РЕАЛЬНОМУ ЧАСІ
            saveToDatabase(todayString, validSteps, cals, dist, mins)
        }
    }

    private fun saveToDatabase(date: String, steps: Int, cals: Int, dist: Double, mins: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dailyStat = DailyStat(date, steps, cals, dist, mins)
            database.statDao().insertDailyStat(dailyStat)
        }
    }

    private fun updateUI(steps: Int, distance: Double, calories: Int, minutes: Int) {
        tvStepsTaken.text = steps.toString()
        val percent = ((steps * 100.0) / 10000).toInt() // 10000 - ціль
        tvPercent.text = "${percent.coerceAtMost(100)}%"

        tvDistance.text = String.format("%.2f", distance)
        tvCalories.text = calories.toString()
        tvMinutes.text = minutes.toString()
    }

    // ... (Методи onResume, onPause, onAccuracyChanged залишаються стандартними)
    override fun onResume() {
        super.onResume()
        running = true
        stepSensor?.also { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // Збереження "точки відліку" в SharedPreferences
    private fun saveInitialOffset(offset: Float) {
        val prefs = requireActivity().getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
        prefs.edit().putFloat("offset_$todayString", offset).apply()
    }

    private fun loadInitialOffset() {
        val prefs = requireActivity().getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
        val savedOffset = prefs.getFloat("offset_$todayString", -1f)
        if (savedOffset != -1f) {
            initialStepsAtStartOfDay = savedOffset
            isSensorInitialized = true
        } else {
            isSensorInitialized = false
        }
    }
}