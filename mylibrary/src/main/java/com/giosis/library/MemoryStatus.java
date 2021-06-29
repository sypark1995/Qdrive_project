package com.giosis.library;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;

public class MemoryStatus {

    public static final int ERROR = -1;
    public static final long PRESENT_BYTE = 10485760;  // 10485760 byte  10메가바이트와 같다.

    static public long getAvailableInternalMemorySize() {

        try {

            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();

            return availableBlocks * blockSize;
        } catch (Exception e) {
            return ERROR;
        }
    }
}