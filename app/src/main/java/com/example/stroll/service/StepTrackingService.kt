package com.example.stroll.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.stroll.MainActivity
import com.example.stroll.R
import com.example.stroll.data.AppDatabase
import com.example.stroll.data.DailyStat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepTrackingService : Service(), SensorEventListener {

    companion object {
        const val CHANNEL_ID = "StepTrackingChannel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_RESTART = "com.example.stroll.RESTART_SERVICE"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null

    private var initialStepsAtStartOfDay = 0f
    private var isSensorInitialized = false
    private var currentDayString = ""

    private var userWeight = 65f
    private var stepLengthMeters = 0.705f

    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getDatabase(applicationContext)

        val prefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val heightCm = prefs.getFloat("height", 170f)
        userWeight = prefs.getFloat("weight", 65f)
        stepLengthMeters = (heightCm * 0.415f) / 100f

        currentDayString = getTodayString()
        loadInitialOffset()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification(0)
        startForeground(NOTIFICATION_ID, notification)

        stepSensor?.also {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val today = getTodayString()

        if (today != currentDayString) {
            // Новий день — зберігаємо вчорашню дистанцію в accumulated для квесту
            finalizeQuestDayProgress()
            currentDayString = today
            isSensorInitialized = false
        }

        val currentTotalSensorSteps = event.values[0]

        if (!isSensorInitialized) {
            initialStepsAtStartOfDay = currentTotalSensorSteps
            saveInitialOffset(initialStepsAtStartOfDay)
            isSensorInitialized = true
        }

        val todaySteps = (currentTotalSensorSteps - initialStepsAtStartOfDay).toInt()
        val validSteps = if (todaySteps > 0) todaySteps else 0

        val dist = (validSteps * stepLengthMeters) / 1000.0
        val cals = (dist * userWeight * 1.036).toInt()
        val mins = validSteps / 100

        serviceScope.launch {
            val dailyStat = DailyStat(currentDayString, validSteps, cals, dist, mins)
            database.statDao().insertDailyStat(dailyStat)

            // Оновлюємо прогрес активного квесту
            updateActiveQuestProgress(dist)
        }

        updateNotification(validSteps)
    }

    private suspend fun updateActiveQuestProgress(todayDistanceKm: Double) {
        val activeQuest = database.questDao().getActiveQuest() ?: return

        val prefs = getSharedPreferences("QuestPrefs", Context.MODE_PRIVATE)
        val accumulatedMeters = prefs.getInt("quest_${activeQuest.id}_accumulated", 0)
        val todayMeters = (todayDistanceKm * 1000).toInt()
        val totalMeters = accumulatedMeters + todayMeters

        database.questDao().updateQuestProgress(activeQuest.id, totalMeters)

        // Перевіряємо завершення квесту
        val targetMeters = (activeQuest.distanceKm * 1000).toInt()
        if (totalMeters >= targetMeters) {
            database.questDao().updateQuestStatus(activeQuest.id, "completed")
        }
    }

    private fun finalizeQuestDayProgress() {
        serviceScope.launch {
            val activeQuest = database.questDao().getActiveQuest() ?: return@launch
            val prefs = getSharedPreferences("QuestPrefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("quest_${activeQuest.id}_accumulated", activeQuest.currentSteps).apply()
        }
    }

    // Авторестарт сервісу якщо його вбила система або змахнули нотифікацію
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartIntent = Intent(applicationContext, StepTrackingService::class.java).apply {
            setPackage(packageName)
        }
        val pendingIntent = PendingIntent.getService(
            applicationContext, 1, restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            pendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        // Перезапускаємо сервіс
        val restartIntent = Intent(applicationContext, StepTrackingService::class.java)
        startForegroundService(restartIntent)
        super.onDestroy()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Step Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Tracks your steps in the background"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(steps: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Stroll — Step Tracker")
            .setContentText("Today: $steps steps")
            .setSmallIcon(R.drawable.ic_hiking)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(steps))
    }

    private fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun saveInitialOffset(offset: Float) {
        val prefs = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
        prefs.edit().putFloat("offset_$currentDayString", offset).apply()
    }

    private fun loadInitialOffset() {
        val prefs = getSharedPreferences("StepPrefs", Context.MODE_PRIVATE)
        val savedOffset = prefs.getFloat("offset_$currentDayString", -1f)
        if (savedOffset != -1f) {
            initialStepsAtStartOfDay = savedOffset
            isSensorInitialized = true
        } else {
            isSensorInitialized = false
        }
    }
}
