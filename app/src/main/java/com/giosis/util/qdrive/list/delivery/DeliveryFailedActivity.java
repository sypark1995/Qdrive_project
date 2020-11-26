package com.giosis.util.qdrive.list.delivery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.OnServerEventListener;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Camera2APIs;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.MemoryStatus;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;

/***
 *
 * @author eylee
 * @date 2017-06-23
 * Delivery failed -> Visit log 로 기능 개선
 */
public class DeliveryFailedActivity extends AppCompatActivity implements Camera2APIs.Camera2Interface, TextureView.SurfaceTextureListener {
    private static final String TAG = "DeliveryFailedActivity";


    // krm0219
    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_sign_d_f_tracking_no;
    TextView text_sign_d_f_receiver;
    TextView text_sign_d_f_sender;
    EditText edit_sign_d_f_memo;

    LinearLayout layout_sign_d_f_take_photo;
    LinearLayout layout_sign_d_f_gallery;
    TextureView texture_sign_d_f_preview;
    ImageView img_sign_d_f_preview_bg;
    ImageView img_sign_d_f_visit_log;
    Button btn_sign_d_f_save;


    //
    Context context;
    String opID = "";
    String officeCode = "";
    String deviceID = "";
    String mStrWaybillNo;

    // Camera & Gallery
    Camera2APIs camera2;
    String cameraId;
    private static final int RESULT_LOAD_IMAGE = 3;
    boolean isGalleryActivate = false;

    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

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
        setContentView(R.layout.activity_delivery_visit_log);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_d_f_tracking_no = findViewById(R.id.text_sign_d_f_tracking_no);
        text_sign_d_f_receiver = findViewById(R.id.text_sign_d_f_receiver);
        text_sign_d_f_sender = findViewById(R.id.text_sign_d_f_sender);
        edit_sign_d_f_memo = findViewById(R.id.edit_sign_d_f_memo);

        layout_sign_d_f_take_photo = findViewById(R.id.layout_sign_d_f_take_photo);
        layout_sign_d_f_gallery = findViewById(R.id.layout_sign_d_f_gallery);
        texture_sign_d_f_preview = findViewById(R.id.texture_sign_d_f_preview);
        img_sign_d_f_preview_bg = findViewById(R.id.img_sign_d_f_preview_bg);
        img_sign_d_f_visit_log = findViewById(R.id.img_sign_d_f_visit_log);
        btn_sign_d_f_save = findViewById(R.id.btn_sign_d_f_save);


        //
        context = getApplicationContext();
        camera2 = new Camera2APIs(this);
//        opID = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
//        officeCode = SharedPreferencesHelper.getSigninOfficeCode(getApplicationContext());
//        deviceID = SharedPreferencesHelper.getSigninDeviceID(getApplicationContext());
        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();
        deviceID = MyApplication.preferences.getDeviceUUID();

        String strTitle = getIntent().getStringExtra("title");
        mStrWaybillNo = getIntent().getStringExtra("waybillNo");
        String strReceiverName = getIntent().getStringExtra("receiverName");
        String strSenderName = getIntent().getStringExtra("senderName");


        text_top_title.setText(strTitle);
        text_sign_d_f_tracking_no.setText(mStrWaybillNo);
        text_sign_d_f_receiver.setText(strReceiverName);
        text_sign_d_f_sender.setText(strSenderName);
        DisplayUtil.setPreviewCamera(img_sign_d_f_preview_bg);


        layout_top_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                cancelSigning();
            }
        });

        layout_sign_d_f_take_photo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (cameraId != null) {

                    camera2.takePhoto(texture_sign_d_f_preview, img_sign_d_f_visit_log);
                } else {

                    Toast.makeText(DeliveryFailedActivity.this, context.getResources().getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show();
                }
            }
        });

        layout_sign_d_f_gallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                getImageFromAlbum();
            }
        });

        btn_sign_d_f_save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                saveServerUploadSign();
            }
        });

        // Memo 입력제한
        edit_sign_d_f_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (99 <= edit_sign_d_f_memo.length()) {
                    Toast.makeText(context, context.getResources().getText(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

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

            // Camera
            camera2 = new Camera2APIs(this);

            if (texture_sign_d_f_preview.isAvailable()) {

                openCamera();
            } else {

                texture_sign_d_f_preview.setSurfaceTextureListener(this);
            }

            // Location
            gpsTrackerManager = new GPSTrackerManager(context);
            gpsEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager.GPSTrackerStart();
                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();

                Log.e("Location", TAG + " GPSTrackerManager onResume : " + latitude + "  " + longitude + "  ");
            } else {

                DataUtil.enableLocationSettings(DeliveryFailedActivity.this, context);
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
                Bitmap resizeBitmap = camera2.getResizeBitmap(selectedImage);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataUtil.stopGPSManager(gpsTrackerManager);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        cancelSigning();
    }

    public void cancelSigning() {

        new AlertDialog.Builder(this)
                .setMessage(R.string.msg_delivered_sign_cancel)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /*
     * 실시간 Upload 처리
     * 2016-10-04 eylee added
     * visit log 페이지에서 저장 버튼 눌렀을 때
     */
    public void saveServerUploadSign() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {

                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            if (gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();

                Log.e("Location", TAG + " saveServerUploadSign  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }

            String driverMemo = edit_sign_d_f_memo.getText().toString().trim();
            if (driverMemo.equals("")) {
                Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_must_enter_memo1), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!camera2.hasImage(img_sign_d_f_visit_log)) {
                Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_visit_photo_require), Toast.LENGTH_SHORT).show();
                return;
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context.getResources().getString(R.string.msg_disk_size_error));
                return;
            }


            DataUtil.logEvent("button_click", TAG, com.giosis.library.util.DataUtil.requestSetUploadDeliveryData);

            new DeliveryFailedUploadHelper.Builder(DeliveryFailedActivity.this, opID, officeCode, deviceID,
                    mStrWaybillNo, driverMemo, img_sign_d_f_visit_log,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnServerEventListener(new OnServerEventListener() {

                        @Override
                        public void onPostResult() {

                            DataUtil.inProgressListPosition = 0;
                            finish();
                        }

                        @Override
                        public void onPostFailList() {
                        }
                    }).build().execute();
        } catch (Exception e) {

            Log.e("krm0219", TAG + "  Exception : " + e.toString());
            Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    private void AlertShow(String msg) {
        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
        alert_internet_status.setTitle(context.getResources().getString(R.string.text_warning));
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(context.getResources().getString(R.string.button_close),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 닫기
                        finish();
                    }
                });
        alert_internet_status.show();
    }


    // Gallery
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


    // CAMERA
    private void openCamera() {

        CameraManager cameraManager = camera2.getCameraManager(this);
        cameraId = camera2.getCameraCharacteristics(cameraManager);

        Log.e("krm0219", TAG + "  openCamera " + cameraId);

        if (cameraId != null) {

            camera2.setCameraDevice(cameraManager, cameraId);
        } else {

            Toast.makeText(DeliveryFailedActivity.this, context.getResources().getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show();
        }
    }

    private void closeCamera() {

        camera2.closeCamera();
    }

    @Override
    public void onCameraDeviceOpened(CameraDevice cameraDevice, Size cameraSize, int rotation) {

        texture_sign_d_f_preview.setRotation(rotation);

        SurfaceTexture texture = texture_sign_d_f_preview.getSurfaceTexture();
        texture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
        // texture.setDefaultBufferSize(1088, 1088);
        Surface surface = new Surface(texture);

        camera2.setCaptureSessionRequest(cameraDevice, surface);
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