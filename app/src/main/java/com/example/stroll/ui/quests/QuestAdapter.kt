package com.example.stroll.ui.quests

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stroll.R
import com.example.stroll.data.Quest

class QuestAdapter(
    private val quests: MutableList<Quest>,
    private val onDeleteClick: (Quest, Int) -> Unit,
    private val onActionClick: (Quest, Int) -> Unit
) : RecyclerView.Adapter<QuestAdapter.QuestViewHolder>() {

    class QuestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageContainer: FrameLayout = view.findViewById(R.id.image_container)
        val ivBg: ImageView = view.findViewById(R.id.iv_quest_bg)
        val tvTitle: TextView = view.findViewById(R.id.tv_quest_title)
        val tvDescription: TextView = view.findViewById(R.id.tv_quest_description)
        val tvDifficulty: TextView = view.findViewById(R.id.tv_difficulty)
        val tvXpReward: TextView = view.findViewById(R.id.tv_xp_reward)
        val tvProgressText: TextView = view.findViewById(R.id.tv_progress_text)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        val btnAction: Button = view.findViewById(R.id.btn_action)
        val btnDelete: ImageView = view.findViewById(R.id.btn_delete_quest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quest, parent, false)
        return QuestViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val quest = quests[position]

        holder.tvTitle.text = quest.name
        holder.tvDescription.text = quest.description

        // Quest image
        if (quest.imageUri.isNotEmpty()) {
            holder.imageContainer.visibility = View.VISIBLE
            try {
                holder.ivBg.setImageURI(Uri.parse(quest.imageUri))
            } catch (e: Exception) {
                holder.imageContainer.visibility = View.GONE
            }
        } else {
            holder.imageContainer.visibility = View.GONE
        }

        // Difficulty badge — use .mutate() to avoid shared drawable state
        holder.tvDifficulty.text = quest.difficulty
        val badgeBg = holder.tvDifficulty.background.mutate()
        if (badgeBg is GradientDrawable) {
            val color = when {
                quest.difficulty.lowercase().contains("easy") -> Color.parseColor("#4CAF50")
                quest.difficulty.lowercase().contains("medium") -> Color.parseColor("#FF9800")
                quest.difficulty.lowercase().contains("hard") -> Color.parseColor("#F44336")
                quest.difficulty.lowercase().contains("extreme") -> Color.parseColor("#9C27B0")
                else -> Color.parseColor("#8A2BE2")
            }
            badgeBg.setColor(color)
            holder.tvDifficulty.background = badgeBg
        }

        // XP reward
        holder.tvXpReward.text = "\uD83C\uDFC6 ${quest.xpReward} XP"

        // Progress in km
        val currentKm = quest.currentSteps / 1000.0
        val targetKm = quest.distanceKm
        holder.tvProgressText.text = String.format("%.1f / %.1f km", currentKm, targetKm)

        val progressPercent = if (targetKm > 0) {
            ((currentKm / targetKm) * 100).toInt()
        } else 0
        holder.progressBar.progress = progressPercent.coerceAtMost(100)

        // Action button
        when (quest.status) {
            "active" -> {
                holder.btnAction.text = "Quest Active"
                holder.btnAction.isEnabled = true
                holder.btnAction.alpha = 0.8f
            }
            "paused" -> {
                holder.btnAction.text = "▶  Resume Quest"
                holder.btnAction.isEnabled = true
                holder.btnAction.alpha = 1f
            }
            "completed" -> {
                holder.btnAction.text = "✅  Completed"
                holder.btnAction.isEnabled = false
                holder.btnAction.alpha = 0.6f
            }
            else -> {
                holder.btnAction.text = "▶  Start Quest"
                holder.btnAction.isEnabled = true
                holder.btnAction.alpha = 1f
            }
        }

        holder.btnAction.setOnClickListener {
            onActionClick(quest, position)
        }

        // Delete button — available for all quests
        holder.btnDelete.setOnClickListener {
            onDeleteClick(quest, position)
        }
    }

    override fun getItemCount(): Int = quests.size
}