package com.giosis.util.qdrive.singapore.util.dialog

import android.os.Parcelable
import androidx.annotation.StringRes

interface IDialogUiConfig : Parcelable {

    @get:StringRes
    val title: Int
    
    @get:StringRes
    val message: Int

    val messageString: String

    @get:StringRes
    val positiveButtonText: Int?

    @get:StringRes
    val negativeButtonText: Int?

    val cancelVisible: Boolean
}