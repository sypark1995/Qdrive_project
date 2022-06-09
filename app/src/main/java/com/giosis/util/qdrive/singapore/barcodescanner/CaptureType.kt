package com.giosis.util.qdrive.singapore.barcodescanner

// capture activity 에 들어가는 타입.....
//
object CaptureType {
    const val CONFIRM_MY_DELIVERY_ORDER = "MDA" // Main & SCAN > Confirm my delivery order
    const val CHANGE_DELIVERY_DRIVER = "CDR" // Main > Change Delivery Driver
    const val PICKUP_CNR = "CNR" // SCAN > Pickup C&R Parcels
    const val PICKUP_SCAN_ALL = "PSA" // LIST > Start to Scan
    const val PICKUP_ADD_SCAN = "PAS" // LIST > Today Done > Add Scan

    const val DELIVERY_DONE = "DDONE"
    const val PICKUP_TAKE_BACK = "PTB" // LIST > Today Done > Take Back
    const val OUTLET_PICKUP_SCAN = "OPS" // LIST > Outlet Pickup Scan
    const val SELF_COLLECTION = "SEC" // SCAN > Self-Collection'
}