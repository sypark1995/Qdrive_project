package com.giosis.util.qdrive.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import androidx.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Camera2APIScan {
    String TAG = "Camera2APIScan";

    public interface Camera2Interface {
        void onCameraDeviceOpened(CameraDevice cameraDevice, Size cameraSize);
    }

    private Activity mActivity;

    private Camera2Interface mInterface;
    private Size mCameraSize;

    private Point screenPoint;
    private Point cameraPoint;

    private CameraManager cameraManager;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private Surface surface;
    private CaptureRequest.Builder mPreviewRequestBuilder;


    private Handler previewHandler;
    private int previewMessage;


    public Camera2APIScan(Camera2Interface impl) {
        mInterface = impl;
    }

    public CameraManager getCameraManager(Activity activity) {

        mActivity = activity;
        cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        return cameraManager;
    }

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

    // 실질적으로 사용할 카메라 셋팅
    @SuppressLint("MissingPermission")
    public void setCameraDevice(CameraManager cameraManager, String cameraId) {
        try {

            cameraManager.openCamera(cameraId, CameraDeviceStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback CameraDeviceStateCallback = new CameraDevice.StateCallback() {

        // 카메라 정상 오픈 시 호출
        @Override
        public void onOpened(@NonNull CameraDevice camera) {

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

            this.surface = surface;
            cameraDevice.createCaptureSession(Collections.singletonList(surface), CaptureSessionCallback, null);

            // 단일 이미지 캡쳐를 위한 하드웨어 설정(센서, 렌즈, 플래쉬) 및 출력 버터등의 정보(immutable)
            mPreviewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
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
                cameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), preViewCaptureCallback, null);
            } catch (CameraAccessException e) {

                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NotNull CameraCaptureSession cameraCaptureSession) {
        }
    };


    private CameraCaptureSession.CaptureCallback preViewCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NotNull CameraCaptureSession session, @NotNull CaptureRequest request, @NotNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NotNull CameraCaptureSession session, @NotNull CaptureRequest request, @NotNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            ImageReader reader = ImageReader.newInstance(mCameraSize.getWidth(), mCameraSize.getHeight(), ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(surface);

            reader.setOnImageAvailableListener(readerListener, null);
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

    // SCAN Barcode
    private ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {

            Log.e("krm_camera", TAG + "  onImageAvailable");
            try {

                Image image = imageReader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                Log.e("krm_camera", TAG + "  Decode Data : " + new String(bytes));
            } catch (Exception e) {

                Toast.makeText(mActivity, "Take Photo_3 Exception : : " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Point findBestPreviewSizeValue(Size[] previewSizeArray, Point screenResolution) {

        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;

        for (Size size : previewSizeArray) {

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
            return new Point(bestX, bestY);
        }

        return null;
    }
}