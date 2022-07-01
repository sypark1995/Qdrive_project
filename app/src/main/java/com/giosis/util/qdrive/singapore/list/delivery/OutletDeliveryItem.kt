package com.giosis.util.qdrive.singapore.list.delivery

import java.io.Serializable

class OutletDeliveryItem : Serializable {
    var trackingNo: String? = null
    var receiverName: String? = null
    var jobID: String? = null
    var vendorCode: String? = null
    var qrCode: String? = null
}