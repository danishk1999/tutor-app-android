package com.example.myapplication

import android.R.attr.rating
import android.app.Activity
import android.content.Intent
import android.media.Rating
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Activity.LoginActivity
import com.example.myapplication.Activity.StudentRegActivity.Companion.PICK_IMAGE_REQUEST
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.example.myapplication.Domain.TutorRegister
import com.google.android.gms.common.api.ResultCallback
import com.google.firebase.database.DatabaseReference
import java.util.*
import kotlin.math.exp

class TutorRegActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutor_reg)

        // Initialize Firebase Database and Storage
        databaseReference = FirebaseDatabase.getInstance().getReference("Tutors")
        storageReference = FirebaseStorage.getInstance().getReference("ProfilePictures")

        // Get Views
        val addressEditText: EditText = findViewById(R.id.editAddress)
        val biographyEditText: EditText = findViewById(R.id.editBiography)
        val emailEditText: EditText = findViewById(R.id.editEmail)
        val experienceEditText: EditText = findViewById(R.id.editExp)
        val locationEditText: EditText = findViewById(R.id.editLocation)
        val mobileEditText: EditText = findViewById(R.id.editMobile)
        val nameEditText: EditText = findViewById(R.id.editName)
        val passwordEditText: EditText = findViewById(R.id.editPassword)
        val selectPictureButton: Button = findViewById(R.id.selectPictureBtn)
        val siteEditText: EditText = findViewById(R.id.editSite)
        val specialEditText: EditText = findViewById(R.id.editSpecial)
        val usernameEditText: EditText = findViewById(R.id.editUsername)
        val registerButton: Button = findViewById(R.id.RegBtn)

        // Handle Picture Selection Button
        selectPictureButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Handle registration button click
        registerButton.setOnClickListener {
            val address = addressEditText.text.toString()
            val biography = biographyEditText.text.toString()
            val email = emailEditText.text.toString()
            val experience = experienceEditText.text.toString().toInt() // EditText to Str to int
            val location = locationEditText.text.toString()
            val mobile = mobileEditText.text.toString()
            val name = nameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val site = siteEditText.text.toString()
            val special = specialEditText.text.toString()
            val username = usernameEditText.text.toString()
            val Rating = 0.0 // Default value for new tutors

            // Validate Input
            if (address.isEmpty() || biography.isEmpty() || email.isEmpty() || location.isEmpty() || mobile.isEmpty() || name.isEmpty() || password.isEmpty() || site.isEmpty() || special.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select a profile picture", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Generate a new ID and Upload Picture
            generateNewTutorId { newId ->
                uploadPictureAndAddTutor(newId, address, biography, email, experience, location, mobile, name, password, site, special, username, Rating)
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

    private fun generateNewTutorId(callback: (Int) -> Unit) {
        databaseReference.get().addOnSuccessListener { snapshot ->
            val maxId = snapshot.children.maxOfOrNull { it.key?.toIntOrNull() ?: 0 } ?: 0
            val newId = maxId + 1
            callback(newId)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to generate Tutor ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPictureAndAddTutor(
        newId: Int,
        address: String,
        biography: String,
        email: String,
        experience: Int,
        location: String,
        mobile: String,
        name: String,
        password: String,
        site: String,
        special: String,
        username: String,
        Rating: Double,
        ){
            val fileName = "profile_${newId}.jpg"
            val fileRef = storageReference.child("ProfilePictures/$fileName")

            //Upload pic
            selectedImageUri?.let { uri ->
                fileRef.putFile(uri)
                    .addOnSuccessListener {
                        // Get download URL
                        fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val profilePictureUri = downloadUri.toString()
                            addTutorToDatabase(newId, address, biography, email, experience, location, mobile, name, password, site, special, username, profilePictureUri, Rating)
                        }
                    }
            }
        }
    private fun addTutorToDatabase(
        newId: Int,
        address: String,
        biography: String,
        email: String,
        experience: Int,
        location: String,
        mobile: String,
        name: String,
        password: String,
        site: String,
        special: String,
        username: String,
        profilePictureUrl: String,
        Rating: Double,
    ) {
        val tutor = mapOf(
            "ID" to newId,
            "Address" to address,
            "Biography" to biography,
            "Email" to email,
            "Experience" to experience,
            "Location" to location,
            "Mobile" to mobile,
            "Name" to name,
            "Password" to password,
            "Site" to site,
            "Special" to special,
            "Username" to username,
            "ProfilePicture" to profilePictureUrl,
            "Rating" to Rating,
        )

        databaseReference.child(newId.toString()).setValue(tutor)
            .addOnSuccessListener {
                Toast.makeText(this, "Tutor registered successfully!", Toast.LENGTH_SHORT).show()
                navigateToLoginActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to register tutor: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
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




