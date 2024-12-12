package com.example.eyesight.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.eyesight.R

class LeDeviceListAdapter : BaseAdapter() {

    private val devices = mutableListOf<BluetoothDevice>()

    fun addDevice(device: BluetoothDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
        }
    }

    fun clear() {
        devices.clear()
    }

    override fun getCount(): Int {
        return devices.size
    }

    override fun getItem(position: Int): Any {
        return devices[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val device: BluetoothDevice = getItem(position) as BluetoothDevice

        if (convertView == null) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_bluetooth_device, parent, false)
        } else {
            view = convertView
        }

        val deviceNameTextView = view.findViewById<TextView>(R.id.device_name)
        val deviceAddressTextView = view.findViewById<TextView>(R.id.device_address)

        deviceNameTextView.text = device.name ?: "Unknown Device"
        deviceAddressTextView.text = device.address

        return view
    }
}
