package com.giosis.library

import android.os.Environment
import android.os.StatFs

object MemoryStatus {
    const val ERROR = -1
    const val PRESENT_BYTE: Long = 10485760 // 10485760 byte  10메가바이트와 같다.

    @JvmStatic
    val availableInternalMemorySize: Long
        get() = try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            availableBlocks * blockSize
        } catch (e: Exception) {
            ERROR.toLong()
        }
}