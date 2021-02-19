package com.giosis.util.qdrive.barcodescanner;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.giosis.util.qdrive.barcodescanner.camera.CameraManager;
import com.giosis.util.qdrive.international.R;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;

@Deprecated
final class DecodeHandler extends Handler {
    private static final String TAG = "DecodeHandler";

    private final CaptureActivityTemp activity;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;

    DecodeHandler(CaptureActivityTemp activity, Hashtable<DecodeHintType, Object> hints) {

        this.activity = activity;
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case R.id.decode: {
                decode((byte[]) message.obj, message.arg1, message.arg2);
            }
            break;
            case R.id.quit:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {

        //Log.e("capture", TAG + "  Decode Data : " + new String(data));
        Result rawResult = null;
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }

        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {

            rawResult = multiFormatReader.decodeWithState(bitmap);
        } catch (Exception e) {

            //   Log.e("capture", TAG + "  decode  Exception : " + e.toString());
        } finally {

            multiFormatReader.reset();
        }


        if (rawResult != null) {

            //   Log.e("capture", TAG + "  rawResult != null  " + rawResult.getText());

            // 바코드 전달  (CaptureActivityTempHandler)
            Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, rawResult);
            message.sendToTarget();
        } else {

            Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
    }
}