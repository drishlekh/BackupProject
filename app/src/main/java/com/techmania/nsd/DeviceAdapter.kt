package com.techmania.nsd

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class DeviceAdapter(private val deviceNames: MutableList<String>) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceNameTextView: TextView = itemView.findViewById(R.id.deviceNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.deviceNameTextView.text = deviceNames[position]
    }

    override fun getItemCount(): Int {
        return deviceNames.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDevices(newDeviceNames: List<String>) {
        deviceNames.clear()
        deviceNames.addAll(newDeviceNames)
        notifyDataSetChanged()

    }
}
















