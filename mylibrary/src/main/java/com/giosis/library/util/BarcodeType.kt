package com.giosis.library.util

object BarcodeType {
    const val TYPE_DELIVERY = "D"
    const val TYPE_PICKUP = "P"
    const val TYPE_CNR = "CnR"

    const val PICKUP_ZERO_QTY = "PZQ"
    const val CONFIRM_MY_DELIVERY_ORDER = "MDA" // Main & SCAN > Confirm my delivery order
    const val CHANGE_DELIVERY_DRIVER = "CDR" // Main > Change Delivery Driver


    const val DELIVERY_START = "D3"
    const val DELIVERY_DONE = "D4" // SCAN > Delivery Done
    const val DELIVERY_FAIL = "DX"
    const val PICKUP_REASSIGN = "RE"
    const val PICKUP_FAIL = "PF"
    const val PICKUP_CANCEL = "PX"
    const val PICKUP_DONE = "P3"
    const val PICKUP_CONFIRM = "P2"
    const val RETURN_FAIL = "RF"


    const val PICKUP_CNR = "CNR" // SCAN > Pickup C&R Parcels
    const val PICKUP_SCAN_ALL = "PSA" // LIST > Start to Scan
    const val PICKUP_ADD_SCAN = "PAS" // LIST > Today Done > Add Scan
    const val PICKUP_TAKE_BACK = "PTB" // LIST > Today Done > Take Back
    const val OUTLET_PICKUP_SCAN = "OPS" // LIST > Outlet Pickup Scan
    const val SELF_COLLECTION = "SEC" // SCAN > Self-Collection
}