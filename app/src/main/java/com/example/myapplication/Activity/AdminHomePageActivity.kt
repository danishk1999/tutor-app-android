package com.example.app.Activity

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.firebase.database.*

class AdminHomePageActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var linearLayoutStudents: LinearLayout
    private lateinit var linearLayoutTutors: LinearLayout
    private lateinit var buttonRemoveStudents: Button
    private lateinit var buttonRemoveTutors: Button
    private val studentsMap = mutableMapOf<String, String>() // Map of ID to Name
    private val tutorsMap = mutableMapOf<String, String>()   // Map of ID to Name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home_page)

        database = FirebaseDatabase.getInstance().reference
        linearLayoutStudents = findViewById(R.id.linearLayoutStudents)
        linearLayoutTutors = findViewById(R.id.linearLayoutTutors)
        buttonRemoveStudents = findViewById(R.id.buttonRemoveStudents)
        buttonRemoveTutors = findViewById(R.id.buttonRemoveTutors)

        // Load students and tutors from Firebase
        loadStudents()
        loadTutors()

        // Remove selected students
        buttonRemoveStudents.setOnClickListener {
            removeSelectedStudents()
        }

        // Remove selected tutors
        buttonRemoveTutors.setOnClickListener {
            removeSelectedTutors()
        }
    }

    // Load students from Firebase and display their names
    private fun loadStudents() {
        database.child("Students").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                studentsMap.clear()
                linearLayoutStudents.removeAllViews() // Clear previous views

                for (studentSnapshot in snapshot.children) {
                    val studentId = studentSnapshot.key ?: continue
                    val studentName = studentSnapshot.child("Name").value as? String ?: "Unknown"
                    studentsMap[studentId] = studentName

                    val studentTextView = TextView(this@AdminHomePageActivity).apply {
                        text = studentName
                        setOnClickListener {
                            toggleSelection(this, studentId)
                        }
                    }
                    linearLayoutStudents.addView(studentTextView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminHomePageActivity, "Failed to load students", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Load tutors from Firebase and display their names
    private fun loadTutors() {
        database.child("Tutors").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tutorsMap.clear()
                linearLayoutTutors.removeAllViews() // Clear previous views

                for (tutorSnapshot in snapshot.children) {
                    val tutorId = tutorSnapshot.key ?: continue
                    val tutorName = tutorSnapshot.child("Name").value as? String ?: "Unknown"
                    tutorsMap[tutorId] = tutorName

                    val tutorTextView = TextView(this@AdminHomePageActivity).apply {
                        text = tutorName
                        setOnClickListener {
                            toggleSelection(this, tutorId)
                        }
                    }
                    linearLayoutTutors.addView(tutorTextView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminHomePageActivity, "Failed to load tutors", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Toggle selection of a student or tutor
    private fun toggleSelection(textView: TextView, id: String) {
        if (textView.alpha == 1.0f) {
            textView.alpha = 0.5f // Mark as selected (fade it)
        } else {
            textView.alpha = 1.0f // Unmark selection
        }
    }

    // Remove selected students from Firebase
    private fun removeSelectedStudents() {
        val selectedStudents = studentsMap.filterKeys { studentId ->
            val index = studentsMap.keys.indexOf(studentId)
            linearLayoutStudents.getChildAt(index).alpha == 0.5f
        }
        for (studentId in selectedStudents.keys) {
            database.child("Students").child(studentId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Student removed successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to remove student", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Remove selected tutors from Firebase
    private fun removeSelectedTutors() {
        val selectedTutors = tutorsMap.filterKeys { tutorId ->
            val index = tutorsMap.keys.indexOf(tutorId)
            linearLayoutTutors.getChildAt(index).alpha == 0.5f
        }
        for (tutorId in selectedTutors.keys) {
            database.child("Tutors").child(tutorId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Tutor removed successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to remove tutor", Toast.LENGTH_SHORT).show()
                }
        }
    }
}