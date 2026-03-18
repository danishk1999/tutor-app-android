package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Activity.StudentRegActivity
import com.example.myapplication.R
import com.example.myapplication.TutorRegActivity
import com.google.android.gms.common.SignInButton

class RegistrationActivity : AppCompatActivity() {

    private lateinit var StudentButton: Button
    private lateinit var TutorButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        StudentButton = findViewById<Button>(R.id.StudentButton)
        TutorButton = findViewById<Button>(R.id.TutorButton)

        StudentButton.setOnClickListener{
            val intent = Intent(this@RegistrationActivity,StudentRegActivity::class.java)
            startActivity(intent)
            finish()
        }
        TutorButton.setOnClickListener{
            val intent = Intent(this@RegistrationActivity, TutorRegActivity::class.java)
            startActivity(intent)
            finish()
        }




    }
}
