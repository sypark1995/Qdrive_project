package com.giosis.library.server

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


object ImageUpload {
// http://dp.image-gmkt.com"

    fun upload(file: File): String {

        try {

            val rqFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val mpFile = MultipartBody.Part.createFormData("photo", file.name, rqFile)

            Log.e("ImageUpload", "start upload image")

            // Dayil yyyyMMdd
            val result = RetrofitClient.instanceImageUpload().upload(
                    size = "50000000",
                    ext = "image",
                    folder = "qx/test",
                    basepath = "dpimage_upload",
                    width = "0",
                    height = "0",
                    quality = "100",
                    allsizeResize = "N",
                    remainSrcImage = "N",
                    extent = "N",
                    id = "uc_file_upload",
                    commitYn = "Y",
                    custNo = "100061082",
                    upload_channel = "QxFront",
                    png_compress = "N",
                    inc_org_size = "N",
                    date = "N",
                    result_flag = "JSON",
                    file = mpFile
            ).execute().body()

            var returnValue = ""
            if (result != null) {
                for (imageResult in result) {
                    returnValue = "http://dp.image-gmkt.com" + imageResult.path
                }
            }
            Log.e("ImageUpload", "end upload url  $returnValue")

            return returnValue

        } catch (e: Exception) {
            Log.e("TAG", e.localizedMessage)
            return ""
        }
    }

}