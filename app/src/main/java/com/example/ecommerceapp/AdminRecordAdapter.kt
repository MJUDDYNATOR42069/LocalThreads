package com.example.ecommerceapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerceapp.databinding.ItemAdminRecordBinding

data class AdminRecord(
    val documentId: String,
    val title: String,
    val subtitle: String,
    val status: String,
    val actionOne: String = "",
    val actionTwo: String = "",
    val actionThree: String = ""
)

class AdminRecordAdapter(
    private var records: List<AdminRecord>,
    private val onAction: (AdminRecord, Int) -> Unit
) : RecyclerView.Adapter<AdminRecordAdapter.RecordViewHolder>() {

    inner class RecordViewHolder(
        val binding: ItemAdminRecordBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemAdminRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        val b = holder.binding

        b.tvRecordTitle.text = record.title
        b.tvRecordSubtitle.text = record.subtitle
        b.tvRecordStatus.text = "Status: ${record.status}"

        setupButton(b.btnActionOne, record.actionOne, record, 1)
        setupButton(b.btnActionTwo, record.actionTwo, record, 2)
        setupButton(b.btnActionThree, record.actionThree, record, 3)
    }

    private fun setupButton(
        button: android.widget.Button,
        label: String,
        record: AdminRecord,
        action: Int
    ) {
        if (label.isBlank()) {
            button.visibility = View.GONE
        } else {
            button.visibility = View.VISIBLE
            button.text = label
            button.setOnClickListener {
                onAction(record, action)
            }
        }
    }

    override fun getItemCount(): Int = records.size

    fun updateRecords(newRecords: List<AdminRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}