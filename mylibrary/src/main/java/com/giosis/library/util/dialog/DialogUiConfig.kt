package com.giosis.library.util.dialog

import androidx.annotation.StringRes
import com.giosis.library.R
import kotlinx.android.parcel.Parcelize

// messageString 을 출력하고 싶으면
//  messageString = "something" 시 출력됨.

@Parcelize
data class DialogUiConfig(
        @StringRes override val title: Int,
        @StringRes override val message: Int = 0,
        override val messageString: String = "",
        @StringRes override val positiveButtonText: Int = R.string.button_ok,
        @StringRes override val negativeButtonText: Int = R.string.button_cancel,
        override val cancelVisible: Boolean = true
) : IDialogUiConfig