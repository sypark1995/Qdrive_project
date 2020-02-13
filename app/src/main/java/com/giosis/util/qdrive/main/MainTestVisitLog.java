package com.giosis.util.qdrive.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Camera2APIScan;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainTestVisitLog extends AppCompatActivity implements Camera2APIScan.Camera2Interface, TextureView.SurfaceTextureListener {
    private static final String TAG = "SigningDeliveryVisitLog";


    // krm0219
    FrameLayout layout_top_back;
    TextView text_top_title;

    LinearLayout layout_sign_d_f_take_photo;
    LinearLayout layout_sign_d_f_gallery;
    ImageView img_sign_d_f_visit_log;
    Button btn_sign_d_f_save;

    //
    Context context;
    String opID = "";
    String officeCode = "";
    String deviceID = "";


    // gallery
    private static final int RESULT_LOAD_IMAGE = 3;
    boolean isGalleryActivate = false;

    // camera
    TextureView textureView;
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

        layout_sign_d_f_take_photo = findViewById(R.id.layout_sign_d_f_take_photo);
        layout_sign_d_f_gallery = findViewById(R.id.layout_sign_d_f_gallery);
        textureView = findViewById(R.id.textureView);
        img_sign_d_f_visit_log = findViewById(R.id.img_sign_d_f_visit_log);
        btn_sign_d_f_save = findViewById(R.id.btn_sign_d_f_save);


        //
        context = getApplicationContext();
        camera = new Camera2APIScan(this);

        opID = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
        officeCode = SharedPreferencesHelper.getSigninOfficeCode(getApplicationContext());
        deviceID = SharedPreferencesHelper.getSigninDeviceID(getApplicationContext());

        layout_top_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                finish();
            }
        });


        layout_sign_d_f_take_photo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        layout_sign_d_f_gallery.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                getImageFromAlbum();
            }
        });

        btn_sign_d_f_save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // saveServerUploadSign();
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

            if (textureView.isAvailable()) {

                openCamera();
            } else {

                textureView.setSurfaceTextureListener(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isGalleryActivate = false;

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            try {

                Uri selectedImageUri = data.getData();
                Bitmap selectedImage = Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                Bitmap resizeBitmap = getResizeBitmap(selectedImage);

                img_sign_d_f_visit_log.setImageBitmap(resizeBitmap);
                img_sign_d_f_visit_log.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                onResume();
            } catch (Exception e) {
                Log.e("eylee", e.toString());
                e.printStackTrace();
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("Permission", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

                isPermissionTrue = true;
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        closeCamera();
    }

    /*
     * 실시간 Upload 처리
     * 2016-10-04 eylee added
     * visit log 페이지에서 저장 버튼 눌렀을 때
     */
    public void saveServerUploadSign() {

        try {

            @SuppressLint("SimpleDateFormat")
            String timeStamp = new SimpleDateFormat("MMddHHmmss").format(new Date());
            String pictureFile = "INFO_" + timeStamp;

            saveImage(pictureFile, img_sign_d_f_visit_log);

            /*new ManualServerUploadVisitLogTypeHelper.Builder(this, opID, officeCode, deviceID,
                    "SG19612068", img_sign_d_f_visit_log, "", "RC",
                    MemoryStatus.getAvailableInternalMemorySize(), 0, 0, "DX")
                    .setOnServerEventListener(new OnServerEventListener() {

                        @Override
                        public void onPostResult() {

                            Log.e("krm_camera", "OK");
                        }

                        @Override
                        public void onPostFailList() {
                        }
                    }).build().execute();
*/
        } catch (Exception e) {

            Log.e("krm0219", TAG + "  Exception : " + e.toString());
            Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    private void saveImage(String name, View targetView) {

        String dirPath = Environment.getExternalStorageDirectory().toString() + "/QdriveTEST";
        File saveDir = new File(dirPath);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }

        Log.e("krm_camera", "Size : " + targetView.getWidth() + " / " + targetView.getHeight());
        targetView.buildDrawingCache();
        Bitmap captureView = targetView.getDrawingCache();

        String filePath = dirPath + "/" + name + ".png";
        File file = new File(filePath);

        try {

            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            captureView.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "save", Toast.LENGTH_SHORT).show();
    }


    // gallery 가는 함수
    private void getImageFromAlbum() {
        try {

            if (!isGalleryActivate) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                isGalleryActivate = true;
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
            }
        } catch (Exception ex) {

            isGalleryActivate = false;
            Log.i("eylee", ex.toString());
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


    private void openCamera() {

        CameraManager cameraManager = camera.getCameraManager(this);
        String cameraId = camera.getCameraCharacteristics(cameraManager);
        camera.setCameraDevice(cameraManager, cameraId);
    }

    private void closeCamera() {

        camera.closeCamera();
    }

    @Override
    public void onCameraDeviceOpened(CameraDevice cameraDevice, Size cameraSize) {

        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
        Surface surface = new Surface(texture);

        camera.setCaptureSessionRequest(cameraDevice, surface);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

        openCamera();
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