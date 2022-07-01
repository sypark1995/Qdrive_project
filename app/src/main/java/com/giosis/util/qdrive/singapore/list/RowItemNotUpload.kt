package com.giosis.util.qdrive.singapore.list

import java.util.*

class RowItemNotUpload(
    var stat: String,
    var shipping: String,
    var name: String,
    var address: String,
    var request: String,
    var type: String,
    var route: String,
    var sender: String
) {

    var items: ArrayList<ChildItemNotUpload>? = null
    var isClicked: Boolean = false
}