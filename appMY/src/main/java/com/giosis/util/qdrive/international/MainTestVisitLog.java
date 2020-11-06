package com.giosis.util.qdrive.international;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.main.Dpc3OutValidationCheckHelper;
import com.giosis.util.qdrive.util.Camera2APIScan;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.ui.CommonActivity;

// TODO  -  CaptureActivity  Camera2
public class MainTestVisitLog extends CommonActivity implements Camera2APIScan.Camera2Interface, TextureView.SurfaceTextureListener {
    private static final String TAG = "MainTestVisitLog";


    // krm0219
    FrameLayout layout_top_back;
    TextView text_top_title;
    ToggleButton toggle_btn_capture_camera_flash;

    //
    Context context;

    // camera
    SurfaceView surfaceView;
    Camera2APIScan camera;


    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE, PermissionChecker.CAMERA};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_maintest);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        surfaceView = findViewById(R.id.surfaceView);
        toggle_btn_capture_camera_flash = findViewById(R.id.toggle_btn_capture_camera_flash);


        //
        context = getApplicationContext();
        camera = new Camera2APIScan(this);

        layout_top_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                Log.e("krm_camera", "Click BACK");
                finish();
            }
        });

        toggle_btn_capture_camera_flash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                camera.flashlight();
            }
        });


        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

                Log.e("krm_camera", TAG + "  surfaceChanged : " + width + " / " + height);
                openCamera(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });


        //
        PermissionChecker checker = new PermissionChecker(this);

        // 권한 여부 체크 (없으면 true, 있으면 false)
        if (checker.lacksPermissions(PERMISSIONS)) {

            isPermissionTrue = false;
            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, PERMISSIONS);
            overridePendingTransition(0, 0);
        } else {

            isPermissionTrue = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isPermissionTrue) {

            camera = new Camera2APIScan(this);

            //    openCamera();
           /* if (surfaceView.isAvailable()) {

                openCamera();
            } else {

                surfaceView.setSurfaceTextureListener(this);
            }*/
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("krm0219", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

                isPermissionTrue = true;
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        closeCamera();
    }


    public void validationCheck(String strBarcodeNo) {

        try {

            Log.e("krm_camera", TAG + "  validationCheck  " + strBarcodeNo);

            final String scanNo = strBarcodeNo;

            new Dpc3OutValidationCheckHelper.Builder(this, MyApplication.preferences.getUserId(), "N", strBarcodeNo)
                    .setOnDpc3OutValidationCheckListener(new Dpc3OutValidationCheckHelper.OnDpc3OutValidationCheckListener() {

                        @Override
                        public void OnDpc3OutValidationCheckResult(StdResult result) {


                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            if (vibrator != null) {

                                vibrator.vibrate(200L);
                            }

                            if (result.getResultCode() < 0) {       // Validation Failed


                                Log.e("krm_camera", TAG + "  Dpc3OutValidationCheckHelper fail");
                            } else {                                // Validation Success


                                Log.e("krm_camera", TAG + "  Dpc3OutValidationCheckHelper success " + scanNo);
                            }
                        }

                        @Override
                        public void OnDpc3OutValidationCheckFailList(StdResult result) {

                            Toast.makeText(context, context.getResources().getString(R.string.msg_error_check_again), Toast.LENGTH_SHORT).show();
                        }
                    }).build().execute();
        } catch (Exception e) {

            Log.e("krm_camera", TAG + "  validationCheck  Exception" + e.toString());
            Log.e("krm0219", TAG + "  Exception : " + e.toString());
            Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    public Bitmap getResizeBitmap(Bitmap originalBitmap) {

        Bitmap rotatebmp = null;

        if (originalBitmap == null) {
            return null;
        }

        try {
            int height = originalBitmap.getHeight();
            int width = originalBitmap.getWidth();

            Log.e("eylee", "★★★★★   first  :: width - " + width + " ,height - " + height);
            if (height > 450) {

                int ratio;

                if (width > height) {

                    ratio = width / 500;
                    width = 500;
                    height = height / ratio;
                } else if (height > width) {

                    ratio = height / 500;
                    height = 500;
                    width = width / ratio;
                } else {

                    height = 500;
                    width = 500;
                }

                Log.e("krm0219", "★★★★★    :: width - " + width + " ,height - " + height);

                if (height > 0) {
                    Bitmap resized = Bitmap.createScaledBitmap(originalBitmap, width, height, false);
                    Matrix matrix = new Matrix();      // setup rotation degree
                    matrix.preRotate(90);
                    rotatebmp = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), matrix, false);
                }
            } else {
                rotatebmp = originalBitmap;
            }

        } catch (Exception e) {
            Log.e("eylee", e.toString());
            e.printStackTrace();
        }
        return rotatebmp;
    }

    private void openCamera(int width, int height) {
        Log.e("krm_camera", TAG + "  openCamera");

        CameraManager cameraManager = camera.getCameraManager(this);
        String cameraId = camera.getCameraCharacteristics(cameraManager);
        camera.setCameraDevice(cameraManager, cameraId, width, height);
    }

    private void closeCamera() {

        camera.closeCamera();
    }

    @Override
    public void onValidationCheck(String barcodeNo) {

        validationCheck(barcodeNo);
    }

    @Override
    public void onCameraDeviceOpened(CameraDevice cameraDevice, Size cameraSize) {

        Log.e("krm_camera", TAG + "  onCameraDeviceOpened");
        /*SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
        Surface surface = new Surface(texture);*/


        surfaceView.getHolder().setFixedSize(cameraSize.getWidth(), cameraSize.getHeight());
        Surface surface = surfaceView.getHolder().getSurface();
       /* if (surface == null) {

            Log.e("krm_camera", TAG + "  onCameraDeviceOpened  Surface Null");
        } else {

            Log.e("krm_camera", TAG + "  onCameraDeviceOpened  Surface Not Null");
        }*/

        camera.setCaptureSessionRequest(cameraDevice, surface);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

        // openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }
}