package com.example.stroll.ui.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.stroll.R

class CreateFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner: Spinner = view.findViewById(R.id.spinner_difficulty)
        val difficulties = arrayOf("Easy (100 XP)", "Medium (250 XP)", "Hard (500 XP)")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, difficulties)
        spinner.adapter = adapter
        spinner.setSelection(1)

        val btnCancel: Button = view.findViewById(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            view.findViewById<android.widget.EditText>(R.id.et_quest_title).text.clear()
            view.findViewById<android.widget.EditText>(R.id.et_quest_desc).text.clear()
            spinner.setSelection(1)
        }

        val btnCreate: Button = view.findViewById(R.id.btn_create)
        btnCreate.setOnClickListener {
            val title = view.findViewById<android.widget.EditText>(R.id.et_quest_title).text.toString()
            if (title.isNotEmpty()) {
                Toast.makeText(requireContext(), "Quest '$title' created successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter a quest title", Toast.LENGTH_SHORT).show()
            }
        }
    }
}