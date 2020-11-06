package com.giosis.util.qdrive.barcodescanner.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.giosis.util.qdrive.barcodescanner.PlanarYUVLuminanceSource;
import com.giosis.util.qdrive.util.DisplayUtil;

import java.io.IOException;


public final class CameraManager {
    private static final String TAG = "CameraManager";

    private final Context context;

    private final CameraConfigurationManager configManager;
    private Camera camera;
    private Rect framingRect;
    private Rect framingRectInPreview;

    private boolean previewing;

    private static CameraManager cameraManager;


    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private final PreviewCallback previewCallback;

    /**
     * Autofocus callbacks arrive here, and are dispatched to the Handler which requested them.
     */
    private final AutoFocusCallback autoFocusCallback;


    //
    public static void init(Context context) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(context);
        }
    }

    public static CameraManager get() {
        return cameraManager;
    }

    private CameraManager(Context context) {

        this.context = context;
        this.configManager = new CameraConfigurationManager(context);
        previewCallback = new PreviewCallback(configManager);
        autoFocusCallback = new AutoFocusCallback();
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    public void openDriver(SurfaceHolder holder) throws IOException {
        if (camera == null) {
            camera = Camera.open();
            if (camera == null) {
                throw new IOException();
            }
        }
        camera.setPreviewDisplay(holder);

        configManager.initFromCameraParameters(camera);
        configManager.setDesiredCameraParameters(camera);
    }


    /**
     * Closes the camera driver if still in use.
     */
    public void closeDriver() {
        if (camera != null) {

            FlashlightManager.disableFlashlight();
            camera.release();
            camera = null;

            // Make sure to clear these each time we close the camera, so that any scanning rect
            // requested by intent is forgotten.
            framingRect = null;
            framingRectInPreview = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public void startPreview() {
        if (camera != null && !previewing) {
            camera.startPreview();
            previewing = true;
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public void stopPreview() {
        if (camera != null && previewing) {
            camera.stopPreview();
            previewCallback.setHandler(null, 0);
            autoFocusCallback.setHandler(null, 0);
            previewing = false;
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public void requestPreviewFrame(Handler handler, int message) {

        if (camera != null && previewing) {
            previewCallback.setHandler(handler, message);
            camera.setOneShotPreviewCallback(previewCallback);
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     *
     * @param handler The Handler to notify when the autofocus completes.
     * @param message The message to deliver.
     */
    public void requestAutoFocus(Handler handler, int message) {
        if (camera != null && previewing) {
            autoFocusCallback.setHandler(handler, message);
            //Log.d(TAG, "Requesting auto-focus callback");
            try {
                camera.autoFocus(autoFocusCallback);
            } catch (Exception e) {
                Log.e("Exception", "Camera AutoFocus Exception : " + e.toString());
            }
        }
    }


    private static final int MIN_FRAME_WIDTH = 480;
    private static final int MIN_FRAME_HEIGHT = 267;
    private static final int MAX_FRAME_WIDTH = 480;
    private static final int MAX_FRAME_HEIGHT = 267;

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public Rect getFramingRect() {

        if (framingRect == null) {
            if (camera == null) {
                return null;
            }
            Point screenResolution = configManager.getScreenResolution();
            int width = screenResolution.x * 3 / 4;
            if (width < DisplayUtil.DPFromPixel(context, MIN_FRAME_WIDTH)) {
                width = DisplayUtil.DPFromPixel(context, MIN_FRAME_WIDTH);
            } else if (width > DisplayUtil.DPFromPixel(context, MAX_FRAME_WIDTH)) {
                width = DisplayUtil.DPFromPixel(context, MAX_FRAME_WIDTH);
            }
            int height = screenResolution.y * 3 / 4;
            if (height < DisplayUtil.DPFromPixel(context, MIN_FRAME_HEIGHT)) {
                height = DisplayUtil.DPFromPixel(context, MIN_FRAME_HEIGHT);
            } else if (height > DisplayUtil.DPFromPixel(context, MAX_FRAME_HEIGHT)) {
                height = DisplayUtil.DPFromPixel(context, MAX_FRAME_HEIGHT);
            }
            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = DisplayUtil.DPFromPixel(context, 0);

            //Log.e("capture", TAG + "  Rect1 Size : " + leftOffset + " / " + leftOffset + width + " / " + topOffset + " / " + topOffset + height);
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        }

        return framingRect;
    }

    /**
     * coordinates are in terms of the preview frame,
     * not UI / screen.
     */
    public Rect getFramingRectInPreview() {

        if (framingRectInPreview == null) {

            Rect rect = new Rect(getFramingRect());
            Point cameraResolution = configManager.getCameraResolution();
            Point screenResolution = configManager.getScreenResolution();
            // rect.left = rect.left * cameraResolution.x / screenResolution.x;
            // rect.right = rect.right * cameraResolution.x / screenResolution.x;
            // rect.top = rect.top * cameraResolution.y / screenResolution.y;
            // rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            rect.left = rect.left * cameraResolution.y / screenResolution.x;
            rect.right = rect.right * cameraResolution.y / screenResolution.x;
            rect.top = rect.top * cameraResolution.x / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
            //Log.e("capture", TAG + "  Rect2 Size : " + rect.left + " / " + rect.right + " / " + rect.top + " / " + rect.bottom);
            framingRectInPreview = rect;
        }

        return framingRectInPreview;
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {

        Rect rect = getFramingRectInPreview();

        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height());
    }


    public void onFlash() {
        if (camera == null) return;

        FlashlightManager.enableFlashlight();

        Parameters params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
    }

    public void offFlash() {
        if (camera == null) return;

        FlashlightManager.disableFlashlight();

        Parameters params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
    }
}