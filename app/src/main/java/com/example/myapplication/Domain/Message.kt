package com.example.myapplication.Domain

data class Message(
    val senderId: Int = -1,
    val senderRole: String = "",
    val recipientId: Int = -1,
    val text: String = "",
    val timestamp: Long = 0L,
    var isSent: Boolean = false // Add this property
)

