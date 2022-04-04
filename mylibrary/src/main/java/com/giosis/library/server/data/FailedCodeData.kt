package com.giosis.library.server.data

import com.google.gson.annotations.SerializedName

class FailedCodeData {
    @SerializedName("cd_nm")
    var failedString: String = ""

    @SerializedName("cd")
    var failedCode: String = ""
}

