package com.example.myapplication.Domain

// Appointment Domain which will store things in database
data class Appointment(
    val studentId: Int=-1,
    val tutorId: Int = -1,
    val date: String="",
    val time: String="",
    val studentName: String="",
    val tutorName: String=""
)

