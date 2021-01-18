package com.giosis.library.list.delivery

import java.io.Serializable

class OutletDeliveryDoneListItem : Serializable {
    var trackingNo: String? = null
    var receiverName: String? = null
    var jobID: String? = null
    var vendorCode: String? = null
    var qrCode: String? = null
}