package com.giosis.library.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ImageResult : Serializable {

    @SerializedName("result")
    var result = ""

    @SerializedName("path")
    var path = ""

    @SerializedName("conv_file_size")
    var conv_file_size = ""

    @SerializedName("conv_width")
    var conv_width = ""

    @SerializedName("conv_height")
    var conv_height = ""

    @SerializedName("src_file_name")
    var src_file_name = ""

    @SerializedName("src_file_full_path")
    var src_file_full_path = ""

    @SerializedName("log_no")
    var log_no = ""

    @SerializedName("id")
    var id = ""

}