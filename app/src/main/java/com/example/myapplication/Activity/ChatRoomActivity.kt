package com.example.myapplication.Activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.Adapter.MessageAdapter
import com.example.myapplication.Domain.Message
import com.example.myapplication.databinding.ActivityChatRoomBinding
import com.google.firebase.database.*

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()
    private val loadedMessageIds = mutableSetOf<String>() // To track loaded messages
    private lateinit var chatId: String
    private var currentUserId: Int = -1
    private var currentUserRole: String = ""
    private var recipientId: Int = -1
    private var recipientName: String = ""

    private val TAG = "ChatRoomActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        chatId = intent.getStringExtra("chat_id") ?: ""
        recipientName = intent.getStringExtra("recipient_name") ?: "Unknown"
        recipientId = intent.getIntExtra("recipient_id", -1)
        val recipientImage = intent.getStringExtra("recipient_image")?: ""



        if (recipientId == -1) {
            Log.e(TAG, "Invalid Recipient ID. Exiting.")
            Toast.makeText(this, "Error: Invalid recipient.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "Recipient Name: $recipientName, Recipient ID: $recipientId")

        // Toolbar setup
        binding.recipientName.text = recipientName
        binding.backButton.setOnClickListener { finish() }

        // Get current user ID and role from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUserId = sharedPreferences.getInt("user_id", -1)
        currentUserRole = sharedPreferences.getString("user_type", "") ?: ""

        if (currentUserId == -1 || currentUserRole.isEmpty()) {
            Log.e(TAG, "Invalid User ID or Role. Exiting.")
            Toast.makeText(this, "Error: Invalid user.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "Current User ID: $currentUserId, Role: $currentUserRole")

        // Initialize RecyclerView
        setupRecyclerView()

        // Check if chat exists
        checkOrCreateChat()

        // Send button functionality
        binding.sendButton.setOnClickListener {
            val messageText = binding.inputMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.inputMessage.text.clear()
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messageList, currentUserId)
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recyclerViewMessages.adapter = messageAdapter
        Log.d(TAG, "RecyclerView setup complete.")
    }

    private fun checkOrCreateChat() {
        val chatRef = FirebaseDatabase.getInstance().getReference("Chats")
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var chatFound = false

                for (chatSnapshot in snapshot.children) {
                    val participants = chatSnapshot.child("participants").value as? Map<*, *> ?: continue
                    val studentId = participants["StudentId"]?.toString()?.toInt()
                    val tutorId = participants["TutorId"]?.toString()?.toInt()

                    Log.d(TAG, "Checking chat: StudentId=$studentId, TutorId=$tutorId")

                    if ((studentId == currentUserId && tutorId == recipientId) ||
                        (tutorId == currentUserId && studentId == recipientId)
                    ) {
                        chatId = chatSnapshot.key!!
                        chatFound = true
                        Log.d(TAG, "Chat found with ID: $chatId")
                        break
                    }
                }

                if (!chatFound) {
                    Log.d(TAG, "No chat found, creating a new chat.")
                    val newChatRef = chatRef.push()
                    chatId = newChatRef.key!!
                    val participants = mapOf(
                        "StudentId" to minOf(currentUserId, recipientId),
                        "TutorId" to maxOf(currentUserId, recipientId)
                    )
                    newChatRef.child("participants").setValue(participants).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "New chat created with ID: $chatId")
                        } else {
                            Log.e(TAG, "Error creating chat: ${task.exception}")
                        }
                    }
                }

                // Load messages for the chat
                loadMessages()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching chat: ${error.message}")
                Toast.makeText(this@ChatRoomActivity, "Error fetching chat", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadMessages() {
        val chatRef = FirebaseDatabase.getInstance().getReference("Chats").child(chatId).child("Messages")
        Log.d(TAG, "Loading messages for chat ID: $chatId")

        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java) ?: return

                // Debug logs for roles and IDs
                Log.d(TAG, "Message: ${message.text}, SenderRole: ${message.senderRole}, SenderId: ${message.senderId}, RecipientId: ${message.recipientId}")

                // Determine if this is a sent or received message
                val isSent = when (currentUserRole) {
                    "Student" -> message.senderRole == "Student"
                    "Tutor" -> message.senderRole == "Tutor"
                    else -> false
                }

                // Append message with its direction
                message.isSent = isSent
                messageList.add(message)

                // Sort by timestamp to ensure chronological order
                messageList.sortBy { it.timestamp }

                // Notify adapter and scroll to the latest message
                messageAdapter.notifyDataSetChanged()
                binding.recyclerViewMessages.scrollToPosition(messageList.size - 1)

                Log.d(TAG, "Message added: ${message.text}, Sent: $isSent")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error loading messages: ${error.message}")
            }
        })
    }


    private fun sendMessage(messageText: String) {
        val timestamp = System.currentTimeMillis()
        val message = Message(
            senderId = currentUserId,
            senderRole = currentUserRole,
            recipientId = recipientId,
            text = messageText,
            timestamp = timestamp
        )

        val chatRef = FirebaseDatabase.getInstance().getReference("Chats").child(chatId).child("Messages")
        chatRef.push().setValue(message).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Message sent: $messageText")
            } else {
                Log.e(TAG, "Error sending message: ${task.exception}")
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
