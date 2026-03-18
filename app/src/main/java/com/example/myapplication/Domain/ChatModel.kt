package com.example.myapplication.Domain

data class ChatModel(
    val chatId: String,
    val recipientName: String,
    val profilePictureUrl: String,
    val lastMessage: String,
    val timestamp: Long,
    val recipientId: Int // Add this field if it's not already present
)
