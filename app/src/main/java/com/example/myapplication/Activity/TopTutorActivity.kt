package com.example.myapplication.Activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Adapter.TopTutorAdapter2
import com.example.myapplication.ViewModel.MainViewModel
import com.example.myapplication.databinding.ActivityTopTutorBinding

class TopTutorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTopTutorBinding
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the selected subject passed from HomeActivity, if any
        val selectedSubject = intent.getStringExtra("selectedSubject")

        initTopTutors(selectedSubject)
    }

    private fun initTopTutors(selectedSubject: String?) {
        binding.apply {
            progressBarTopTutor.visibility = View.VISIBLE
            viewModel.tutors.observe(this@TopTutorActivity, Observer { tutors ->
                // Display all tutors if no subject filter is provided, else filter by subject
                val filteredTutors = if (selectedSubject != null) {
                    tutors.filter { it.Special == selectedSubject }
                } else {
                    tutors
                }
                viewTopTutorList.layoutManager = LinearLayoutManager(this@TopTutorActivity, LinearLayoutManager.VERTICAL, false)
                viewTopTutorList.adapter = TopTutorAdapter2(filteredTutors.toMutableList())
                progressBarTopTutor.visibility = View.GONE
            })
            viewModel.loadTutors()

            backBtn.setOnClickListener { finish() }
        }
    }
}
