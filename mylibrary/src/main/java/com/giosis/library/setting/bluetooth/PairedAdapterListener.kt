package com.giosis.library.setting.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket

interface PairedAdapterListener {
    fun cancelDiscovery()
    fun connectDevice(position: Int, address: String, name: String): Boolean
    fun getRemoteDevice(address: String): BluetoothDevice?
    fun getConnectedPrinterAddress(): String?
    fun closeSocket()
    fun getSocket(): BluetoothSocket?
}