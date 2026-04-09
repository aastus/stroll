package com.example.stroll.ui.profile

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.stroll.R

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

        tabStatistics = view.findViewById(R.id.tab_statistics)
        tabSettings = view.findViewById(R.id.tab_settings)
        layoutStatistics = view.findViewById(R.id.layout_statistics)
        layoutSettings = view.findViewById(R.id.layout_settings)

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
}