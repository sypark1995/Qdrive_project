package com.giosis.util.qdrive.international

import java.io.Serializable

data class LoginNation(
    val nation_nm: String,
    val nation_cd: String,
    val nation_img_url: String
): Serializable