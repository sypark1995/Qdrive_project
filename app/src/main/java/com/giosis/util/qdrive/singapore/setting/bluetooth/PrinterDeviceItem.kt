package com.giosis.util.qdrive.singapore.setting.bluetooth

class PrinterDeviceItem internal constructor(var deviceNm: String,
                                             var deviceAddress: String, // Pairing 발견
                                             var isFound: Boolean, // Connected
                                             var isConnected: Boolean)