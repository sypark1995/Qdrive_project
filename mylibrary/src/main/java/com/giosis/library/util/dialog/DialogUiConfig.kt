package com.giosis.library.util.dialog

import androidx.annotation.StringRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DialogUiConfig(
        @StringRes override val title: Int,
        @StringRes override val message: Int,
        @StringRes override val positiveButtonText: Int? = null,
        @StringRes override val negativeButtonText: Int? = null,
        override val cancelVisible: Boolean = true
) : IDialogUiConfig