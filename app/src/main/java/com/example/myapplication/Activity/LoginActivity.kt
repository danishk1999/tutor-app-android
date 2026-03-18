package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.firebase.database.*
import android.util.Log
import com.example.app.Activity.AdminHomePageActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var regBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailField = findViewById(R.id.editTextTextEmailAddress)
        passwordField = findViewById(R.id.editTextTextPassword)
        loginButton = findViewById(R.id.LoginBtn)
        regBtn = findViewById(R.id.RegBtn)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check for Admin login first
            if (email == "admin@gmail.com" && password == "admin123") {
                loginUser("Admin")
            } else {
                validateUser(email, password)
            }
        }

        regBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateUser(email: String, password: String) {
        Log.d("LoginActivity", "Validating user with email: $email")

        // Validate as Student
        database.child("Students").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(studentSnapshot: DataSnapshot) {
                val student = findUserInSnapshot(studentSnapshot, email, password)

                if (student != null) {
                    storeInSharedPreferences(student, "Student")
                    loginUser("Student")
                } else {
                    // If not found as Student, validate as Tutor
                    database.child("Tutors").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(tutorSnapshot: DataSnapshot) {
                            val tutor = findUserInSnapshot(tutorSnapshot, email, password)

                            if (tutor != null) {
                                storeInSharedPreferences(tutor, "Tutor")
                                loginUser("Tutor")
                            } else {
                                Toast.makeText(applicationContext, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                                Log.e("LoginActivity", "Invalid login credentials for email: $email")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            handleDatabaseError(error)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                handleDatabaseError(error)
            }
        })
    }

    private fun findUserInSnapshot(snapshot: DataSnapshot, email: String, password: String): Map<String, Any>? {
        for (user in snapshot.children) {
            val dbEmail = user.child("Email").getValue(String::class.java)
            val dbPassword = user.child("Password").getValue(String::class.java)

            if (dbEmail == email && dbPassword == password) {
                return user.value as? Map<String, Any>
            }
        }
        return null
    }

    private fun storeInSharedPreferences(userData: Map<String, Any>, userType: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Common keys for all users
        editor.putString("user_name", userData["Name"] as? String)
        editor.putString("user_email", userData["Email"] as? String)
        editor.putInt("user_id", (userData["Id"] as? Long)?.toInt() ?: -1)
        editor.putString("user_username", userData["Username"] as? String)
        editor.putString("user_password", userData["Password"] as? String)

        if (userType == "Student") {
            editor.putString("user_major", userData["Major"] as? String)
            editor.putString("user_year", userData["Year"] as? String)
            editor.putString("profile_image", userData["ProfilePicture"] as? String)
        } else if (userType == "Tutor") {
            editor.putString("user_address", userData["Address"] as? String)
            editor.putString("user_biography", userData["Biography"] as? String)
            editor.putInt("user_experience", (userData["Experience"] as? Long)?.toInt() ?: 0)
            editor.putString("user_location", userData["Location"] as? String)
            editor.putString("user_mobile", userData["Mobile"] as? String)
            editor.putString("profile_image", userData["Picture"] as? String)
            editor.putFloat("user_rating", (userData["Rating"] as? Double)?.toFloat() ?: 0.0f)
            editor.putString("user_site", userData["Site"] as? String)
            editor.putString("user_special", userData["Special"] as? String)
            editor.putString("user_students", userData["Students"] as? String)
        }

        editor.putString("user_type", userType) // Store user type
        editor.apply()

        Log.d("LoginActivity", "All user data stored in SharedPreferences as $userType")
    }

    private fun loginUser(userType: String) {
        Toast.makeText(applicationContext, "Login Successful as $userType", Toast.LENGTH_SHORT).show()

        val intent = when (userType) {
            "Admin" -> Intent(this@LoginActivity, AdminHomePageActivity::class.java)
            "Tutor" -> Intent(this@LoginActivity, TutorHomePageActivity::class.java)
            else -> Intent(this@LoginActivity, HomeActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun handleDatabaseError(error: DatabaseError) {
        Toast.makeText(applicationContext, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
        Log.e("LoginActivity", "Database error: ${error.details}")
    }
}
