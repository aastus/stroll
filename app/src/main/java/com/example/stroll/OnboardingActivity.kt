package com.example.stroll

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val etName = findViewById<EditText>(R.id.et_name)
        val etAge = findViewById<EditText>(R.id.et_age)
        val etHeight = findViewById<EditText>(R.id.et_height)
        val etWeight = findViewById<EditText>(R.id.et_weight)
        val btnStart = findViewById<Button>(R.id.btn_start)

        btnStart.setOnClickListener {
            val name = etName.text.toString()
            val age = etAge.text.toString()
            val height = etHeight.text.toString()
            val weight = etWeight.text.toString()

            // Перевіряємо, чи заповнені основні поля
            if (name.isEmpty() || height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Please fill in your details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Зберігаємо дані в пам'ять телефону (SharedPreferences)
            val prefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("name", name)
                .putInt("age", age.toIntOrNull() ?: 25)
                .putFloat("height", height.toFloatOrNull() ?: 170f)
                .putFloat("weight", weight.toFloatOrNull() ?: 65f)
                .putBoolean("isFirstRun", false) // Запам'ятовуємо, що знайомство пройдено
                .apply()

            // Переходимо на головний екран
            startActivity(Intent(this, MainActivity::class.java))

            // Закриваємо цей екран, щоб користувач не міг повернутися сюди кнопкою "Назад"
            finish()
        }
    }
}