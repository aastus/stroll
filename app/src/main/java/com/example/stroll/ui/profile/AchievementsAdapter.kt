package com.example.stroll.ui.achievements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stroll.R
import com.example.stroll.data.Achievement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AchievementsAdapter(
    private val achievements: List<Achievement>
) : RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder>() {

    class AchievementViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val image: ImageView = view.findViewById(R.id.imgAchievement)
        val title: TextView = view.findViewById(R.id.txtTitle)
        val description: TextView = view.findViewById(R.id.txtDescription)
        val progress: TextView = view.findViewById(R.id.txtProgress)
        val unlocked: TextView = view.findViewById(R.id.txtUnlocked)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)

        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {

        val achievement = achievements[position]

        holder.image.setImageResource(achievement.imageResId)
        holder.title.text = achievement.title
        holder.description.text = achievement.description

        holder.progress.text =
            "${achievement.currentProgress} / ${achievement.maxProgress}"

        if (achievement.isUnlocked) {

            val formatter =
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            val date = formatter.format(Date(achievement.unlockedTime))

            holder.unlocked.text = "Unlocked: $date"

            holder.itemView.alpha = 1f

        } else {

            holder.unlocked.text = "Locked"

            holder.itemView.alpha = 0.6f
        }
    }

    override fun getItemCount(): Int = achievements.size
}