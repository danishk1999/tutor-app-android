package com.example.myapplication.Activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Domain.Student
import com.example.myapplication.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class StudentRegActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_reg)

        // Initialize Firebase Database and Storage
        databaseReference = FirebaseDatabase.getInstance().getReference("Students")
        storageReference = FirebaseStorage.getInstance().getReference("ProfilePictures")

        // Get Views
        val emailEditText: EditText = findViewById(R.id.editEmail)
        val passwordEditText: EditText = findViewById(R.id.editPassword)
        val nameEditText: EditText = findViewById(R.id.editName)
        val majorEditText: EditText = findViewById(R.id.editMajor)
        val yearEditText: EditText = findViewById(R.id.editYear)
        val usernameEditText: EditText = findViewById(R.id.editUsername)
        val selectPictureButton: Button = findViewById(R.id.selectPictureBtn)
        val registerButton: Button = findViewById(R.id.RegBtn)

        // Handle Picture Selection Button
        selectPictureButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Handle Registration Button Click
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()
            val major = majorEditText.text.toString()
            val year = yearEditText.text.toString()
            val username = usernameEditText.text.toString()

            // Validate Input
            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || major.isEmpty() || year.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select a profile picture", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Generate a new ID and Upload Picture
            generateNewStudentId { newId ->
                uploadPictureAndAddStudent(newId, email, password, name, major, year, username)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            Toast.makeText(this, "Picture selected successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateNewStudentId(callback: (Int) -> Unit) {
        databaseReference.get().addOnSuccessListener { snapshot ->
            val maxId = snapshot.children.maxOfOrNull { it.key?.toIntOrNull() ?: 0 } ?: 0
            val newId = maxId + 1
            callback(newId)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to generate student ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPictureAndAddStudent(
        newId: Int,
        email: String,
        password: String,
        name: String,
        major: String,
        year: String,
        username: String
    ) {
        val fileName = "profile_${newId}.jpg"
        val fileRef = storageReference.child("ProfilePictures/$fileName")

        // Upload the picture
        selectedImageUri?.let { uri ->
            fileRef.putFile(uri)
                .addOnSuccessListener {
                    // Get the download URL
                    fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val profilePictureUrl = downloadUri.toString()
                        addStudentToDatabase(newId, email, password, name, major, year, username, profilePictureUrl)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload picture: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addStudentToDatabase(
        newId: Int,
        email: String,
        password: String,
        name: String,
        major: String,
        year: String,
        username: String,
        profilePictureUrl: String
    ) {
        val student = mapOf(
            "Email" to email,
            "Password" to password,
            "Name" to name,
            "Id" to newId, // Store ID as an integer
            "Major" to major,
            "Year" to year,
            "Username" to username,
            "ProfilePicture" to profilePictureUrl
        )

        databaseReference.child(newId.toString()).setValue(student)
            .addOnSuccessListener {
                Toast.makeText(this, "Student registered successfully!", Toast.LENGTH_SHORT).show()
                navigateToLoginActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to register student: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Finish current activity so the user cannot navigate back
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }
}
