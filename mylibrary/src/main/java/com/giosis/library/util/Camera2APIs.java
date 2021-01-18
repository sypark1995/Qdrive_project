package com.giosis.library.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Camera2APIs {
    private String TAG = "Camera2APIs";

    public interface Camera2Interface {
        void onCameraDeviceOpened(CameraDevice cameraDevice, Size cameraSize, int rotation);
    }

    private Camera2Interface mInterface;
    private Size mCameraSize;
    private int mRotation = 0;

    private CameraCaptureSession mCaptureSession = null;
    private CameraDevice mCameraDevice = null;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest.Builder captureBuilder;

    private Activity mActivity;
    private ImageView mImageView;


    public Camera2APIs(Camera2Interface impl) {
        mInterface = impl;
    }

    public CameraManager getCameraManager(Activity activity) {

        mActivity = activity;
        return (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    }


    public String getCameraCharacteristics(CameraManager cameraManager) {

        try {

            Log.e(TAG, "  Camera List Count : " + cameraManager.getCameraIdList().length);

            for (String cameraId : cameraManager.getCameraIdList()) {

                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

                Log.e(TAG, "  Lens : " + characteristics.get(CameraCharacteristics.LENS_FACING));
                Log.e(TAG, "  Orientation : " + characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION));

                /*
                CameraCharacteristics.LENS_FACING : 카메라 렌즈 방향 (0:전면 / 1:후면 / 2:기타)
                CameraCharacteristics.SENSOR_ORIENTATION) : 카메라 방향
                 */
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {

                    mRotation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) - 90;

                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    assert map != null;
                    Size[] sizes = map.getOutputSizes(SurfaceTexture.class);

                    mCameraSize = sizes[0];

                    for (Size size : sizes) {

                        // 최적의 사이즈 찾아내기!
                        // Log.e("krm0219", "Size ; " + size.getWidth() + " / " + size.getHeight() + " / " + mCameraSize.getWidth());
                        if (size.getWidth() > mCameraSize.getWidth()) {

                            mCameraSize = size;
                        }
                    }

                    return cameraId;
                }
            }
        } catch (Exception e) {

            Log.e("Exception", TAG + "  getCameraCharacteristics  Exception : " + e.toString());
            e.printStackTrace();
        }

        return null;
    }

    // 실질적으로 사용할 카메라 셋팅
    @SuppressLint("MissingPermission")
    public void setCameraDevice(CameraManager cameraManager, String cameraId) {
        try {

            cameraManager.openCamera(cameraId, CameraDeviceStateCallback, null);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  setCameraDevice Exception : " + e.toString());
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback CameraDeviceStateCallback = new CameraDevice.StateCallback() {

        // 카메라 정상 오픈 시 호출
        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            mCameraDevice = camera;
            mInterface.onCameraDeviceOpened(camera, mCameraSize, mRotation);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

            if (mCameraDevice != null) {

                mCameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

            if (mCameraDevice != null) {

                mCameraDevice.close();
                mCameraDevice = null;

                //     Toast.makeText(mActivity, "Camera State Error : " + error + "\nPlease try again…", Toast.LENGTH_SHORT).show();
            }
        }
    };


    // 이미지 캡쳐를 위한 세션 연결 (해당 세션이 연결될 Surface 전달)
    public void setCaptureSessionRequest(CameraDevice cameraDevice, Surface surface) {

        try {

            cameraDevice.createCaptureSession(Collections.singletonList(surface), CaptureSessionCallback, null);

            mPreviewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }
    }

    private CameraCaptureSession.StateCallback CaptureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

            try {

                mCaptureSession = cameraCaptureSession;

                // AUTO FOCUS
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                cameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), preViewCaptureCallback, null);
            } catch (Exception e) {

                Toast.makeText(mActivity, "Camera State Exception : " + e.toString() + "\nPlease try again…", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
        }
    };

    private CameraCaptureSession.CaptureCallback preViewCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    public void closeCamera() {

        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }


    // NOTIFICATION.  Image Capture
    public void takePhoto(TextureView textureView, ImageView imageView) {

        mImageView = imageView;

        ImageReader reader = ImageReader.newInstance(mCameraSize.getWidth(), mCameraSize.getHeight(), ImageFormat.JPEG, 1);
        List<Surface> outputSurface = new ArrayList<>(2);
        outputSurface.add(reader.getSurface());
        outputSurface.add(new Surface(textureView.getSurfaceTexture()));
        reader.setOnImageAvailableListener(readerListener, null);


        try {

            captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            mCameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

                    try {

                        cameraCaptureSession.capture(captureBuilder.build(), captureCallback, null);
                    } catch (Exception e) {

                        Toast.makeText(mActivity, "Take Photo_1 Exception : : " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                }
            }, null);
        } catch (Exception e) {

            Toast.makeText(mActivity, "Take Photo_2 Exception : : " + e.toString(), Toast.LENGTH_SHORT).show();
            Log.e("Exception", "Camera2APIs  Take Photo_2  Exception : " + e.toString());
        }
    }


    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            mInterface.onCameraDeviceOpened(mCameraDevice, mCameraSize, mRotation);
        }
    };

    // 이미지 저장
    private ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {

            MediaActionSound mediaActionSound = new MediaActionSound();
            mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);

            try {

                Image image = imageReader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.inDither = false;
                options.inTempStorage = new byte[32 * 1024];
                options.inPreferredConfig = Bitmap.Config.RGB_565;

                Bitmap bMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                Bitmap resizeBitmap = getResizeBitmap(bMap);

                mImageView.setImageBitmap(resizeBitmap);
                mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            } catch (Exception e) {

                Toast.makeText(mActivity, "Take Photo_3 Exception : : " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };


    public Bitmap getResizeBitmap(Bitmap originalBitmap) {

        Bitmap rotatebmp = null;

        if (originalBitmap == null) {
            return null;
        }

        try {

            int height = originalBitmap.getHeight();
            int width = originalBitmap.getWidth();

            Log.e(TAG, "★★★★★   first  :: width - " + width + " ,height - " + height);

            if (height > 900) {

                int ratio;

                if (width > height) {

                    ratio = width / 1000;
                    width = 1000;
                    height = height / ratio;
                } else if (height > width) {

                    ratio = height / 1000;
                    height = 1000;
                    width = width / ratio;
                } else {

                    height = 1000;
                    width = 1000;
                }

                Log.e(TAG, "★★★★★    :: width - " + width + " ,height - " + height);

                if (0 < height) {

                    Bitmap resized = Bitmap.createScaledBitmap(originalBitmap, width, height, false);
                    Matrix matrix = new Matrix();
                    matrix.preRotate(90);
                    rotatebmp = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), matrix, false);
                }
            } else {

                rotatebmp = originalBitmap;
            }
        } catch (Exception e) {

            Log.e(TAG, "getResizeBitmap  Exception : " + e.toString());
            e.printStackTrace();
        }

        return rotatebmp;
    }

    public boolean hasImage(ImageView view) {

        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }

        return hasImage;
    }
}