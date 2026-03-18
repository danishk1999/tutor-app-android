package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Activity.DetailActivity
import com.example.myapplication.Domain.Appointment
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCalendarBinding
import com.google.firebase.database.FirebaseDatabase
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    // Declare tutorId and tutorName as mutable variables
    private var tutorId: Int = -1
    private var tutorName: String? = null
    private lateinit var backBtn : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false) // For edge-to-edge display
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        backBtn = findViewById(R.id.backBtn)

        backBtn.setOnClickListener {
            val intent = Intent(this@CalendarActivity, HomeActivity::class.java)
            startActivity(intent)
        }

        // Retrieve tutor details from Intent first, then SharedPreferences if missing
        tutorId = intent.getIntExtra("tutorId", -1)
        tutorName = intent.getStringExtra("tutorName")

        if (tutorId == -1 || tutorName == null) {
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            tutorId = sharedPreferences.getInt("tutorId", -1)
            tutorName = sharedPreferences.getString("tutorName", null)
        }

        if (tutorId == -1 || tutorName == null) {
            Log.e("CalendarActivity", "Error: tutor_id or tutor_name is missing")
            Toast.makeText(this, "Error: Missing tutor details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupPickers()

        binding.nextButton.setOnClickListener {
            val hour = binding.numberPickerHour.value
            val min = binding.numberPickerMin.value
            val amPm = if (binding.numberPickerAm.value == 0) "AM" else "PM"
            selectedTime = String.format("%02d:%02d %s", hour, min, amPm)

            saveAppointment(selectedDate, selectedTime)

            Toast.makeText(
                this,
                "Your Appointment is booked for $selectedDate at $selectedTime",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this@CalendarActivity, DetailActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            intent.putExtra("selectedTime", selectedTime)
            startActivity(intent)
        }
    }

    private fun saveAppointment(date: String, time: String) {
        val studentId = getStudentId()
        val studentName = getStudentName()

        if (studentId == -1 || tutorId == -1 || studentName.isNullOrEmpty() || tutorName.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Missing information for appointment", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val appointment = Appointment(
                studentId = studentId,
                studentName = studentName,
                tutorId = tutorId,
                tutorName = tutorName!!,
                date = date,
                time = time
            )

            val database = FirebaseDatabase.getInstance().getReference("Appointments")
            val appointmentId = database.push().key ?: throw Exception("Error generating appointment ID")

            database.child(appointmentId).setValue(appointment)
                .addOnSuccessListener {
                    Log.d("CalendarActivity", "Appointment saved successfully")
                    Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.e("CalendarActivity", "Failed to save appointment", it)
                    Toast.makeText(this, "Error: Could not save appointment. Try again later.", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            Log.e("CalendarActivity", "Firebase save error", e)
        }
    }

    private fun setupPickers() {
        binding.numberPickerHour.minValue = 1
        binding.numberPickerHour.maxValue = 12

        binding.numberPickerMin.minValue = 0
        binding.numberPickerMin.maxValue = 59
        binding.numberPickerMin.setFormatter { value -> String.format("%02d", value) }

        binding.numberPickerAm.minValue = 0
        binding.numberPickerAm.maxValue = 1
        binding.numberPickerAm.displayedValues = arrayOf("AM", "PM")

        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val localDate = LocalDate.of(year, month + 1, day)
            selectedDate = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            binding.textView.text = selectedDate
        }
    }


    private fun getStudentName(): String? {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("user_name", null)
    }

    private fun getStudentId(): Int {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }
}
