package com.giosis.library.list

import java.io.Serializable

class BarcodeData : Serializable {
    var barcode: String? = null

    var state // type : 'D', 'P'... 등등~
            : String? = null

}