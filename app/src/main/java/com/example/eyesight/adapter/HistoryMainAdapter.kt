package com.example.eyesight.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eyesight.HistoryItem
import com.example.eyesight.R
import com.example.eyesight.data.response.HistoryResponseItem
import java.util.Date

class HistoryMainAdapter(
    private var items: List<HistoryResponseItem>,
    private val onItemClicked: (String, String, String, String, String) -> Unit
) : RecyclerView.Adapter<HistoryMainAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id: TextView = itemView.findViewById(R.id.result_id)
        val feasibilityTest: TextView = itemView.findViewById(R.id.result_feasibility_test)
        val descFeasibilityTest: TextView = itemView.findViewById(R.id.desc_feasibility_test)
        val confidenceLevel: TextView = itemView.findViewById(R.id.result_confidence_level)
        val date: TextView = itemView.findViewById(R.id.result_date)

        val idTv: TextView = itemView.findViewById(R.id.tv_id)
        val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)
        val tvFeasibiityTest: TextView = itemView.findViewById(R.id.tv_feasibility_test)
        val tvConfidence: TextView = itemView.findViewById(R.id.tv_confidence)
        val icFeasibilityTest: ImageView = itemView.findViewById(R.id.ic_feasibility_test)

        init {
            itemView.setOnClickListener {
                val id = id.text.toString()
                val feasibilityTest = feasibilityTest.text.toString()
                val descFeasibilityTest = descFeasibilityTest.text.toString()
                val confidenceLevel = confidenceLevel.text.toString()
                val date = date.text.toString()
                onItemClicked(id, feasibilityTest, descFeasibilityTest, confidenceLevel, date)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_result_main, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.id.text = item.id.toString()
        holder.feasibilityTest.text = "${item.feasibilityTest}%"
        holder.descFeasibilityTest.text = item.descFeasibility
        holder.confidenceLevel.text = item.confidence
        holder.date.text = item.date

        holder.idTv.text = "ID"
        holder.tvItemName.text = "Nama Barang"
        holder.tvFeasibiityTest.text = "Uji Kelayakan"
        holder.tvConfidence.text = "Confidence Level"

        if (item.descFeasibility.equals("Gagal", ignoreCase = true)) {
            holder.icFeasibilityTest.setImageResource(R.drawable.failed_ic)
        } else {
            holder.icFeasibilityTest.setImageResource(R.drawable.lulus_ic)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<HistoryResponseItem>) {
        items = newItems
        notifyDataSetChanged()
    }

}