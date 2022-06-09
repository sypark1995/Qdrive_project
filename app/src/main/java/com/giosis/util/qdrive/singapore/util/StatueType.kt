package com.giosis.util.qdrive.singapore.util

object StatueType {
    const val TYPE_DELIVERY = "D"
    const val TYPE_PICKUP = "P"
    const val TYPE_CNR = "CnR"

    const val DELIVERY_START = "D3"
    const val DELIVERY_DONE = "D4" // SCAN > Delivery Done
    const val DELIVERY_FAIL = "DX"
    const val PICKUP_REASSIGN = "RE"
    const val PICKUP_FAIL = "PF"
    const val PICKUP_CANCEL = "PX"
    const val PICKUP_DONE = "P3"
    const val PICKUP_CONFIRM = "P2"
    const val RETURN_FAIL = "RF"

}