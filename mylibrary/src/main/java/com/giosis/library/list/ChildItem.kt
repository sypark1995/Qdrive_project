package com.giosis.library.list

import java.io.Serializable

class ChildItem : Serializable {
    var tel: String? = null
    var hp: String? = null
    var stat: String? = null
    var statReason: String? = null
    var statMsg: String? = null
    private var secret_no_type: String? = null
    private var secret_no: String? = null

    var secretNoType: String?
        get() = secret_no_type
        set(secret_no_type) {
            if (secret_no_type != null) {
                this.secret_no_type = secret_no_type
            } else {
                this.secret_no_type = ""
            }
        }

    var secretNo: String?
        get() = secret_no
        set(secret_no) {
            if (secret_no != null) {
                this.secret_no = secret_no
            } else {
                this.secret_no = ""
            }
        }
}