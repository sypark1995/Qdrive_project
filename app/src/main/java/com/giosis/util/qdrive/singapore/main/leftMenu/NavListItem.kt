package com.giosis.util.qdrive.singapore.main.leftMenu

import java.util.*

data class NavListItem(
    var iconId: Int = 0,
    var titleResource: Int = 0,
    var className: Class<*>? = null,
    var subList: ArrayList<SubMenuItem>? = null
)
