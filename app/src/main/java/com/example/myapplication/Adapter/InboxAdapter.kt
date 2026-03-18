package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.Domain.ChatModel
import com.example.myapplication.databinding.ItemInboxBinding

class InboxAdapter(
    private val chatList: List<ChatModel>,
    private val onItemClick: (ChatModel) -> Unit
) : RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    inner class InboxViewHolder(private val binding: ItemInboxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatModel) {
            binding.recipientName.text = chat.recipientName
            binding.lastMessage.text = chat.lastMessage
            binding.timestamp.text = android.text.format.DateFormat.format("hh:mm a", chat.timestamp)

            Glide.with(binding.root.context)
                .load(chat.profilePictureUrl)
                .placeholder(com.example.myapplication.R.drawable.ic_placeholder_profile)
                .into(binding.recipientProfileImage)

            binding.root.setOnClickListener { onItemClick(chat) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val binding = ItemInboxBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InboxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int = chatList.size
}
