package com.anangkur.websocketplayground

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anangkur.websocketplayground.databinding.ItemMessageBinding
import com.anangkur.websocketplayground.model.Message

class ChatMessageAdapter : RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>() {

    private val items = arrayListOf<Message>()

    inner class ViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Message) {
            if (item.isFromSender) showMessageFromSender(item.message)
            else showMessageFromReceiver(item.message)
        }

        private fun showMessageFromSender(message: String) {
            binding.root.displayedChild = 0
            binding.tvMessageSender.text = message
        }

        private fun showMessageFromReceiver(message: String) {
            binding.root.displayedChild = 1
            binding.tvMessageReceiver.text = message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(item: Message) {
        this.items.add(item)
        notifyDataSetChanged()
    }
}

