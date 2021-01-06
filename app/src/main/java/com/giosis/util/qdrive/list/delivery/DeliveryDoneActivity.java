package com.giosis.util.qdrive.list.delivery;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.giosis.util.qdrive.barcodescanner.CaptureActivity;
import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.list.BarcodeData;
import com.giosis.util.qdrive.list.OutletInfo;
import com.giosis.util.qdrive.list.SigningView;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.Camera2APIs;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.MemoryStatus;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.ui.CommonActivity;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;


/***************
 * @author jtpark
 * @editor krm0219
 * LIST, In Progress > 'Delivered'  // SCAN > Delivery Done
 * 2020.06 사진 추가
 */
public class DeliveryDoneActivity extends CommonActivity implements Camera2APIs.Camera2Interface, TextureView.SurfaceTextureListener {
    String TAG = "DeliveryDoneActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    //
    TextView text_sign_d_tracking_no_title;
    TextView text_sign_d_tracking_no;
    TextView text_sign_d_tracking_no_more;
    LinearLayout layout_sign_d_receiver;
    TextView text_sign_d_receiver;
    ImageView img_sign_d_receiver_self;
    TextView text_sign_d_receiver_self;
    ImageView img_sign_d_receiver_substitute;
    TextView text_sign_d_receiver_substitute;
    ImageView img_sign_d_receiver_other;
    TextView text_sign_d_receiver_other;
    LinearLayout layout_sign_d_sender;
    TextView text_sign_d_sender;

    LinearLayout layout_sign_d_sign_memo;
    EditText edit_sign_d_memo;
    LinearLayout layout_sign_d_sign_eraser;
    SigningView sign_view_sign_d_signature;
    LinearLayout layout_sign_d_visit_log;
    LinearLayout layout_sign_d_take_photo;
    LinearLayout layout_sign_d_gallery;
    TextureView texture_sign_d_preview;
    ImageView img_sign_d_preview_bg;
    ImageView img_sign_d_visit_log;
    Button btn_sign_d_save;

    // Outlet
    LinearLayout layout_sign_d_outlet_address;
    TextView text_sign_d_outlet_address_title;
    TextView text_sign_d_outlet_address;
    LinearLayout layout_sign_d_outlet_operation_hour;
    TextView text_sign_d_outlet_operation_time;
    ListView list_sign_d_outlet_list;

    //
    Context context;
    String opID = "";
    String officeCode = "";
    String deviceID = "";

    String mStrWaybillNo = "";
    String mReceiveType = "RC";
    String mType = BarcodeType.TYPE_DELIVERY;
    String routeNumber;

    ArrayList<BarcodeData> songjanglist;
    String senderName;
    String receiverName;


    // Camera & Gallery
    Camera2APIs camera2;
    String cameraId;
    private static final int RESULT_LOAD_IMAGE = 3;
    boolean isGalleryActivate = false;

    // GPS
    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    // Outlet
    OutletInfo outletInfo;
    String jobID;
    String vendorCode;
    boolean showQRCode = false;
    ArrayList<OutletDeliveryDoneListItem> outletDeliveryDoneListItemArrayList;
    OutletTrackingNoAdapter outletTrackingNoAdapter;


    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE,
            PermissionChecker.ACCESS_COARSE_LOCATION, PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.CAMERA};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivered);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_d_tracking_no_title = findViewById(R.id.text_sign_d_tracking_no_title);
        text_sign_d_tracking_no = findViewById(R.id.text_sign_d_tracking_no);
        text_sign_d_tracking_no_more = findViewById(R.id.text_sign_d_tracking_no_more);
        layout_sign_d_receiver = findViewById(R.id.layout_sign_d_receiver);
        text_sign_d_receiver = findViewById(R.id.text_sign_d_receiver);
        img_sign_d_receiver_self = findViewById(R.id.img_sign_d_receiver_self);
        text_sign_d_receiver_self = findViewById(R.id.text_sign_d_receiver_self);
        img_sign_d_receiver_substitute = findViewById(R.id.img_sign_d_receiver_substitute);
        text_sign_d_receiver_substitute = findViewById(R.id.text_sign_d_receiver_substitute);
        img_sign_d_receiver_other = findViewById(R.id.img_sign_d_receiver_other);
        text_sign_d_receiver_other = findViewById(R.id.text_sign_d_receiver_other);
        layout_sign_d_sender = findViewById(R.id.layout_sign_d_sender);
        text_sign_d_sender = findViewById(R.id.text_sign_d_sender);

        layout_sign_d_sign_memo = findViewById(R.id.layout_sign_d_sign_memo);
        edit_sign_d_memo = findViewById(R.id.edit_sign_d_memo);
        layout_sign_d_sign_eraser = findViewById(R.id.layout_sign_d_sign_eraser);
        sign_view_sign_d_signature = findViewById(R.id.sign_view_sign_d_signature);
        layout_sign_d_visit_log = findViewById(R.id.layout_sign_d_visit_log);
        layout_sign_d_take_photo = findViewById(R.id.layout_sign_d_take_photo);
        layout_sign_d_gallery = findViewById(R.id.layout_sign_d_gallery);
        texture_sign_d_preview = findViewById(R.id.texture_sign_d_preview);
        img_sign_d_preview_bg = findViewById(R.id.img_sign_d_preview_bg);
        img_sign_d_visit_log = findViewById(R.id.img_sign_d_visit_log);
        btn_sign_d_save = findViewById(R.id.btn_sign_d_save);

        // Outlet
        layout_sign_d_outlet_address = findViewById(R.id.layout_sign_d_outlet_address);
        text_sign_d_outlet_address_title = findViewById(R.id.text_sign_d_outlet_address_title);
        text_sign_d_outlet_address = findViewById(R.id.text_sign_d_outlet_address);
        layout_sign_d_outlet_operation_hour = findViewById(R.id.layout_sign_d_outlet_operation_hour);
        text_sign_d_outlet_operation_time = findViewById(R.id.text_sign_d_outlet_operation_time);
        list_sign_d_outlet_list = findViewById(R.id.list_sign_d_outlet_list);


        //
        layout_top_back.setOnClickListener(clickListener);
        img_sign_d_receiver_self.setOnClickListener(clickListener);
        text_sign_d_receiver_self.setOnClickListener(clickListener);
        img_sign_d_receiver_substitute.setOnClickListener(clickListener);
        text_sign_d_receiver_substitute.setOnClickListener(clickListener);
        img_sign_d_receiver_other.setOnClickListener(clickListener);
        text_sign_d_receiver_other.setOnClickListener(clickListener);
        layout_sign_d_sign_eraser.setOnClickListener(clickListener);
        layout_sign_d_take_photo.setOnClickListener(clickListener);
        layout_sign_d_gallery.setOnClickListener(clickListener);
        btn_sign_d_save.setOnClickListener(clickListener);


        //
        context = getApplicationContext();
        camera2 = new Camera2APIs(this);
        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();
        deviceID = MyApplication.preferences.getDeviceUUID();

        mStrWaybillNo = getIntent().getStringExtra("waybillNo");
        ArrayList<CaptureActivity.BarcodeListData> barcodeList = (ArrayList<CaptureActivity.BarcodeListData>) getIntent().getSerializableExtra("data");


        try {

            String routeType = getIntent().getStringExtra("route");
            String[] routeSplit = routeType.split(" ");
            routeNumber = routeSplit[0] + " " + routeSplit[1];
        } catch (Exception e) {

            routeNumber = null;
        }

        // 단건 다수건 바코드정보에 대한 바코드정보 리스트 재정의 songjanglist
        songjanglist = new ArrayList<>();
        BarcodeData songData;

        if (barcodeList == null) {

            songData = new BarcodeData();
            songData.setBarcode(mStrWaybillNo.toUpperCase());
            songData.setState(mType);
            songjanglist.add(songData);
        } else {

            int size = barcodeList.size();

            for (int i = 0; i < size; i++) {
                songData = new BarcodeData();
                songData.setBarcode(barcodeList.get(i).getBarcode().toUpperCase());
                songData.setState(barcodeList.get(i).getState());
                songjanglist.add(songData);
            }
        }

        String barcodeMsg = "";
        int songJangListSize = songjanglist.size();
        for (int i = 0; i < songJangListSize; i++) {
            barcodeMsg += songjanglist.get(i).getBarcode().toUpperCase() + "  ";
        }

        text_sign_d_tracking_no_title.setText(R.string.text_tracking_no);
        if (songJangListSize > 1) {  //다수건

            String qtyFormat = String.format(context.getResources().getString(R.string.text_total_qty_count), songJangListSize);
            text_sign_d_tracking_no.setText(qtyFormat);
            text_sign_d_tracking_no_more.setVisibility(View.VISIBLE);
            text_sign_d_tracking_no_more.setText(barcodeMsg);
            layout_sign_d_sender.setVisibility(View.GONE);
        } else {  //1건

            text_sign_d_tracking_no.setText(barcodeMsg.trim());
            text_sign_d_tracking_no_more.setVisibility(View.GONE);
        }


        getDeliveryInfo(songjanglist.get(0).getBarcode());
        outletInfo = getOutletInfo(songjanglist.get(0).getBarcode());

        text_top_title.setText(R.string.text_delivered);
        text_sign_d_receiver.setText(receiverName);
        text_sign_d_sender.setText(senderName);
        DisplayUtil.setPreviewCamera(img_sign_d_preview_bg);

        Log.e("krm0219", TAG + "  Outlet info Route : " + outletInfo.route.substring(0, 2) + " / " + outletInfo.route);

        // NOTIFICATION.  Outlet Delivery
        if (outletInfo.route.substring(0, 2).contains("7E") || outletInfo.route.substring(0, 2).contains("FL")) {

            layout_sign_d_outlet_address.setVisibility(View.VISIBLE);
            text_sign_d_outlet_address.setText("(" + outletInfo.zip_code + ") " + outletInfo.address);

            // 2019.04
            String outletAddress = outletInfo.address.toUpperCase();
            String operationHour = null;
            Log.e("krm0219", "Operation Address : " + outletInfo.address);

            if (outletAddress.contains(context.getResources().getString(R.string.text_operation_hours).toUpperCase())) {

                String indexString = "(" + context.getResources().getString(R.string.text_operation_hours).toUpperCase() + ":";
                int operationHourIndex = outletAddress.indexOf(indexString);

                operationHour = outletInfo.address.substring(operationHourIndex + indexString.length(), outletAddress.length() - 1);
                outletAddress = outletInfo.address.substring(0, operationHourIndex);
                Log.e("krm0219", "Operation Hour : " + operationHour);
            } else if (outletAddress.contains(context.getResources().getString(R.string.text_operation_hour).toUpperCase())) {

                String indexString = "(" + context.getResources().getString(R.string.text_operation_hour).toUpperCase() + ":";
                int operationHourIndex = outletAddress.indexOf(indexString);

                operationHour = outletInfo.address.substring(operationHourIndex + indexString.length(), outletAddress.length() - 1);
                outletAddress = outletInfo.address.substring(0, operationHourIndex);
                Log.e("krm0219", "Operation Hour : " + operationHour);
            }

            if (operationHour != null) {

                layout_sign_d_outlet_operation_hour.setVisibility(View.VISIBLE);
                text_sign_d_outlet_operation_time.setText(operationHour);
            }

            text_sign_d_outlet_address.setText("(" + outletInfo.zip_code + ") " + outletAddress);


            text_sign_d_tracking_no_more.setVisibility(View.GONE);
            layout_sign_d_receiver.setVisibility(View.GONE);
            list_sign_d_outlet_list.setVisibility(View.VISIBLE);


            outletDeliveryDoneListItemArrayList = new ArrayList<>();

            DatabaseHelper dbHelper = DatabaseHelper.getInstance();

            if (routeNumber == null) {      // SCAN > Delivery Done

                for (int i = 0; i < songjanglist.size(); i++) {

                    Cursor cs = dbHelper.get("SELECT rcv_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + opID + "' and invoice_no='" + songjanglist.get(i).getBarcode() + "'");

                    if (cs.moveToFirst()) {
                        do {

                            String receiver_name = cs.getString(cs.getColumnIndex("rcv_nm"));

                            OutletDeliveryDoneListItem outletDeliveryDoneListItem = new OutletDeliveryDoneListItem();
                            outletDeliveryDoneListItem.setTrackingNo(songjanglist.get(i).getBarcode());
                            outletDeliveryDoneListItem.setReceiverName(receiver_name);
                            outletDeliveryDoneListItemArrayList.add(outletDeliveryDoneListItem);
                        } while (cs.moveToNext());
                    }
                }
            } else {    // LIST > In Progress

                songjanglist = new ArrayList<>();
                BarcodeData barcodeData;

                Cursor cs = dbHelper.get("SELECT invoice_no, rcv_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + opID + "' and route LIKE '%" + routeNumber + "%'");

                if (cs.moveToFirst()) {
                    do {

                        String invoice_no = cs.getString(cs.getColumnIndex("invoice_no"));
                        String receiver_name = cs.getString(cs.getColumnIndex("rcv_nm"));

                        barcodeData = new BarcodeData();
                        barcodeData.setBarcode(invoice_no);
                        barcodeData.setState(mType);
                        songjanglist.add(barcodeData);

                        OutletDeliveryDoneListItem outletDeliveryDoneListItem = new OutletDeliveryDoneListItem();
                        outletDeliveryDoneListItem.setTrackingNo(invoice_no);
                        outletDeliveryDoneListItem.setReceiverName(receiver_name);
                        outletDeliveryDoneListItemArrayList.add(outletDeliveryDoneListItem);
                    } while (cs.moveToNext());
                }

                if (outletDeliveryDoneListItemArrayList.size() > 1) {

                    String qtyFormat = String.format(context.getResources().getString(R.string.text_total_qty_count), outletDeliveryDoneListItemArrayList.size());
                    text_sign_d_tracking_no_title.setText(R.string.text_parcel_qty1);
                    text_sign_d_tracking_no.setText(qtyFormat);
                    layout_sign_d_sender.setVisibility(View.GONE);
                }
            }


            if ((outletInfo.route.substring(0, 2).contains("7E"))) {

                text_top_title.setText(R.string.text_title_7e_store_delivery);
                text_sign_d_outlet_address_title.setText(R.string.text_7e_store_address);
                layout_sign_d_sign_memo.setVisibility(View.VISIBLE);
                layout_sign_d_visit_log.setVisibility(View.VISIBLE);

                if (!NetworkUtil.isNetworkAvailable(context)) {

                    AlertShow(context.getResources().getString(R.string.text_warning), context.getResources().getString(R.string.msg_network_connect_error), context.getResources().getString(R.string.button_close));
                    return;
                } else {

                    QRCodeAsyncTask qrCodeAsyncTask = new QRCodeAsyncTask(getString(R.string.text_outlet_7e), outletDeliveryDoneListItemArrayList);
                    qrCodeAsyncTask.execute();
                }
            } else {

                text_top_title.setText(R.string.text_title_fl_delivery);
                text_sign_d_outlet_address_title.setText(R.string.text_federated_locker_address);
                layout_sign_d_sign_memo.setVisibility(View.GONE);
                layout_sign_d_visit_log.setVisibility(View.GONE);

                outletTrackingNoAdapter = new OutletTrackingNoAdapter(DeliveryDoneActivity.this, outletDeliveryDoneListItemArrayList, "FL");
                list_sign_d_outlet_list.setAdapter(outletTrackingNoAdapter);
                setListViewHeightBasedOnChildren(list_sign_d_outlet_list);
            }
        } else {

            layout_sign_d_outlet_address.setVisibility(View.GONE);
            layout_sign_d_outlet_operation_hour.setVisibility(View.GONE);
            layout_sign_d_receiver.setVisibility(View.VISIBLE);
            list_sign_d_outlet_list.setVisibility(View.GONE);
            layout_sign_d_sign_memo.setVisibility(View.VISIBLE);
            layout_sign_d_visit_log.setVisibility(View.VISIBLE);
        }


        // Memo 입력제한
        edit_sign_d_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (99 <= edit_sign_d_memo.length()) {
                    Toast.makeText(context, context.getResources().getText(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        // 권한 여부 체크 (없으면 true, 있으면 false)
        PermissionChecker checker = new PermissionChecker(this);

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

            if (texture_sign_d_preview.isAvailable()) {

                openCamera();
            } else {

                texture_sign_d_preview.setSurfaceTextureListener(this);
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

                DataUtil.enableLocationSettings(DeliveryDoneActivity.this, context);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isGalleryActivate = false;

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {

            try {

                Uri selectedImageUri = data.getData();

                Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                Bitmap resizeBitmap = camera2.getResizeBitmap(selectedImage);
                img_sign_d_visit_log.setImageBitmap(resizeBitmap);
                img_sign_d_visit_log.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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
    protected void onDestroy() {
        super.onDestroy();
        DataUtil.stopGPSManager(gpsTrackerManager);
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

                        setResult(Activity.RESULT_CANCELED);
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


    public void confirmSigning() {

        if (outletInfo.route.contains("7E")) {
            if (showQRCode) {        // QR Code Show

                saveOutletDeliveryDone();
            } else {                // QR Code Not Show... > 진행 불가능

                Toast.makeText(DeliveryDoneActivity.this, context.getResources().getString(R.string.msg_outlet_qrcode_require), Toast.LENGTH_SHORT).show();
            }
        } else if (outletInfo.route.contains("FL")) {

            saveOutletDeliveryDone();
        } else {

            saveServerUploadSign();
        }
    }


    public void getDeliveryInfo(String barcodeNo) {

        Cursor cursor = DatabaseHelper.getInstance().get("SELECT rcv_nm, sender_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        if (cursor.moveToFirst()) {
            receiverName = cursor.getString(cursor.getColumnIndexOrThrow("rcv_nm"));
            senderName = cursor.getString(cursor.getColumnIndexOrThrow("sender_nm"));
        }

        cursor.close();
    }

    public OutletInfo getOutletInfo(String barcodeNo) {

        Cursor cursor = DatabaseHelper.getInstance().get("SELECT route, zip_code, address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        OutletInfo outletInfo = new OutletInfo();

        if (cursor.moveToFirst()) {
            outletInfo.route = cursor.getString(cursor.getColumnIndexOrThrow("route"));
            outletInfo.zip_code = cursor.getString(cursor.getColumnIndexOrThrow("zip_code"));
            outletInfo.address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        }

        cursor.close();

        return outletInfo;
    }


    private void AlertShow(final String title, String msg, String btnText) {

        final AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
        alert_internet_status.setTitle(title);
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(btnText,
                (dialog, which) -> {

                    if (title.contains("Result")) {

                    } else {
                        dialog.dismiss(); // 닫기
                        finish();
                    }
                });
        alert_internet_status.show();
    }


    /*
     * 실시간 Upload 처리
     * add by jmkang 2014-01-22
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

            String driverMemo = edit_sign_d_memo.getText().toString();

            // NOTIFICATION. 2020.06  visit log 추가
            // 사인 or 사진 둘 중 하나는 있어야 함
            boolean hasSignImage = sign_view_sign_d_signature.getIsTouche();
            boolean hasVisitImage = camera2.hasImage(img_sign_d_visit_log);
            Log.e("krm0219", TAG + "  has DATA : " + hasSignImage + " / " + hasVisitImage);

            if (!hasSignImage && !hasVisitImage) {

                String msg = context.getResources().getString(R.string.msg_signature_require) + " or \n" + context.getResources().getString(R.string.msg_visit_photo_require);

                Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                return;
            }

            //서버에 올리기전 용량체크  내장메모리가 100Kbyte 안남은경우
            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context.getResources().getString(R.string.text_warning), context.getResources().getString(R.string.msg_disk_size_error), context.getResources().getString(R.string.button_close));
                return;
            }


            DataUtil.logEvent("button_click", TAG, com.giosis.library.util.DataUtil.requestSetUploadDeliveryData);

//            DataUtil.captureSign("/Qdrive", songjanglist.get(0).getBarcode(), sign_view_sign_d_signature);
//            DataUtil.captureSign("/Qdrive", songjanglist.get(0).getBarcode() + "_1", img_sign_d_visit_log);

            new DeliveryDoneUploadHelper.Builder(this, opID, officeCode, deviceID,
                    songjanglist, mReceiveType, driverMemo,
                    sign_view_sign_d_signature, hasSignImage, img_sign_d_visit_log, hasVisitImage,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnServerUploadEventListener(() -> {

                        DataUtil.inProgressListPosition = 0;

                        setResult(Activity.RESULT_OK);
                        finish();
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

    public void saveOutletDeliveryDone() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {

                AlertShow(context.getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            if (gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();

                Log.e("Location", TAG + " saveOutletDeliveryDone  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }

            if (outletInfo.route.substring(0, 2).contains("7E")) {
                if (!sign_view_sign_d_signature.getIsTouche()) {
                    Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(context.getResources().getString(R.string.text_warning), context.getResources().getString(R.string.msg_disk_size_error), context.getResources().getString(R.string.button_close));
                return;
            }

            String driverMemo = edit_sign_d_memo.getText().toString();

            DataUtil.logEvent("button_click", TAG + "_OUTLET", "SetOutletDeliveryUploadData");

            // 2019.02 - stat : D3 로..   서버에서 outlet stat 변경
            new OutletDeliveryDoneHelper.Builder(this, opID, officeCode, deviceID,
                    songjanglist, outletInfo.route.substring(0, 2), mReceiveType, sign_view_sign_d_signature, driverMemo,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnOutletDataUploadEventListener(new OnOutletDataUploadEventListener() {

                        @Override
                        public void onPostResult() {

                            setResult(Activity.RESULT_OK);
                            finish();
                        }
                    }).build().execute();
        } catch (Exception e) {

            Log.e("krm0219", "Exception ; " + e.toString());
            Toast.makeText(this.getApplicationContext(), context.getResources().getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.layout_top_back: {

                    cancelSigning();
                }
                break;

                case R.id.img_sign_d_receiver_self:
                case R.id.text_sign_d_receiver_self: {

                    img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
                    img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                    img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);

                    mReceiveType = "RC";
                }
                break;

                case R.id.img_sign_d_receiver_substitute:
                case R.id.text_sign_d_receiver_substitute: {

                    img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                    img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
                    img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);

                    mReceiveType = "AG";
                }
                break;

                case R.id.img_sign_d_receiver_other:
                case R.id.text_sign_d_receiver_other: {

                    img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                    img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                    img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);

                    mReceiveType = "ET";
                }
                break;

                case R.id.layout_sign_d_sign_eraser: {

                    sign_view_sign_d_signature.clearText();
                }
                break;

                case R.id.layout_sign_d_take_photo: {

                    if (cameraId != null) {

                        camera2.takePhoto(texture_sign_d_preview, img_sign_d_visit_log);
                    } else {

                        Toast.makeText(DeliveryDoneActivity.this, context.getResources().getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show();
                    }
                }
                break;

                case R.id.layout_sign_d_gallery: {

                    getImageFromAlbum();
                }
                break;

                case R.id.btn_sign_d_save: {

                    confirmSigning();
                }
                break;
            }
        }
    };


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

            Toast.makeText(DeliveryDoneActivity.this, context.getResources().getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show();
        }
    }


    private void closeCamera() {

        camera2.closeCamera();
    }


    @Override
    public void onCameraDeviceOpened(CameraDevice cameraDevice, Size cameraSize, int rotation) {

        texture_sign_d_preview.setRotation(rotation);

        SurfaceTexture texture = texture_sign_d_preview.getSurfaceTexture();
        texture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
        Surface surface = new Surface(texture);

        camera2.setCaptureSessionRequest(cameraDevice, surface);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }


    public class QRCodeAsyncTask extends AsyncTask<Void, Void, String> {

        String outlet_type;
        ArrayList<OutletDeliveryDoneListItem> outletDeliveryDoneListItemArrayList;
        ArrayList<QRCodeResult> qrCodeResultArrayList;
        String imgUrl;

        ProgressDialog progressDialog = new ProgressDialog(DeliveryDoneActivity.this);

        public QRCodeAsyncTask(String outletType, ArrayList<OutletDeliveryDoneListItem> outletDeliveryDoneListItemArrayList) {

            this.outlet_type = outletType;
            this.outletDeliveryDoneListItemArrayList = outletDeliveryDoneListItemArrayList;

            qrCodeResultArrayList = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(context.getResources().getString(R.string.text_please_wait));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {

                for (int i = 0; i < outletDeliveryDoneListItemArrayList.size(); i++) {

                    QRCodeResult result = getQRCodeData(outletDeliveryDoneListItemArrayList.get(i).getTrackingNo());

                    if (result != null) {

                       /* //    TODO 7E TEST
                        //   ServerDownloadHelper.java 에서 테스트 데이터 넣고  테스트 가능
                        //   테스트 데이터를 넣은 만큼 데이터 셋팅
                        if (i == 0) {
                            result.setQrcode_data("{\"Q\":\"D\",\"J\":\"CR20181022001\",\"V\":\"QT\",\"S\":\"\",\"C\":1}");
                        } else if (i == 1) {
                            result.setQrcode_data("{\"Q\":\"D\",\"J\":\"CR20181107001\",\"V\":\"QT\",\"S\":\"\",\"C\":1}");
                        } else if (i == 2) {
                            result.setQrcode_data("{\"Q\":\"D\",\"J\":\"CR20181022001\",\"V\":\"QT\",\"S\":\"\",\"C\":1}");
                        }*/

                        JSONObject jsonObject = new JSONObject(result.getQrcode_data());
                        String type = jsonObject.getString("Q");

                        if (!type.equals("D")) {

                            return null;
                        }

                        jobID = jsonObject.getString("J");
                        if (jobID == null || jobID.equalsIgnoreCase("")) {

                            return null;
                        } else {
                            outletDeliveryDoneListItemArrayList.get(i).setJobID(jobID);
                        }

                        vendorCode = jsonObject.getString("V");
                        outletDeliveryDoneListItemArrayList.get(i).setVendorCode(vendorCode);

                        imgUrl = DataUtil.qrcode_url + result.getQrcode_data();
                        // test Data. https://dp.image-gmkt.com/qr.bar?scale=7&version=4&code={"Q":"D","J":"CR20190313001","V":"QT","S":"472","C":1}
                        Log.e("krm0219", "QR Code URL > " + imgUrl);
                        outletDeliveryDoneListItemArrayList.get(i).setQrCode(imgUrl);
                    }
                }

                return "SUCCESS";
            } catch (Exception e) {

                Log.e("krm0219", "QRCodeAsyncTask Exception : " + e.toString());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            DisplayUtil.dismissProgressDialog(progressDialog);

            if (result != null) {

                showQRCode = true;

                outletTrackingNoAdapter = new OutletTrackingNoAdapter(DeliveryDoneActivity.this, outletDeliveryDoneListItemArrayList, "7E");
                list_sign_d_outlet_list.setAdapter(outletTrackingNoAdapter);
                setListViewHeightBasedOnChildren(list_sign_d_outlet_list);
            } else {

                showQRCode = false;
                Toast.makeText(context, context.getResources().getString(R.string.msg_outlet_qrcode_data_error), Toast.LENGTH_LONG).show();
            }
        }

        private QRCodeResult getQRCodeData(String tracking_no) {
            Log.e("krm0219", TAG + "  getQRCodeData  " + outlet_type + " / " + tracking_no);

            QRCodeResult resultObj;
            Gson gson = new Gson();

            try {

                JSONObject job = new JSONObject();
                job.accumulate("qstation_type", outlet_type);
                job.accumulate("tracking_id", tracking_no);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


                String methodName = "QRCodeForQStationDelivery";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);

                resultObj = gson.fromJson(jsonString, QRCodeResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  QRCodeForQStationDelivery Json Exception : " + e.toString());
                resultObj = null;
            }

            return resultObj;
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for (int i = 0; i < listAdapter.getCount(); i++) {

            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;

        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}