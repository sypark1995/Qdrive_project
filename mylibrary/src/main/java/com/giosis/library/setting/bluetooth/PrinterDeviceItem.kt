package com.giosis.library.setting.bluetooth

class PrinterDeviceItem internal constructor(var deviceNm: String,
                                             var deviceAddress: String, // Pairing 발견
                                             var isFound: Boolean, // Connected
                                             var isConnected: Boolean)