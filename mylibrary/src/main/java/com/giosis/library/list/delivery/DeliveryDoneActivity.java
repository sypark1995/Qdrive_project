package com.giosis.library.list.delivery;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import com.giosis.library.MemoryStatus;
import com.giosis.library.R;
import com.giosis.library.database.DatabaseHelper;
import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.library.gps.LocationModel;
import com.giosis.library.list.BarcodeData;
import com.giosis.library.list.OutletInfo;
import com.giosis.library.list.RowItem;
import com.giosis.library.list.SigningView;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.BarcodeType;
import com.giosis.library.util.Camera2APIs;
import com.giosis.library.util.CommonActivity;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.DisplayUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.OnServerEventListener;
import com.giosis.library.util.PermissionActivity;
import com.giosis.library.util.PermissionChecker;
import com.giosis.library.util.Preferences;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;


/***************
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
    String opID = "";
    String officeCode = "";
    String deviceID = "";

    String mStrWaybillNo = "";
    String mReceiveType = "RC";
    String mType = BarcodeType.TYPE_DELIVERY;
    String routeNumber;

    ArrayList<BarcodeData> barcodeList;
    String senderName;
    String receiverName;

    String highAmountYn = "N";


    // Camera & Gallery
    Camera2APIs camera2 = new Camera2APIs(this);
    String cameraId;
    boolean isClickedPhoto = false;
    private static final int RESULT_LOAD_IMAGE = 3;
    boolean isGalleryActivate = false;

    // GPS
    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

    LocationModel locationModel = new LocationModel();


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
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int id = view.getId();
            if (id == R.id.layout_top_back) {

                cancelSigning();
            } else if (id == R.id.img_sign_d_receiver_self || id == R.id.text_sign_d_receiver_self) {

                img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
                img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                mReceiveType = "RC";
            } else if (id == R.id.img_sign_d_receiver_substitute || id == R.id.text_sign_d_receiver_substitute) {

                img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
                img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                mReceiveType = "AG";
            } else if (id == R.id.img_sign_d_receiver_other || id == R.id.text_sign_d_receiver_other) {

                img_sign_d_receiver_self.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_d_receiver_substitute.setBackgroundResource(R.drawable.qdrive_btn_icon_check_off);
                img_sign_d_receiver_other.setBackgroundResource(R.drawable.qdrive_btn_icon_check_on);
                mReceiveType = "ET";
            } else if (id == R.id.layout_sign_d_sign_eraser) {

                sign_view_sign_d_signature.clearText();
            } else if (id == R.id.layout_sign_d_take_photo) {

                if (cameraId != null) {
                    if (!isClickedPhoto) {  // Camera CaptureSession 완료되면 다시 클릭할 수 있도록 수정

                        isClickedPhoto = true;
                        camera2.takePhoto(texture_sign_d_preview, img_sign_d_visit_log);
                    }
                } else {

                    Toast.makeText(DeliveryDoneActivity.this, getResources().getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.layout_sign_d_gallery) {

                getImageFromAlbum();
            } else if (id == R.id.btn_sign_d_save) {

                confirmSigning();
            }
        }
    };

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
        camera2 = new Camera2APIs(this);
        opID = Preferences.INSTANCE.getUserId();
        officeCode = Preferences.INSTANCE.getOfficeCode();
        deviceID = Preferences.INSTANCE.getDeviceUUID();


        barcodeList = new ArrayList<>();

        // in List (단건)
        try {

            RowItem parcel = (RowItem) getIntent().getSerializableExtra("parcel");

            BarcodeData songData;
            songData = new BarcodeData();
            songData.setBarcode(parcel.getShipping().toUpperCase());
            songData.setState(BarcodeType.TYPE_DELIVERY);
            barcodeList.add(songData);

            highAmountYn = parcel.getHigh_amount_yn();
            mStrWaybillNo = parcel.getShipping();

            if (!Preferences.INSTANCE.getUserNation().equals("SG")) {
                locationModel.setParcelLocation(parcel.getLat(), parcel.getLng(), parcel.getZip_code(), parcel.getState(), parcel.getCity(), parcel.getStreet());
                Log.e("GPSUpdate", "Parcel " + parcel.getShipping() + " // " + parcel.getLat() + ", " + parcel.getLng() + " // "
                        + parcel.getZip_code() + " - " + parcel.getState() + " - " + parcel.getCity() + " - " + parcel.getStreet());
            }
        } catch (Exception e) {

            Log.e("Exception", "Exception " + e.toString());
        }

        try {

            String routeType = getIntent().getStringExtra("route");
            String[] routeSplit = routeType.split(" ");
            routeNumber = routeSplit[0] + " " + routeSplit[1];
        } catch (Exception e) {

            routeNumber = null;
        }


        // in Capture (bulk)
        try {

            ArrayList<BarcodeData> list = (ArrayList<BarcodeData>) getIntent().getSerializableExtra("data");

            for (int i = 0; i < list.size(); i++) {

                String trackingNo = list.get(i).getBarcode().toUpperCase();

                BarcodeData songData;
                songData = new BarcodeData();
                songData.setBarcode(trackingNo);
                songData.setState(BarcodeType.TYPE_DELIVERY);
                barcodeList.add(songData);

                // 위, 경도 & high amount
                Cursor cs = DatabaseHelper.getInstance().get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + trackingNo + "'");

                if (cs.moveToFirst()) {
                    try {

                        String value = cs.getString(cs.getColumnIndex("high_amount_yn"));

                        if (value.equalsIgnoreCase("Y")) {

                            highAmountYn = value;
                        }
                    } catch (Exception ignore) {
                    }


                    if (!Preferences.INSTANCE.getUserNation().equals("SG")) {
                        if (barcodeList.size() == 1) {

                            double parcelLat = cs.getDouble(cs.getColumnIndex("lat"));
                            double parcelLng = cs.getDouble(cs.getColumnIndex("lng"));
                            String zipCode = cs.getString(cs.getColumnIndex("zip_code"));
                            String state = cs.getString(cs.getColumnIndex("state"));
                            String city = cs.getString(cs.getColumnIndex("city"));
                            String street = cs.getString(cs.getColumnIndex("street"));
                            Log.e("GPSUpdate", "Parcel " + trackingNo + " // " + parcelLat + ", " + parcelLng + " // "
                                    + zipCode + " - " + state + " - " + city + " - " + street);

                            locationModel.setParcelLocation(parcelLat, parcelLng, zipCode, state, city, street);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }


        StringBuilder barcodeMsg = new StringBuilder();
        int size = barcodeList.size();
        for (int i = 0; i < size; i++) {
            barcodeMsg.append(barcodeList.get(i).getBarcode()).append("  ");
        }

        text_sign_d_tracking_no_title.setText(R.string.text_tracking_no);
        if (1 < size) {  // 다수건

            String qtyFormat = String.format(getResources().getString(R.string.text_total_qty_count), size);
            text_sign_d_tracking_no.setText(qtyFormat);
            text_sign_d_tracking_no_more.setVisibility(View.VISIBLE);
            text_sign_d_tracking_no_more.setText(barcodeMsg.toString());
            layout_sign_d_sender.setVisibility(View.GONE);
        } else {  //1건

            text_sign_d_tracking_no.setText(barcodeMsg.toString().trim());
            text_sign_d_tracking_no_more.setVisibility(View.GONE);
        }


        getDeliveryInfo(barcodeList.get(0).getBarcode());
        outletInfo = getOutletInfo(barcodeList.get(0).getBarcode());

        text_top_title.setText(R.string.text_delivered);
        text_sign_d_receiver.setText(receiverName);
        text_sign_d_sender.setText(senderName);
        DisplayUtil.setPreviewCamera(img_sign_d_preview_bg);

        // NOTIFICATION.  Outlet Delivery
        if (outletInfo.getRoute() != null) {
            if (outletInfo.getRoute().substring(0, 2).contains("7E") || outletInfo.getRoute().substring(0, 2).contains("FL")) {

                layout_sign_d_outlet_address.setVisibility(View.VISIBLE);
                text_sign_d_outlet_address.setText("(" + outletInfo.getZip_code() + ") " + outletInfo.getAddress());

                // 2019.04
                String outletAddress = outletInfo.getAddress().toUpperCase();
                String operationHour = null;
                Log.e(TAG, "Operation Address : " + outletInfo.getAddress());

                if (outletAddress.contains(getResources().getString(R.string.text_operation_hours).toUpperCase())) {

                    String indexString = "(" + getResources().getString(R.string.text_operation_hours).toUpperCase() + ":";
                    int operationHourIndex = outletAddress.indexOf(indexString);

                    operationHour = outletInfo.getAddress().substring(operationHourIndex + indexString.length(), outletAddress.length() - 1);
                    outletAddress = outletInfo.getAddress().substring(0, operationHourIndex);
                    Log.e(TAG, "Operation Hour : " + operationHour);
                } else if (outletAddress.contains(getResources().getString(R.string.text_operation_hour).toUpperCase())) {

                    String indexString = "(" + getResources().getString(R.string.text_operation_hour).toUpperCase() + ":";
                    int operationHourIndex = outletAddress.indexOf(indexString);

                    operationHour = outletInfo.getAddress().substring(operationHourIndex + indexString.length(), outletAddress.length() - 1);
                    outletAddress = outletInfo.getAddress().substring(0, operationHourIndex);
                    Log.e(TAG, "Operation Hour : " + operationHour);
                }

                if (operationHour != null) {

                    layout_sign_d_outlet_operation_hour.setVisibility(View.VISIBLE);
                    text_sign_d_outlet_operation_time.setText(operationHour);
                }

                text_sign_d_outlet_address.setText("(" + outletInfo.getZip_code() + ") " + outletAddress);


                text_sign_d_tracking_no_more.setVisibility(View.GONE);
                layout_sign_d_receiver.setVisibility(View.GONE);
                list_sign_d_outlet_list.setVisibility(View.VISIBLE);


                outletDeliveryDoneListItemArrayList = new ArrayList<>();

                DatabaseHelper dbHelper = DatabaseHelper.getInstance();

                if (routeNumber == null) {      // SCAN > Delivery Done

                    for (int i = 0; i < barcodeList.size(); i++) {

                        Cursor cs = dbHelper.get("SELECT rcv_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST
                                + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + opID + "' and invoice_no='" + barcodeList.get(i).getBarcode() + "'");

                        if (cs.moveToFirst()) {
                            do {

                                String receiver_name = cs.getString(cs.getColumnIndex("rcv_nm"));

                                OutletDeliveryDoneListItem outletDeliveryDoneListItem = new OutletDeliveryDoneListItem();
                                outletDeliveryDoneListItem.setTrackingNo(barcodeList.get(i).getBarcode());
                                outletDeliveryDoneListItem.setReceiverName(receiver_name);
                                outletDeliveryDoneListItemArrayList.add(outletDeliveryDoneListItem);
                            } while (cs.moveToNext());
                        }
                    }
                } else {    // LIST > In Progress

                    barcodeList = new ArrayList<>();
                    BarcodeData barcodeData;

                    Cursor cs = dbHelper.get("SELECT invoice_no, rcv_nm FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST
                            + " WHERE punchOut_stat = 'N' and chg_dt is null and type = 'D' and reg_id='" + opID + "' and route LIKE '%" + routeNumber + "%'");

                    if (cs.moveToFirst()) {
                        do {

                            String invoice_no = cs.getString(cs.getColumnIndex("invoice_no"));
                            String receiver_name = cs.getString(cs.getColumnIndex("rcv_nm"));

                            barcodeData = new BarcodeData();
                            barcodeData.setBarcode(invoice_no);
                            barcodeData.setState(mType);
                            barcodeList.add(barcodeData);

                            OutletDeliveryDoneListItem outletDeliveryDoneListItem = new OutletDeliveryDoneListItem();
                            outletDeliveryDoneListItem.setTrackingNo(invoice_no);
                            outletDeliveryDoneListItem.setReceiverName(receiver_name);
                            outletDeliveryDoneListItemArrayList.add(outletDeliveryDoneListItem);
                        } while (cs.moveToNext());
                    }

                    if (outletDeliveryDoneListItemArrayList.size() > 1) {

                        String qtyFormat = String.format(getResources().getString(R.string.text_total_qty_count), outletDeliveryDoneListItemArrayList.size());
                        text_sign_d_tracking_no_title.setText(R.string.text_parcel_qty1);
                        text_sign_d_tracking_no.setText(qtyFormat);
                        layout_sign_d_sender.setVisibility(View.GONE);
                    }
                }


                if ((outletInfo.getRoute().substring(0, 2).contains("7E"))) {

                    text_top_title.setText(R.string.text_title_7e_store_delivery);
                    text_sign_d_outlet_address_title.setText(R.string.text_7e_store_address);
                    layout_sign_d_sign_memo.setVisibility(View.VISIBLE);
                    layout_sign_d_visit_log.setVisibility(View.VISIBLE);

                    if (!NetworkUtil.isNetworkAvailable(this)) {

                        AlertShow(getResources().getString(R.string.msg_network_connect_error));
                        return;
                    } else {

                        closeCamera();
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
                    Toast.makeText(DeliveryDoneActivity.this, getResources().getText(R.string.msg_memo_too_long), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();

        if (isPermissionTrue) {
            // Camera

            if (!outletInfo.getRoute().substring(0, 2).contains("7E")) {

                // When the screen is turned off and turned back on, the SurfaceTexture is already available.
                if (texture_sign_d_preview.isAvailable()) {

                    openCamera("onResume");
                } else {

                    texture_sign_d_preview.setSurfaceTextureListener(this);
                }
            }

            // Location
            gpsTrackerManager = new GPSTrackerManager(this);
            gpsEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager.gpsTrackerStart();
                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
            } else {

                DataUtil.enableLocationSettings(DeliveryDoneActivity.this);
            }
        }
    }


    public void confirmSigning() {

        if (outletInfo.getRoute().contains("7E")) {
            if (showQRCode) {        // QR Code Show

                saveOutletDeliveryDone();
            } else {                // QR Code Not Show... > 진행 불가능

                Toast.makeText(DeliveryDoneActivity.this, getResources().getString(R.string.msg_outlet_qrcode_require), Toast.LENGTH_SHORT).show();
            }
        } else if (outletInfo.getRoute().contains("FL")) {

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

    public void cancelSigning() {

        new AlertDialog.Builder(this)
                .setMessage(R.string.msg_delivered_sign_cancel)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {

                    setResult(Activity.RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.dismiss()).show();
    }

    public OutletInfo getOutletInfo(String barcodeNo) {

        Cursor cursor = DatabaseHelper.getInstance().get("SELECT route, zip_code, address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        OutletInfo outletInfo = new OutletInfo();

        if (cursor.moveToFirst()) {
            outletInfo.setRoute(cursor.getString(cursor.getColumnIndexOrThrow("route")));
            outletInfo.setZip_code(cursor.getString(cursor.getColumnIndexOrThrow("zip_code")));
            outletInfo.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
        }

        cursor.close();

        return outletInfo;
    }

    /*
     * 실시간 Upload 처리
     * add by jmkang 2014-01-22
     */
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
                locationModel.setDriverLocation(latitude, longitude);
            }

            String driverMemo = edit_sign_d_memo.getText().toString();

            // NOTIFICATION. 2020.06  visit log 추가
            // 사인 or 사진 둘 중 하나는 있어야 함
            boolean hasSignImage = sign_view_sign_d_signature.isTouch();
            boolean hasVisitImage = camera2.hasImage(img_sign_d_visit_log);
            //   Log.e(TAG, TAG + "  has DATA : " + hasSignImage + " / " + hasVisitImage);

            if (highAmountYn.equals("Y")) {

                if (!hasSignImage || !hasVisitImage) {

                    String msg = getResources().getString(R.string.msg_high_amount_sign_photo);
                    Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {

                if (!hasSignImage && !hasVisitImage) {

                    String msg = getResources().getString(R.string.msg_signature_require) + " or \n" + getResources().getString(R.string.msg_visit_photo_require);
                    Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            //서버에 올리기전 용량체크  내장메모리가 100Kbyte 안남은경우
            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(getResources().getString(R.string.msg_disk_size_error));
                return;
            }


            DataUtil.logEvent("button_click", TAG, "SetDeliveryUploadData");
            new DeliveryDoneUploadHelper.Builder(this, opID, officeCode, deviceID,
                    barcodeList, mReceiveType, driverMemo,
                    sign_view_sign_d_signature, hasSignImage, img_sign_d_visit_log, hasVisitImage,
                    MemoryStatus.getAvailableInternalMemorySize(), locationModel)
                    .setOnServerUploadEventListener(new OnServerEventListener() {
                        @Override
                        public void onPostResult() {

                            DataUtil.inProgressListPosition = 0;
                            setResult(Activity.RESULT_OK);
                            finish();
                        }

                        @Override
                        public void onPostFailList() {
                        }
                    }).build().execute();
        } catch (Exception e) {

            Log.e("Exception", "saveServerUploadSign  Exception : " + e.toString());
            Toast.makeText(this, getResources().getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void AlertShow(String msg) {

        AlertDialog.Builder alert_internet_status = new AlertDialog.Builder(this);
        alert_internet_status.setTitle(getResources().getString(R.string.text_warning));
        alert_internet_status.setMessage(msg);
        alert_internet_status.setPositiveButton(getResources().getString(R.string.button_close),
                (dialog, which) -> {

                    dialog.dismiss();
                    finish();
                });
        alert_internet_status.show();
    }

    public void saveOutletDeliveryDone() {

        try {

            if (!NetworkUtil.isNetworkAvailable(this)) {

                AlertShow(getResources().getString(R.string.msg_network_connect_error));
                return;
            }

            if (gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                Log.e("Location", TAG + " saveOutletDeliveryDone  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }

            if (outletInfo.getRoute().substring(0, 2).contains("7E")) {
                if (!sign_view_sign_d_signature.isTouch()) {
                    Toast.makeText(this, getResources().getString(R.string.msg_signature_require), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (MemoryStatus.getAvailableInternalMemorySize() != MemoryStatus.ERROR && MemoryStatus.getAvailableInternalMemorySize() < MemoryStatus.PRESENT_BYTE) {
                AlertShow(getResources().getString(R.string.msg_disk_size_error));
                return;
            }

            String driverMemo = edit_sign_d_memo.getText().toString();

            DataUtil.logEvent("button_click", TAG + "_OUTLET", "SetOutletDeliveryUploadData");
            // 2019.02 - stat : D3 로..   서버에서 outlet stat 변경
            new OutletDeliveryDoneHelper.Builder(this, opID, officeCode, deviceID,
                    barcodeList, outletInfo.getRoute().substring(0, 2), mReceiveType, sign_view_sign_d_signature, driverMemo,
                    MemoryStatus.getAvailableInternalMemorySize(), latitude, longitude)
                    .setOnOutletDataUploadEventListener(() -> {

                        setResult(Activity.RESULT_OK);
                        finish();
                    }).build().execute();
        } catch (Exception e) {

            Log.e("Exception", "saveOutletDeliveryDone   Exception ; " + e.toString());
            Toast.makeText(this, getResources().getString(R.string.text_error) + " - " + e.toString(), Toast.LENGTH_SHORT).show();
        }
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
    private void openCamera(String it) {

        CameraManager cameraManager = camera2.getCameraManager(this);
        cameraId = camera2.getCameraCharacteristics(cameraManager);
        Log.e("Camera", TAG + "  openCamera " + cameraId + "   >>> " + it);

        if (cameraId != null) {

            camera2.setCameraDevice(cameraManager, cameraId);
        } else {

            Toast.makeText(DeliveryDoneActivity.this, getResources().getString(R.string.msg_back_camera_required), Toast.LENGTH_SHORT).show();
        }
    }


    private void closeCamera() {

        camera2.closeCamera();
    }


    @Override
    public void onCameraDeviceOpened(CameraDevice cameraDevice, Size cameraSize, int rotation, String it) {
        Log.e("Camera", "onCameraDeviceOpened  " + it);
        texture_sign_d_preview.setRotation(rotation);

        try {

            SurfaceTexture texture = texture_sign_d_preview.getSurfaceTexture();
            texture.setDefaultBufferSize(cameraSize.getWidth(), cameraSize.getHeight());
            Surface surface = new Surface(texture);
            camera2.setCaptureSessionRequest(cameraDevice, surface);
        } catch (Exception e) {
            Log.e("Exception", "onCameraDeviceOpened  Exception : " + e.toString());
        }
    }

    @Override
    public void onCaptureCompleted() {

        isClickedPhoto = false;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        openCamera("onSurfaceTextureAvailable");
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
            progressDialog.setMessage(getResources().getString(R.string.text_please_wait));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {

                for (int i = 0; i < outletDeliveryDoneListItemArrayList.size(); i++) {

                    QRCodeResult result = getQRCodeData(outletDeliveryDoneListItemArrayList.get(i).getTrackingNo());

                    if (result != null) {

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
                        // https://dp.image-gmkt.com/qr.bar?scale=7&version=4&code={"Q":"D","J":"CR20190313001","V":"QT","S":"472","C":1}
                        Log.e(TAG, "QR Code URL > " + imgUrl);
                        outletDeliveryDoneListItemArrayList.get(i).setQrCode(imgUrl);
                    }
                }

                return "SUCCESS";
            } catch (Exception e) {

                Log.e("Exception", "QRCodeForQStationDelivery Exception : " + e.toString());
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


                if (texture_sign_d_preview.isAvailable()) {

                    openCamera("Outlet");
                } else {

                    texture_sign_d_preview.setSurfaceTextureListener(DeliveryDoneActivity.this);
                }
            } else {

                showQRCode = false;
                Toast.makeText(DeliveryDoneActivity.this, getResources().getString(R.string.msg_outlet_qrcode_data_error), Toast.LENGTH_LONG).show();
            }
        }

        private QRCodeResult getQRCodeData(String tracking_no) {
            Log.e(TAG, TAG + "  getQRCodeData  " + outlet_type + " / " + tracking_no);

            QRCodeResult resultObj;

            try {

                JSONObject job = new JSONObject();
                job.accumulate("qstation_type", outlet_type);
                job.accumulate("tracking_id", tracking_no);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());


                String methodName = "QRCodeForQStationDelivery";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);

                resultObj = new Gson().fromJson(jsonString, QRCodeResult.class);
            } catch (Exception e) {

                Log.e("Exception", "  QRCodeForQStationDelivery Json Exception : " + e.toString());
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