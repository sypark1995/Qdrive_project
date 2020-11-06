package com.giosis.util.qdrive.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Camera2APIScan {
    String TAG = "Camera2APIScan";

    public interface Camera2Interface {
        void onCameraDeviceOpened(CameraDevice cameraDevice, Size cameraSize);

        void onValidationCheck(String barcodeNo);
    }

    private Activity mActivity;

    private Camera2Interface mInterface;
    private Size mCameraSize;

    private Point screenPoint;
    private Point cameraPoint;

    private CameraManager cameraManager;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;


    Handler cameraBkgHandler;


    public Camera2APIScan(Camera2Interface impl) {
        mInterface = impl;
    }

    public CameraManager getCameraManager(Activity activity) {

        Handler cameraBkgHandler = new Handler();
        mActivity = activity;
        cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        return cameraManager;
    }

    Size mPreviewSize;


    public String getCameraCharacteristics(CameraManager cameraManager) {

        try {

            for (String cameraId : cameraManager.getCameraIdList()) {

                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {

                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    assert map != null;
                    Size[] sizes = map.getOutputSizes(SurfaceTexture.class);


                    WindowManager manager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
                    Display display = manager.getDefaultDisplay();
                    Point size1 = new Point();
                    display.getSize(size1);

                    screenPoint = new Point(size1.x, size1.y);
                    Log.e("krm_camera", "Screen resolution: " + screenPoint);

                    cameraPoint = findBestPreviewSizeValue(sizes, screenPoint);
                    Log.e("krm_camera", "Camera resolution: " + cameraPoint);

                    Log.e("krm_camera", "  Camera Size : " + mCameraSize);

                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }

        return null;
    }

    int width;
    int height;

    // 실질적으로 사용할 카메라 셋팅
    @SuppressLint("MissingPermission")
    public void setCameraDevice(CameraManager cameraManager, String cameraId, int width, int height) {
        try {

            this.width = width;
            this.height = height;

            Log.e("krm_camera", TAG + "  setCameraDevice");
            cameraManager.openCamera(cameraId, CameraDeviceStateCallback, cameraBkgHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    BarcodeDetector barcodeDetector;


    private CameraDevice.StateCallback CameraDeviceStateCallback = new CameraDevice.StateCallback() {

        // 카메라 정상 오픈 시 호출
        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            // TODO
            barcodeDetector = new BarcodeDetector.Builder(mActivity)
                    .setBarcodeFormats(Barcode.ALL_FORMATS)
                    .build();


            mCameraDevice = camera;
            mInterface.onCameraDeviceOpened(camera, mCameraSize);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };


    // 이미지 캡쳐를 위한 세션 연결 (해당 세션이 연결될 Surface 전달)
    public void setCaptureSessionRequest(CameraDevice cameraDevice, Surface surface) {

        try {

            ImageReader reader = ImageReader.newInstance(mCameraSize.getWidth(), mCameraSize.getHeight(), ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(surface);
            outputSurface.add(reader.getSurface());
            reader.setOnImageAvailableListener(readerListener, null);

            //    cameraDevice.createCaptureSession(Collections.singletonList(surface), CaptureSessionCallback, cameraBkgHandler);
            cameraDevice.createCaptureSession(outputSurface, CaptureSessionCallback, cameraBkgHandler);

            // 단일 이미지 캡쳐를 위한 하드웨어 설정(센서, 렌즈, 플래쉬) 및 출력 버터등의 정보(immutable)
            mPreviewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.addTarget(reader.getSurface());
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }
    }

    private CameraCaptureSession.StateCallback CaptureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {

            try {

                mCaptureSession = cameraCaptureSession;

                // AUTO FOCUS
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                cameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
            } catch (CameraAccessException e) {

                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
        }
    };


    private CameraCaptureSession.CaptureCallback preViewCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };


    public void closeCamera() {
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }


    // SCAN
    String barcodeNo = "";

    private ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {

            //     Log.e("krm_camera", TAG + "  onImageAvailable");
            try {

                Image image = imageReader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                Bitmap bMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                Frame frame = new Frame.Builder().setBitmap(bMap).build();
                SparseArray<Barcode> barcodeResults = barcodeDetector.detect(frame);

                if (barcodeResults.size() != 0) {

                    String barcodeContents = barcodeResults.valueAt(0).displayValue;

                    if (!barcodeNo.equals(barcodeContents)) {

                        Log.e("krm_camera", "  Barcode : " + barcodeContents);
                        barcodeNo = barcodeContents;
                        mInterface.onValidationCheck(barcodeNo);
                    }
                    //  handleDecode(barcodeContents);
                }

                image.close();

                //    Log.e("krm_camera", TAG + "  Decode Data : " + new String(bytes));
            } catch (Exception e) {

                Log.e("krm_camera", TAG + "  Exception : " + e.toString());
                Toast.makeText(mActivity, "Take Photo_3 Exception : : " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };


    private Point findBestPreviewSizeValue(Size[] previewSizeArray, Point screenResolution) {

        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;

        for (Size size : previewSizeArray) {

            //    Log.e("krm_camera", TAG + "   Size : " + size.getWidth() + " / " + size.getHeight());

            int newX;
            int newY;

            try {

                newX = size.getWidth();
                newY = size.getHeight();
            } catch (NumberFormatException nfe) {

                continue;
            }

            // 차이 절대값
            int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
                break;
            } else if (newDiff < diff) {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }
        }

        if (0 < bestX && 0 < bestY) {

            mCameraSize = new Size(bestX, bestY);
            Log.e("krm_camera", TAG + "   Best Size : " + bestX + " / " + bestY);
            return new Point(bestX, bestY);
        }

        return null;
    }


    boolean flashOn = false;

    public void flashlight() {

        flashOn = !flashOn;

        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, flashOn ? CaptureRequest.FLASH_MODE_TORCH : CaptureRequest.FLASH_MODE_OFF);

        try {

            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
        } catch (Exception e) {

            Log.e("krm_camera", TAG + "  flashList Exception : " + e.toString());
        }
    }
}