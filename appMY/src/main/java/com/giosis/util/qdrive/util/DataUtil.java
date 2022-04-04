package com.giosis.util.qdrive.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class DataUtil {

    public static String locker_pin_url = "https://www.lockeralliance.net/pin";

    public static void copyClipBoard(Context context, String data) {

        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label", data);
        clipboardManager.setPrimaryClip(clipData);
    }


}