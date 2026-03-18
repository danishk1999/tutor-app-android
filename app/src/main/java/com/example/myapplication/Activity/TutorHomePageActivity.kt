package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
//import com.example.myapplication.Adapter.AppointmentAdapter
import com.example.myapplication.R
import com.example.myapplication.ViewModel.MainViewModel
import de.hdodenhof.circleimageview.CircleImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import com.example.myapplication.databinding.ActivityHomeBinding
import com.example.myapplication.databinding.ActivityTutorHomePageBinding

class TutorHomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorHomePageBinding
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "User")
        val profileImg = sharedPreferences.getString("profile_image", null)

        binding.tutorName.text = "Hi, $userName"

        if (profileImg != null) {
            Glide.with(this)
                .load(profileImg)
                .placeholder(R.drawable.profile_icon)
                .error(R.drawable.error)
                .into(binding.tutorProfilePic)
        } else {
            // Set a default if no image exits
            binding.tutorProfilePic.setImageResource(R.drawable.profile_icon)
        }
        binding.homebtn.setOnClickListener {
            val intent = Intent(this@TutorHomePageActivity, TutorHomePageActivity::class.java)
            startActivity(intent)
        }
        binding.userCalendarBtn.setOnClickListener {
            val intent = Intent(this@TutorHomePageActivity, UserCalendarActivity::class.java)
            startActivity(intent)
        }
        binding.profileBtn.setOnClickListener {
            val intent = Intent(this@TutorHomePageActivity, TutorProfileActivity::class.java)
            startActivity(intent)
        }
        binding.navMessages.setOnClickListener{

            val intent = Intent(this@TutorHomePageActivity,ChatInBoxActivity::class.java)
            intent.putExtra("currentUserId", getUserId())
            startActivity(intent)
        }
    }
    private fun getUserId(): Int {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }

}
