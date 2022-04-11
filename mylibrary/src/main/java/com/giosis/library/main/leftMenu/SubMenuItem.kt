package com.giosis.library.main.leftMenu

import android.os.Bundle

data class SubMenuItem(
    val title: Int,
    var className: Class<*>,
    val extras: Bundle.() -> Unit = {},
)
