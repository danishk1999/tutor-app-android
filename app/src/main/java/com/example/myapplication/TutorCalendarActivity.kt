package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Decorators.AppointmentDecorator
import com.example.myapplication.Domain.Appointment
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityUserCalendarBinding
import com.google.firebase.database.*
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import com.jakewharton.threetenabp.AndroidThreeTen

class TutorCalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserCalendarBinding
    private lateinit var database: DatabaseReference
    private var userAppointments = mutableSetOf<LocalDate>()
    private lateinit var appointmentDetailsView: View
    private lateinit var professorName: TextView
    private lateinit var appointmentDate: TextView
    private lateinit var appointmentTime: TextView
    private lateinit var closeButton: View
    private lateinit var backButton: View
    private lateinit var cancelButton: View
    private lateinit var yesButton: View
    private lateinit var noButton: View





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)

        binding = ActivityUserCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inflate user_appointment.xml and add it to the main layout
        appointmentDetailsView = LayoutInflater.from(this).inflate(R.layout.user_appointment, binding.root, false)
        binding.root.addView(appointmentDetailsView)

        // Find views in the inflated layout
        professorName = appointmentDetailsView.findViewById(R.id.professorName)
        appointmentDate = appointmentDetailsView.findViewById(R.id.appointmentDate)
        appointmentTime = appointmentDetailsView.findViewById(R.id.appointmentTime)
        closeButton = appointmentDetailsView.findViewById(R.id.closeButton)
        backButton = binding.root.findViewById(R.id.bckbtn) // Ensure backButton is present in XML
        cancelButton = findViewById(R.id.cancelButton)
        yesButton = findViewById(R.id.yesButton)
        noButton = findViewById(R.id.noButton)


        // Initially hide the appointment details view
        appointmentDetailsView.visibility = View.GONE

        database = FirebaseDatabase.getInstance().reference.child("Appointments")
        fetchUserAppointments()

        val calendarView: MaterialCalendarView = findViewById(R.id.calendarView)
        calendarView.setOnDateChangedListener { _, date, _ ->
            val selectedDate = LocalDate.of(date.year, date.month + 1, date.day)
            showAppointmentDetails(selectedDate)
        }

        // Set up back button click listener
        backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }


    }

    private fun fetchUserAppointments() {
        val studentId = getStudentId()
        database.orderByChild("studentId").equalTo(studentId.toDouble()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userAppointments.clear()
                for (appointmentSnapshot in snapshot.children) {
                    val appointment = appointmentSnapshot.getValue(Appointment::class.java)
                    val appointmentDate = appointment?.date?.let { parseDate(it) }
                    appointmentDate?.let {
                        userAppointments.add(it)
                    }
                }
                updateCalendarDecorator()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TutorCalendarActivity, "Failed to fetch appointments", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseDate(dateString: String): LocalDate? {
        val formats = listOf("dd-MM-yyyy", "yyyy-MM-dd")
        for (format in formats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(format)
                return LocalDate.parse(dateString, formatter)
            } catch (e: Exception) {
                // Ignore and try the next format
            }
        }
        return null
    }

    private fun updateCalendarDecorator() {
        val calendarView: MaterialCalendarView = findViewById(R.id.calendarView)
        val appointmentDecorator = AppointmentDecorator(this@TutorCalendarActivity, userAppointments)
        calendarView.addDecorator(appointmentDecorator)
    }

    private fun showAppointmentDetails(selectedDate: LocalDate) {
        val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        database.orderByChild("date").equalTo(formattedDate).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var appointmentFound = false
                    for (appointmentSnapshot in snapshot.children) {
                        val appointment = appointmentSnapshot.getValue(Appointment::class.java)
                        if (appointment != null) {
                            professorName.text = appointment.tutorName
                            appointmentDate.text = "Date: ${appointment.date}"
                            appointmentTime.text = "Time: ${appointment.time}"
                            appointmentDetailsView.visibility = View.VISIBLE
                            appointmentFound = true
                        }
                    }
                    if (!appointmentFound) {
                        appointmentDetailsView.visibility = View.GONE
                        Toast.makeText(this@TutorCalendarActivity, "No appointments on this date", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    appointmentDetailsView.visibility = View.GONE
                    Toast.makeText(this@TutorCalendarActivity, "No appointments on this date", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TutorCalendarActivity, "Failed to fetch appointment details", Toast.LENGTH_SHORT).show()
            }
        })

        closeButton.setOnClickListener {
            appointmentDetailsView.visibility = View.GONE
        }
        cancelButton.setOnClickListener {
            if (noButton.visibility == View.GONE && yesButton.visibility == View.GONE) {
                noButton.visibility = View.VISIBLE
                yesButton.visibility = View.VISIBLE
            } else {
                noButton.visibility = View.GONE
                yesButton.visibility = View.GONE
            }
        }

        yesButton.setOnClickListener {
            // Cancel the appointment (delete from Firebase)
            val selectedDate = appointmentDate.text.toString().removePrefix("Date: ").trim()
            val studentId = getStudentId()
            database.orderByChild("date").equalTo(selectedDate).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (appointmentSnapshot in snapshot.children) {
                        val appointment = appointmentSnapshot.getValue(Appointment::class.java)
                        if (appointment?.studentId == studentId) {
                            appointmentSnapshot.ref.removeValue() // Delete the appointment
                            Toast.makeText(this@TutorCalendarActivity, "Appointment canceled", Toast.LENGTH_SHORT).show()
                        }

                    }
                    // Hide the "Yes" and "No" buttons after deletion
                    yesButton.visibility = View.GONE
                    noButton.visibility = View.GONE
                    appointmentDetailsView.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TutorCalendarActivity, "Failed to cancel the appointment", Toast.LENGTH_SHORT).show()
                }
            })
        }

        noButton.setOnClickListener {
            // Just hide the "Yes" and "No" buttons
            yesButton.visibility = View.GONE
            noButton.visibility = View.GONE
        }


    }

    private fun getStudentId(): Int {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)
    }
}
