package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.InboxAdapter
import com.example.myapplication.Domain.ChatModel
import com.google.firebase.database.*
import com.example.myapplication.R

class ChatInBoxActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var inboxAdapter: InboxAdapter
    private val chatList = mutableListOf<ChatModel>()

    private val TAG = "ChatInBoxActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_inbox)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        val userRole = sharedPreferences.getString("user_type", "")

        Log.d(TAG, "Retrieved User ID: $userId, Role: $userRole")

        if (userId == -1 || userRole.isNullOrEmpty()) {
            Log.e(TAG, "Error: User ID or Role not set in SharedPreferences.")
            Toast.makeText(this, "User data not found. Please log in again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // Back button logic
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            if (userRole == "Tutor") {
                val intent = Intent(this, TutorHomePageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            finish()
        }

        fetchUserChats(userId, userRole)
    }

    private fun fetchUserChats(userId: Int, userRole: String) {
        Log.d(TAG, "Fetching chats for user: $userId, role: $userRole")

        val chatsRef = FirebaseDatabase.getInstance().getReference("Chats")
        chatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (chatSnapshot in snapshot.children) {
                        val chatId = chatSnapshot.key ?: continue
                        val participants = chatSnapshot.child("participants").value as? Map<*, *> ?: continue

                        val isParticipant = if (userRole == "Tutor") {
                            participants["TutorId"]?.toString() == userId.toString()
                        } else {
                            participants["StudentId"]?.toString() == userId.toString()
                        }

                        if (isParticipant) {
                            val lastMessage = chatSnapshot.child("lastMessage").getValue(String::class.java)
                            val timestamp = chatSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                            val recipientId = if (userRole == "Tutor") {
                                participants["StudentId"]?.toString()?.toIntOrNull() // Convert to Int
                            } else {
                                participants["TutorId"]?.toString()?.toIntOrNull() // Convert to Int
                            }

                            if (recipientId != null) {
                                fetchRecipientDetails(chatId, recipientId, lastMessage, timestamp, userRole)
                            } else {
                                Log.e(TAG, "Recipient ID is null for chat ID: $chatId")
                            }
                        } else {
                            Log.d(TAG, "Skipping chat ID: $chatId, user is not a participant.")
                        }
                    }
                } else {
                    Log.d(TAG, "No chats found for the current user.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching chats: ${error.message}")
                Toast.makeText(this@ChatInBoxActivity, "Error fetching chats.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRecipientDetails(
        chatId: String,
        recipientId: Int, // Keep it as Int
        lastMessage: String?,
        timestamp: Long,
        userRole: String
    ) {
        Log.d(TAG, "Fetching recipient details for Recipient ID: $recipientId")

        val isStudent = userRole == "Tutor" // Tutor fetching Student details
        val userRef = if (isStudent) {
            FirebaseDatabase.getInstance().getReference("Students").child(recipientId.toString()) // Convert to String
        } else {
            FirebaseDatabase.getInstance().getReference("Tutors").child(recipientId.toString()) // Convert to String
        }

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val recipientName = snapshot.child("Name").getValue(String::class.java) ?: "Unknown User"
                    val profilePictureUrl = snapshot.child("ProfilePicture").getValue(String::class.java)
                        ?: snapshot.child("Picture").getValue(String::class.java)
                        ?: "https://example.com/default-profile.png"

                    Log.d(TAG, "Recipient Details - Name: $recipientName, Profile Picture: $profilePictureUrl")

                    // Add chat model to the list
                    val chat = ChatModel(
                        chatId,
                        recipientName,
                        profilePictureUrl,
                        lastMessage ?: "",
                        timestamp,
                        recipientId
                    )
                    chatList.add(chat)

                    // Update the RecyclerView adapter
                    if (!::inboxAdapter.isInitialized) {
                        inboxAdapter = InboxAdapter(chatList) { chat ->
                            val intent = Intent(this@ChatInBoxActivity, ChatRoomActivity::class.java).apply {
                                putExtra("chat_id", chat.chatId)
                                putExtra("recipient_name", chat.recipientName)
                                putExtra("recipient_image", chat.profilePictureUrl)
                                putExtra("recipient_id", chat.recipientId)

                            }
                            startActivity(intent)
                        }
                        Log.d(TAG, "ChatRoomActivity with Recipient ID transferred success: ${recipientId}")

                        recyclerView.adapter = inboxAdapter
                    } else {
                        inboxAdapter.notifyDataSetChanged()
                    }
                } else {
                    Log.e(TAG, "Recipient not found in the database for Recipient ID: $recipientId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching recipient details: ${error.message}")
                Toast.makeText(this@ChatInBoxActivity, "Error fetching user details", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
