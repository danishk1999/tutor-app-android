package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.R

class TutorProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var emailAddress: TextView
    private lateinit var special: TextView
    private lateinit var rating: TextView
    private lateinit var password: EditText
    private lateinit var passwordBtn: ImageView
    private lateinit var logoutBtn: TextView
    private lateinit var student_name: TextView
    private lateinit var bckBtn: ImageView

    private var isPasswordVisible = false // Track password visibility state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutor_profile)

        // Initialize views
        profileImage = findViewById(R.id.profilepic)
        userName = findViewById(R.id.userName)
        emailAddress = findViewById(R.id.emailAddress)
        rating = findViewById(R.id.Rating)
        special = findViewById(R.id.Speciality)
        password = findViewById(R.id.password)
        passwordBtn = findViewById(R.id.passwordBtn)
        logoutBtn = findViewById(R.id.logoutBtn)
        student_name = findViewById(R.id.tutor_name)
        bckBtn = findViewById(R.id.backBtn)

        // Set password visibility initially to hidden
        password.transformationMethod = PasswordTransformationMethod.getInstance()

        // Load user profile
        loadUserProfile()

        // Handle password visibility toggle
        passwordBtn.setOnClickListener {
            togglePasswordVisibility()
        }

        // Handle logout
        logoutBtn.setOnClickListener {
            logoutUser()
        }
        bckBtn.setOnClickListener {
            val intent = Intent(this@TutorProfileActivity, TutorHomePageActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loadUserProfile() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val name = sharedPreferences.getString("user_name", "N/A")
        val email = sharedPreferences.getString("user_email", "N/A")
        val userRating = sharedPreferences.getFloat("user_rating", 0.0F)
        val userSpecial = sharedPreferences.getString("user_special", "N/A")
        val profileImg = sharedPreferences.getString("profile_image", null)
        Log.d("ProfileActivity", "Profile Image URL: $profileImg")
        val userPassword = sharedPreferences.getString("user_password", "N/A")

        userName.text = name ?: "N/A"
        emailAddress.text = email ?: "N/A"
        rating.text = userRating.toString()
        special.text = userSpecial ?: "N/A"
        password.setText(userPassword)
        student_name.text = name ?: "N/A"

        if (!profileImg.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileImg)
                .placeholder(R.drawable.profile_icon)
                .error(R.drawable.error)
                .into(profileImage)
        } else {
            Glide.with(this)
                .load(R.drawable.profile_icon)
                .into(profileImage)
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            password.transformationMethod = PasswordTransformationMethod.getInstance()
            passwordBtn.setImageResource(R.drawable.eye_closed) // Change to closed eye icon
        } else {
            password.transformationMethod = null
            passwordBtn.setImageResource(R.drawable.eye) // Change to open eye icon
        }
        isPasswordVisible = !isPasswordVisible
        password.setSelection(password.text.length) // Move cursor to the end of the text
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}