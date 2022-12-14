package com.giosis.util.qdrive.singapore.list.delivery;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.util.qdrive.singapore.MemoryStatus;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager;
import com.giosis.util.qdrive.singapore.util.Camera2APIs;
import com.giosis.util.qdrive.singapore.util.CommonActivity;
import com.giosis.util.qdrive.singapore.util.DataUtil;
import com.giosis.util.qdrive.singapore.database.DatabaseHelper;
import com.giosis.util.qdrive.singapore.util.DisplayUtil;
import com.giosis.util.qdrive.singapore.util.FirebaseEvent;
import com.giosis.util.qdrive.singapore.util.NetworkUtil;
import com.giosis.util.qdrive.singapore.util.OnServerEventListener;
import com.giosis.util.qdrive.singapore.util.PermissionActivity;
import com.giosis.util.qdrive.singapore.util.PermissionChecker;
import com.giosis.util.qdrive.singapore.util.Preferences;


// TODO_kjyoo _TEST
public class QuickReturnFailedActivity extends CommonActivity implements Camera2APIs.Camera2Interface, TextureView.SurfaceTextureListener {
    String TAG = "QuickReturnFailedActivity";

    //
    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_sign_d_r_f_tracking_no;
    TextView text_sign_d_r_f_receiver;
    TextView text_sign_d_r_f_sender;
    EditText edit_sign_d_r_f_memo;

    LinearLayout layout_sign_d_r_f_take_photo;
    LinearLayout layout_sign_d_r_f_gallery;
    TextureView texture_sign_d_r_f_preview;
    ImageView img_sign_d_r_f_preview_bg;
    ImageView img_sign_d_r_f_visit_log;
    Button btn_sign_d_r_f_save;

    String opID = "";
    String officeCode = "";
    String deviceID = "";

    String mStrWaybillNo;
    String receiverName;
    String senderName;

    // Camera & Gallery
    Camera2APIs camera2;
    String cameraId;
    boolean isClickedPhoto = false;
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
        setContentView(R.layout.activity_quick_returned_visit_log);

        FirebaseEvent.INSTANCE.createEvent(this, TAG);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_d_r_f_tracking_no = findViewById(R.id.text_sign_d_r_f_tracking_no);
        text_sign_d_r_f_receiver = findViewById(R.id.text_sign_d_r_f_receiver);
        text_sign_d_r_f_sender = findViewById(R.id.text_sign_d_r_f_sender);
        edit_sign_d_r_f_memo = findViewById(R.id.edit_sign_d_r_f_memo);

        layout_sign_d_r_f_take_photo = findViewById(R.id.layout_sign_d_r_f_take_photo);
        layout_sign_d_r_f_gallery = findViewById(R.id.layout_sign_d_r_f_gallery);
        texture_sign_d_r_f_preview = findViewById(R.id.texture_sign_d_r_f_preview);
        img_sign_d_r_f_preview_bg = findViewById(R.id.img_sign_d_r_f_preview_bg);
        img_sign_d_r_f_visit_log = findViewById(R.id.img_sign_d_r_f_visit_log);
        btn_sign_d_r_f_save = findViewById(R.id.btn_sign_d_r_f_save);

        //-----
        camera2 = new Camera2APIs(this);
        opID = Preferences.INSTANCE.getUserId();
        officeCode = Preferences.INSTANCE.getOfficeCode();
        deviceID = Preferences.INSTANCE.getDeviceUUID();

        String strTitle = getIntent().getStringExtra("title");
        mStrWaybillNo = getIntent().getStringExtra("waybillNo");

        getDeliveryInfo(mStrWaybillNo);

        text_top_title.setText(strTitle);
        text_sign_d_r_f_tracking_no.setText(mStrWaybillNo);
        text_sign_d_r_f_receiver.setText(receiverName);
        text_sign_d_r_f_sender.setText(senderName);
        DisplayUtil.setPreviewCamera(img_sign_d_r_f_preview_bg);

        layout_top_back.setOnClickListener(view -> cancelSigning());

        layout_sign_d_r_f_take_photo.setOnClickListener(view -> {

            if (cameraId != null) {
                if (!isClickedPhoto) {

                    isClickedPhoto = true;
                    camera2.takePhoto(texture_sign_d_r_f_preview, img_sign_d_r_f_visit_log);
                }
            } else {

                Toast.makeText(QuickReturnFailedActivity.this, getResources().getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show();
            }
        });

        layout_sign_d_r_f_gallery.setOnClickListener(v -> getImageFromAlbum());
        btn_sign_d_r_f_save.setOnClickListener(v -> saveServerUploadSign());

        // Memo ????????????
        edit_sign_d_r_f_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (99 <= edit_sign_d_r_f_memo.length()) {
                    Toast.makeText(QuickReturnFailedActivity.this, getResources().getText(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //
        PermissionChecker checker = new PermissionChecker(this);

        // ?????? ?????? ?????? (????????? true, ????????? false)
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

            if (texture_sign_d_r_f_preview.isAvailable()) {
                openCamera();
            } else {
                texture_sign_d_r_f_preview.setSurfaceTextureListener(this);
            }

            // Location
            gpsTrackerManager = new GPSTrackerManager(this);
            gpsEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsEnable && gpsTrackerManager != null) {
                gpsTrackerManager.gpsTrackerStart();

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                Log.e("Location", TAG + " GPSTrackerManager onResume : " + latitude + "  " + longitude + "  ");

            } else {

                DataUtil.enableLocationSettings(QuickReturnFailedActivity.this);
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
                img_sign_d_r_f_visit_log.setImageBitmap(resizeBitmap);
                img_sign_d_r_f_visit_log.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                onResume();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
            Log.e("Permission", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

            isPermissionTrue = true;
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
                .setPositiveButton(R.string.button_ok, (dialog, which) -> finish())
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }


    public void saveServerUploadSign() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {
                AlertShow(getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            if (gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();

                Log.e("Location", TAG + " saveServerUploadSign  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }

            String driverMemo = edit_sign_d_r_f_memo.getText().toString().trim();
            if (driverMemo.equals("")) {
                Toast.makeText(this, getResources().getString(R.string.msg_must_enter_memo1), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!camera2.hasImage(img_sign_d_r_f_visit_log)) {
                Toast.makeText(this, getResources().getString(R.string.msg_visit_photo_require), Toast.LENGTH_SHORT).show();
                return;
            }

            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(getResources().getString(R.string.msg_disk_size_error));
                return;
            }

            FirebaseEvent.INSTANCE.clickEvent(this, TAG, "setDeliveryRTNDPTypeUploadData");

            new QuickReturnFailedUploadHelper.Builder(this, opID, officeCode, deviceID,
                    mStrWaybillNo, driverMemo, img_sign_d_r_f_visit_log,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnServerEventListener(new OnServerEventListener() {

                        @Override
                        public void onPostResult() {

                            finish();
                        }

                        @Override
                        public void onPostFailList() {
                        }
                    }).build().execute();
        } catch (Exception e) {

            Toast.makeText(this, getResources().getString(R.string.text_error) + " - " + e, Toast.LENGTH_SHORT).show();
        }
    }

    private void AlertShow(String msg) {
        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
        alert_internet_status.setTitle(getResources().getString(R.string.text_warning));
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(getResources().getString(R.string.button_close),
                (dialog, which) -> {
                    dialog.dismiss(); // ??????
                    finish();
                });
        alert_internet_status.show();
    }


    public void getDeliveryInfo(String barcodeNo) {

        Cursor cursor = DatabaseHelper.getInstance().get("SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        if (cursor.moveToFirst()) {
            receiverName = cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"));
            senderName = cursor.getString(cursor.getColumnIndexOrThrow("sender_nm"));
        }

        cursor.close();
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
        }
    }


    // CAMERA
    private void openCamera() {

        CameraManager cameraManager = camera2.getCameraManager(this);
        cameraId = camera2.getCameraCharacteristics(cameraManager);

        Log.e("Camera", TAG + "  openCamera " + cameraId);

        if (cameraId != null) {
            camera2.setCameraDevice(cameraManager, cameraId);
        } else {
            Toast.makeText(QuickReturnFailedActivity.this, getResources().getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show();
        }
    }

    private void closeCamera() {
        camera2.closeCamera();
    }

    @Override
    public void onCameraDeviceOpened(CameraDevice cameraDevice, Size cameraSize, int rotation, String it) {
        Log.e("Camera", "onCameraDeviceOpened  " + it);
        texture_sign_d_r_f_preview.setRotation(rotation);

        SurfaceTexture texture = texture_sign_d_r_f_preview.getSurfaceTexture();
        texture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
        Surface surface = new Surface(texture);

        camera2.setCaptureSessionRequest(cameraDevice, surface);
    }

    @Override
    public void onCaptureCompleted() {

        isClickedPhoto = false;
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