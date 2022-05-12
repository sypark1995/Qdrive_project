package com.giosis.library.list

import java.io.Serializable
import java.util.*

class RowItem(
    var contrNo: String,
    var delay: String,
    var shipping: String,
    var name: String,
    var address: String,
    var request: String?,
    var type: String,
    var route: String,
    var sender: String,
    var desiredDate: String?,
    var qty: String?,
    var selfMemo: String?,
    var lat: Double,
    var lng: Double,
    var stat: String,
    var custNo: String?,
    var partnerID: String?,
    val secure_delivery_yn: String?,
    val parcel_amount: String?,
    val currency: String?,
    val high_amount_yn: String?
) : Serializable {

    private var childItems: ArrayList<ChildItem>? = null

    //
    var order_type_etc: String? = null
    var orderType: String? = null

    var outlet_qty = 0
    var outlet_company: String? = null
    var outlet_store_code: String? = null
    var outlet_store_name: String? = null
    var outlet_operation_hour: String? = null
    var desired_time: String? = null
    var zip_code: String? = null
    var ref_pickup_no: String? = null

    var state: String? = null
    var city: String? = null
    var street: String? = null


    // 2020.06  Trip 단위 묶음
    var tripNo = 0
    var isPrimaryKey = false
    var tripSubDataArrayList: ArrayList<RowItem>? = null

    // 드라이버 위치와의 거리
    var distance = 0f

    var items: ArrayList<ChildItem>?
        get() = childItems
        set(Items) {
            childItems = Items
        }
    var isClicked = false
}