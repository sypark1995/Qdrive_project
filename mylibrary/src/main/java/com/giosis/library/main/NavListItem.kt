package com.giosis.library.main

import java.util.*

data class NavListItem(
    var id: Int = 0,
    var title: String = "",
    var childArrayList: ArrayList<String>? = null
)
