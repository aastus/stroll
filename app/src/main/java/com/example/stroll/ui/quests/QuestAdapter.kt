package com.example.stroll.ui.quests

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stroll.R
import com.example.stroll.data.Quest

class QuestAdapter(
    private val quests: MutableList<Quest>,
    private val onDeleteClick: (Quest, Int) -> Unit
) : RecyclerView.Adapter<QuestAdapter.QuestViewHolder>() {

    class QuestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivBg: ImageView = view.findViewById(R.id.iv_quest_bg)
        val tvTitle: TextView = view.findViewById(R.id.tv_quest_title)
        val tvDistance: TextView = view.findViewById(R.id.tv_quest_distance)
        val tvDifficulty: TextView = view.findViewById(R.id.tv_difficulty)
        val tvRoute: TextView = view.findViewById(R.id.tv_quest_route)
        val btnDelete: ImageView = view.findViewById(R.id.btn_delete_quest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quest, parent, false)
        return QuestViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val quest = quests[position]

        holder.tvTitle.text = quest.name
        holder.tvDistance.text = String.format("%.1f km", quest.distanceKm)
        holder.tvDifficulty.text = quest.difficulty
        
        holder.tvRoute.text = "${quest.startLocationName} ➔ ${quest.destLocationName}"

        try {
            holder.ivBg.setImageURI(Uri.parse(quest.imageUri))
        } catch (e: Exception) {
            e.printStackTrace() 
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(quest, position)
        }
    }

    override fun getItemCount(): Int = quests.size
}