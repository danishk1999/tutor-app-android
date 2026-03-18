package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Adapter.CategoryAdapter
import com.example.myapplication.Adapter.TopTutorAdapter
import com.example.myapplication.R
import com.example.myapplication.ViewModel.MainViewModel
import com.example.myapplication.databinding.ActivityHomeBinding
import com.bumptech.glide.Glide

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "User")
        val profileImg = sharedPreferences.getString("profile_image", null)

        binding.userNameTextView.text = "Hi, $userName"

        // Set profile image if available
        if (profileImg != null) {
            Glide.with(this)
                .load(profileImg) // Load the image URL
                .placeholder(R.drawable.profile_icon) // Add a placeholder drawable
                .error(R.drawable.error) // Add an error drawable
                .into(binding.profilepic) // Load into the ImageView with ID 'profilepic'
        } else {
            // Set a default image if no profile image is available
            binding.profilepic.setImageResource(R.drawable.profile_icon)
        }

        initCategory()
        initTopTutors()

        binding.userCalendarBtn.setOnClickListener {
            val intent = Intent(this@HomeActivity, UserCalendarActivity::class.java)
            startActivity(intent)
        }

        binding.profileBtn.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.messageBtn.setOnClickListener{

            val intent = Intent(this@HomeActivity,ChatInBoxActivity::class.java)
            intent.putExtra("currentUserId", getUserId())
            startActivity(intent)
        }
    }

    private fun getUserId(): Int {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }

    private fun initTopTutors() {
        binding.apply {
            progressBarTopDoctor.visibility = View.VISIBLE
            viewModel.tutors.observe(this@HomeActivity, Observer {
                recyclerViewTopTutors.layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
                recyclerViewTopTutors.adapter = TopTutorAdapter(it)
                progressBarTopDoctor.visibility = View.GONE
            })
            viewModel.loadTutors()

            tutorListTxt.setOnClickListener {
                // Start TopTutorActivity without any subject filter
                startActivity(Intent(this@HomeActivity, TopTutorActivity::class.java))
            }
        }
    }

    private fun initCategory() {
        binding.progressBarCategory.visibility = View.VISIBLE
        viewModel.category.observe(this, Observer { categories ->
            binding.viewCategory.layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            binding.viewCategory.adapter = CategoryAdapter(categories) { category ->
                // Pass selected subject when a category is clicked
                val intent = Intent(this@HomeActivity, TopTutorActivity::class.java)
                intent.putExtra("selectedSubject", category.Name)
                startActivity(intent)
            }
            binding.progressBarCategory.visibility = View.GONE
        })
        viewModel.loadCategory()
    }
}
