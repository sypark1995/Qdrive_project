package com.giosis.library.list

import java.io.Serializable

class BarcodeData : Serializable {
    var barcode: String? = null

    var state // type : 'D', 'P'... 등등~
            : String? = null

    // TODO_kjyoo set 만 있고 get은 없음
    var changeDeliveryText // Change Driver 에서 표시되는 글자.  ex) "MY19612073 | DPC3-OUT | hyemi"
            : String? = null
}