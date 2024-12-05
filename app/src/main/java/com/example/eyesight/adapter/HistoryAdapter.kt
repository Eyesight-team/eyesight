package com.example.eyesight.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eyesight.HistoryItem
import com.example.eyesight.R
import java.util.Date

class HistoryAdapter(
    private val items: List<HistoryItem>,
    private val onItemClicked: (String, String, String, String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val totalTv: TextView = itemView.findViewById(R.id.result_total)
        val lulusTv: TextView = itemView.findViewById(R.id.result_lulus)
        val gagalTv: TextView = itemView.findViewById(R.id.result_failed)
        val dateTv: TextView = itemView.findViewById(R.id.date)

        val totalTitleTextView: TextView = itemView.findViewById(R.id.tv_total)
        val lulusTitleTextView: TextView = itemView.findViewById(R.id.tv_lulus)
        val gagalTitleTextView: TextView = itemView.findViewById(R.id.tv_failed)

        init {
            itemView.setOnClickListener {
                // Mengambil nilai dari TextView dan meneruskan ke callback
                val total = totalTv.text.toString()
                val lulus = lulusTv.text.toString()
                val gagal = gagalTv.text.toString()
                val date = dateTv.text.toString()
                onItemClicked(total, lulus, gagal, date)
            }
        }
    }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_result, parent, false)
            return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.totalTv.text = item.total
        holder.lulusTv.text = item.lulus
        holder.gagalTv.text = item.failed
        holder.dateTv.text = item.date

        holder.totalTitleTextView.text = "Total"
        holder.lulusTitleTextView.text = "Lulus"
        holder.gagalTitleTextView.text = "Gagal"
    }

    override fun getItemCount(): Int = items.size

}