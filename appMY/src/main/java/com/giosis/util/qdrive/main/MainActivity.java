package com.giosis.util.qdrive.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.library.gps.QuickAppUserInfoUploadHelper;
import com.giosis.util.qdrive.barcodescanner.CaptureActivity;
import com.giosis.util.qdrive.barcodescanner.PodListActivity;
import com.giosis.util.qdrive.gps.FusedProviderService;
import com.giosis.util.qdrive.gps.LocationManagerService;
import com.giosis.util.qdrive.international.LoginActivity;
import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.OnServerEventListener;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.international.UploadData;
import com.giosis.util.qdrive.list.ListActivity;
import com.giosis.util.qdrive.settings.BluetoothDeviceData;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.QDataUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


/**
 * @author wontae
 * @editor krm0219
 */
public class MainActivity extends AppBaseActivity {
    String TAG = "MainActivity";

    // krm0219
    TextView text_home_driver_name;
    TextView text_home_driver_office;
    TextView text_home_total_qty;
    ImageView img_home_driver_profile;

    LinearLayout layout_home_list_count;
    TextView text_home_in_progress_count;
    TextView text_home_delivery_count;
    TextView text_home_pickup_count;
    TextView text_home_rpc_count;
    LinearLayout layout_home_download;
    LinearLayout layout_home_upload;

    Button btn_home_confirm_my_delivery_order;
    Button btn_home_change_delivery_driver;
    Button btn_home_outlet_order_status;
    Button btn_home_assign_pickup_driver;
    Button btn_home_scan_delivery_sheet;


    String fcmToken;
    AsyncTask<?, ?, ?> saveFCMTokenTask;
    String myResult;

    DatabaseHelper dbHelper;
    private String uploadFailedCount = "0";
    String opID = "";
    String opName = "";
    String opDefault = "";
    String officeCode = "";
    String officeName = "";
    String deviceID = "";

    String pickup_driver_yn = "";
    Context mContext = null;

    Intent fusedProviderService = null;
    Intent locationManagerService = null;

    boolean isGooglePlayService = false;

    GPSTrackerManager gpsTrackerManager;
    boolean gpsOnceEnable = false;
    double latitude = 0;
    double longitude = 0;

    static Boolean isHomeBtnClick = false;

    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("krm0219", "User Agent : " + QDataUtil.Companion.getCustomUserAgent(MyApplication.getContext()));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // FCM token
        saveServerFCMToken();


        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.home, container, false);
        drawerLayout.addView(contentView, 0);
        setTopTitle(getString(R.string.navi_home));

        mContext = this.getApplicationContext();
        dbHelper = DatabaseHelper.getInstance();

        opID = MyApplication.preferences.getUserId();
        officeCode = MyApplication.preferences.getOfficeCode();
        officeName = MyApplication.preferences.getOfficeName();
        deviceID = MyApplication.preferences.getDeviceUUID();
        opDefault = MyApplication.preferences.getDefault();
        pickup_driver_yn = MyApplication.preferences.getPickupDriver();
        Log.e("krm0219", "DATA : " + opID + " / " + opName + " / " + officeCode + " / " + officeName + " / " + deviceID);

        // krm0219 Outlet
        String outletDriverYN;
        try {

            outletDriverYN = MyApplication.preferences.getOutletDriver();
        } catch (Exception e) {

            outletDriverYN = "N";
        }

        // 7ETB, FLTB push 받고 온 경우 확인
        String outletPush = getIntent().getStringExtra("outletPush");

        if (outletPush == null) {
            outletPush = "N";
        }
        Log.e("krm0219", "  Outlet Driver : " + outletDriverYN + "  outletPush  : " + outletPush + "  Pickup Driver : " + pickup_driver_yn);


        text_home_driver_name = findViewById(R.id.text_home_driver_name);
        text_home_driver_office = findViewById(R.id.text_home_driver_office);
        text_home_total_qty = findViewById(R.id.text_home_total_qty);
        img_home_driver_profile = findViewById(R.id.img_home_driver_profile);

        layout_home_list_count = findViewById(R.id.layout_home_list_count);
        text_home_in_progress_count = findViewById(R.id.text_home_in_progress_count);
        text_home_delivery_count = findViewById(R.id.text_home_delivery_count);
        text_home_pickup_count = findViewById(R.id.text_home_pickup_count);
        text_home_rpc_count = findViewById(R.id.text_home_rpc_count);
        layout_home_download = findViewById(R.id.layout_home_download);
        layout_home_upload = findViewById(R.id.layout_home_upload);

        btn_home_confirm_my_delivery_order = findViewById(R.id.btn_home_confirm_my_delivery_order);
        btn_home_assign_pickup_driver = findViewById(R.id.btn_home_assign_pickup_driver);
        btn_home_scan_delivery_sheet = findViewById(R.id.btn_home_scan_delivery_sheet);
        btn_home_change_delivery_driver = findViewById(R.id.btn_home_change_delivery_driver);
        btn_home_outlet_order_status = findViewById(R.id.btn_home_outlet_order_status);    // 19.01 krm0219


        getLocalCount();
        if (DownloadChk()) {

            Download();
        } else if (outletPush.equals("Y")) {

            Download();
        }

        layout_home_list_count.setOnClickListener(clickListener);
        layout_home_download.setOnClickListener(clickListener);
        layout_home_upload.setOnClickListener(clickListener);

        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qdrive_img_default);
        img_home_driver_profile.setImageBitmap(mBitmap);
        RoundedBitmapDrawable roundedImageDrawable = createRoundedBitmapImageDrawableWithBorder(mBitmap);
        img_home_driver_profile.setImageDrawable(roundedImageDrawable);

        if (outletDriverYN.equals("Y")) {

            btn_home_confirm_my_delivery_order.setText(mContext.getResources().getString(R.string.text_start_delivery_for_outlet));
            btn_home_outlet_order_status.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, DisplayUtil.dpTopx(mContext, 15), 0, 0);
            btn_home_change_delivery_driver.setLayoutParams(lp);
        } else {

            btn_home_confirm_my_delivery_order.setText(mContext.getResources().getString(R.string.button_confirm_my_delivery_order));
            btn_home_outlet_order_status.setVisibility(View.GONE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, DisplayUtil.dpTopx(mContext, 15), 0, DisplayUtil.dpTopx(mContext, 30));
            btn_home_change_delivery_driver.setLayoutParams(lp);
        }

        //POD Scan 권한 : 파트너 사장 또는 Auth:91
        if (opDefault.equals("Y") || MyApplication.preferences.getAuthNo().contains("91")) {
            btn_home_scan_delivery_sheet.setVisibility(View.VISIBLE);
            btn_home_scan_delivery_sheet.setOnClickListener(clickListener);
        }


        btn_home_confirm_my_delivery_order.setOnClickListener(clickListener);
        btn_home_change_delivery_driver.setOnClickListener(clickListener);
        btn_home_outlet_order_status.setOnClickListener(clickListener);
        btn_home_assign_pickup_driver.setOnClickListener(clickListener);
        btn_home_scan_delivery_sheet.setOnClickListener(clickListener);


        //
        //
        PermissionChecker checker = new PermissionChecker(this);

        // 권한 여부 체크 (없으면 true, 있으면 false)
        if (checker.lacksPermissions(PERMISSIONS)) {

            isPermissionTrue = false;
            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, PERMISSIONS);
            overridePendingTransition(0, 0);
        } else {

            isPermissionTrue = true;
            GPSTrackerServiceStart();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("krm0219", TAG + "  onResume");

        opName = MyApplication.preferences.getUserName();
        setNaviHeader(opName, officeName);

        text_home_driver_name.setText(opName);
        text_home_driver_office.setText(officeName);

        try {

            DataUtil.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        } catch (Exception e) {

            Log.e("FA", TAG + "  FirebaseAnalytics 초기화 Exception : " + e.toString());
        }

        if (isPermissionTrue) {

            gpsTrackerManager = new GPSTrackerManager(mContext);
            gpsOnceEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsOnceEnable && gpsTrackerManager != null) {

                gpsTrackerManager.GPSTrackerStart();

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                Log.e("Location", TAG + " GPSTrackerManager onResume : " + latitude + "  " + longitude + "  ");
            } else {

                DataUtil.enableLocationSettings(MainActivity.this, mContext);
            }
        }

        // TODO
        // AppBaseActivity.java >  nav_list_header.xml > layout_message  gone 처리해놓음.
        //initMessageCount(opID);


        MyApplication myApp = (MyApplication) getApplicationContext();
        myApp.setBadgeCnt(0);
        setBadge(getApplicationContext(), 0);

        //Qx 소속 드라이버는 Assign 된 픽업 조회
        if (officeName.contains("Qxpress")) {
            new PickupAssignCheckHelper.Builder(this, opID, officeCode, deviceID)
                    .setOnPickupAssignCheckListener(new PickupAssignCheckHelper.OnPickupAssignCheckListener() {
                        @Override
                        public void onDownloadResult(Integer result) {
                            getLocalCount();
                        }

                        @Override
                        public void onDownloadFailList(Integer result) {
                            //Deactivated 사용자
                            if (result == -1) {

                                String msg = mContext.getResources().getString(R.string.msg_your_account_deactivated);
                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage(msg)
                                        .setTitle(mContext.getResources().getString(R.string.text_alert))
                                        .setCancelable(false).setPositiveButton(mContext.getResources().getString(R.string.button_ok),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        }).show();
                            }
                        }
                    }).build().execute();
        }

        getLocalCount();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Confirm my delivery order 결과
        // 리스트(Upload Failed) 에서 다시 시도 할 수 있으므로 실패사유만 팝업리스트로 보여준다. (Shipping : Fail Reason)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

                String failList = intent.getStringExtra("result");
                if (0 < failList.length()) {
                    Intent failIntent = new Intent(MainActivity.this, FailListActivity.class);
                    failIntent.putExtra("failList", failList);
                    startActivity(failIntent);
                }
            }
        }
        //POD Upload Result (실패시 POD 전용 리스트로 이동)
        else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                String result = intent.getStringExtra("result");
                if (result != null && !result.equals("OK")) {
                    Intent intent1 = new Intent(MainActivity.this, PodListActivity.class);
                    startActivity(intent1);
                }
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

                isPermissionTrue = true;
                GPSTrackerServiceStart();
            }
        } else if (requestCode == 1010) { // setting activity result
            if (resultCode == Activity.RESULT_OK) {
                String login = intent.getStringExtra("method");

                if ("signOut".equals(login)) {
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    loginIntent.putExtra("method", "signOut");
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(loginIntent);
                }
            }
        }

    }


    public static void setBadge(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                return resolveInfo.activityInfo.name;
            }
        }
        return null;
    }


    public void getLocalCount() {
        String rpcCnt;

        try {

            String selectQuery = " select   ifnull(sum(case when chg_dt is null then 1 else 0 end), 0) as InprogressCnt " //In-Progress
                    + " , ifnull(sum(case when punchOut_stat = 'S' and strftime('%Y-%m-%d', chg_dt) = date('now') then 1 else 0 end) ,0) as TodayUploadedCnt " // Uploaded Today
                    + " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is not null  then 1 else 0 end), 0) as UploadFailedCnt " //Upload Failed
                    + " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is null and type = 'D' then 1 else 0 end), 0) as InprogressDeliveryCnt " //Delivery
                    + " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is null and type = 'P' and route <> 'RPC' then 1 else 0 end), 0) as InprogressPickupCnt "  //Pickup
                    + " , ifnull(sum(case when punchOut_stat <> 'S' and chg_dt is null and route = 'RPC' then 1 else 0 end), 0) as InprogressRpcCnt " //RPC
                    + " , datetime(max(reg_dt), 'localtime') as PI_Time "
                    + " from " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST
                    + " where reg_id= '" + opID + "'";

            Cursor cs = dbHelper.get(selectQuery);

            if (cs.moveToFirst()) {

                int total = Integer.parseInt(cs.getString(cs.getColumnIndex("InprogressCnt")))
                        + Integer.parseInt(cs.getString(cs.getColumnIndex("TodayUploadedCnt")))
                        + Integer.parseInt(cs.getString(cs.getColumnIndex("UploadFailedCnt")));

                text_home_total_qty.setText(String.valueOf(total));

                uploadFailedCount = cs.getString(cs.getColumnIndex("UploadFailedCnt"));
                rpcCnt = cs.getString(cs.getColumnIndex("InprogressRpcCnt"));
                text_home_in_progress_count.setText(cs.getString(cs.getColumnIndex("InprogressCnt")));
                text_home_delivery_count.setText(cs.getString(cs.getColumnIndex("InprogressDeliveryCnt")));
                text_home_pickup_count.setText(cs.getString(cs.getColumnIndex("InprogressPickupCnt")));
                text_home_rpc_count.setText(cs.getString(cs.getColumnIndex("InprogressRpcCnt")));

                //파트너 Office Header - RPC Change Driver 버튼 설정
                if (opDefault.equals("Y")) {
                    if (!rpcCnt.equals("0")) {
                        btn_home_assign_pickup_driver.setVisibility(View.VISIBLE);
                    } else {
                        btn_home_assign_pickup_driver.setVisibility(View.GONE);
                    }
                }
            }
        } catch (Exception e) {

            Log.e("Exception", "Local Count Exception : " + e.toString());
        }
    }


    private void Upload() {

        if (!NetworkUtil.isNetworkAvailable(MainActivity.this)) {

            Toast.makeText(MainActivity.this, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show();
            return;
        }


        ArrayList<UploadData> songjanglist = new ArrayList<>();
        // 업로드 대상건 로컬 DB 조회
        dbHelper = DatabaseHelper.getInstance();
        String selectQuery = "select invoice_no" +
                " , stat " +
                " , ifnull(rcv_type, '')  as rcv_type" +
                " , ifnull(fail_reason, '')  as fail_reason" +
                " , ifnull(driver_memo, '') as driver_memo" +
                " , ifnull(real_qty, '') as real_qty" +
                " , ifnull(retry_dt , '') as retry_dt" +
                " , type " +
                " from " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST +
                " where reg_id= '" + opID + "'" +
                " and punchOut_stat <> 'S' and chg_dt is not null";
        Cursor cs = dbHelper.get(selectQuery);

        if (cs.moveToFirst()) {
            do {
                UploadData data = new UploadData();
                data.setNoSongjang(cs.getString(cs.getColumnIndex("invoice_no")));
                data.setStat(cs.getString(cs.getColumnIndex("stat")));
                data.setReceiveType(cs.getString(cs.getColumnIndex("rcv_type")));
                data.setFailReason(cs.getString(cs.getColumnIndex("fail_reason")));
                data.setDriverMemo(cs.getString(cs.getColumnIndex("driver_memo")));
                data.setRealQty(cs.getString(cs.getColumnIndex("real_qty")));
                data.setRetryDay(cs.getString(cs.getColumnIndex("retry_dt")));
                data.setType(cs.getString(cs.getColumnIndex("type")));
                songjanglist.add(data);
            } while (cs.moveToNext());
        }


        if (gpsOnceEnable && gpsTrackerManager != null) {

            latitude = gpsTrackerManager.getLatitude();
            longitude = gpsTrackerManager.getLongitude();

            Log.e("Location", TAG + " Upload()  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
        }

        if (0 < songjanglist.size()) {

            DataUtil.logEvent("button_click", TAG, com.giosis.library.util.DataUtil.requestSetUploadDeliveryData + "/" + com.giosis.library.util.DataUtil.requestSetUploadPickupData);

            new DeviceDataUploadHelper.Builder(this, opID, officeCode, deviceID, songjanglist, "QH", latitude, longitude).
                    setOnServerEventListener(new OnServerEventListener() {

                        @Override
                        public void onPostResult() {

                            getLocalCount();
                        }

                        @Override
                        public void onPostFailList() {

                            getLocalCount();
                        }
                    }).build().execute();
        } else {

            String msg = mContext.getResources().getString(R.string.msg_no_data_to_upload);
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setTitle(mContext.getResources().getString(R.string.button_upload))
                    .setCancelable(false).setPositiveButton(mContext.getResources().getString(R.string.button_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }
    }

    private Boolean DownloadChk() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String today = dateFormat.format(new Date());

        // 날짜기준 UTC
        String selectQuery = " select  datetime(max(reg_dt), 'localtime') as PI_Time, "
                + " strftime('%Y-%m-%d', max(reg_dt)) as PI_Date "
                + " from " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST
                + " where reg_id= '" + opID + "'"
                + " and strftime('%Y-%m-%d', reg_dt) = date('now')";

        try {
            Cursor cs = dbHelper.get(selectQuery);
            if (cs.moveToFirst()) {
                String PunchInDate = cs.getString(cs.getColumnIndex("PI_Date"));

                if (PunchInDate != null && !PunchInDate.equals("")) {
                    //오늘 날짜가 있으면 다운로드 하지 않음
                    if (today.equals(PunchInDate)) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {

            Log.e("Error", "Download Check Exception : " + e.toString());
        }

        return true;
    }

    private void Download() {

        if (!NetworkUtil.isNetworkAvailable(MainActivity.this)) {

            Toast.makeText(MainActivity.this, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show();
            return;
        }

        // 2020.12  Failed Code 가져오기
        DataUtil.requestServerPickupFailedCode();
        DataUtil.requestServerDeliveryFailedCode();


        // 2020.02 NOTIFICATION.  login.js 삭제 - 휴무일 가져오기
        int delete = DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_REST_DAYS, "");
        Log.e("krm0219", "DELETE  DB_TABLE_REST_DAYS  Count : " + delete);
        new GetRestDaysAsyncTask(DataUtil.nationCode, Calendar.getInstance().get(Calendar.YEAR), null).execute();
        new GetRestDaysAsyncTask(DataUtil.nationCode, Calendar.getInstance().get(Calendar.YEAR) + 1, null).execute();


        if (gpsOnceEnable && gpsTrackerManager != null) {

            latitude = gpsTrackerManager.getLatitude();
            longitude = gpsTrackerManager.getLongitude();
        }
        Log.e("Location", TAG + "  Download()  Location : " + latitude + "  " + longitude);

        //
        if (0 < Integer.parseInt(uploadFailedCount)) {
            String msg = mContext.getResources().getString(R.string.msg_download_not_supported);
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setTitle(mContext.getResources().getString(R.string.text_alert))
                    .setCancelable(false).setPositiveButton(mContext.getResources().getString(R.string.button_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();

            return;
        }

        if (!NetworkUtil.isNetworkAvailable(MainActivity.this)) {

            Toast.makeText(MainActivity.this, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show();
            return;
        }


        try {

            dbHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "");
        } catch (Exception e) {

            dbHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "");

            try {

                Bundle params = new Bundle();
                params.putString("Activity", TAG);
                params.putString("message", "" + " DB.delete > " + e.toString());
                DataUtil.mFirebaseAnalytics.logEvent("error_exception", params);
                Log.e("krm0219", "dbHelper.delete Exception : " + e.toString());
            } catch (Exception e1) {

                Log.e("FA", TAG + "  FirebaseAnalytics Download Exception : " + e.toString());
            }
        }

        new ServerDownloadHelper.Builder(this, opID, officeCode, deviceID)
                .setOnServerDownloadEventListener(new ServerDownloadHelper.OnServerDownloadEventListener() {

                    @Override
                    public void onDownloadResult() {

                        getLocalCount();
                    }
                }).build().execute();
    }

    public void GPSTrackerServiceStart() {

        if (pickup_driver_yn != null && pickup_driver_yn.equals("Y")) {

            // gps 켜는 alert 창  -> 객체 하나에만 호출해도 GPS 설정창 문제 없음(기기는 하나)
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            enableGPSSetting(locationManager);

            isGooglePlayService = isGooglePlayServicesAvailable();

            Log.e("krm0219", TAG + "   MANUFACTURER = " + Build.MANUFACTURER); //제조사
            if (Build.MANUFACTURER.equals("HUAWEI")) {  // 화웨이 - google 위치정보 못가져옴
                isGooglePlayService = false;
            }

            if (isGooglePlayService) {  // Fused Provider Service start (Google play 에 클라이언트 객체 얻어 서비스)

                // 서비스 시작
                if (DataUtil.getFusedProviderService() != null) {

                    Log.e("krm0219", TAG + "  FusedProviderService - not null");
                    //    stopService(DataUtil.getFusedProviderService());
                } else {

                    Log.e("krm0219", TAG + "  FusedProviderService - null");
                    fusedProviderService = new Intent(MainActivity.this, FusedProviderService.class);
                    ContextCompat.startForegroundService(this, fusedProviderService); // 내부에서 알아서 분기
                    DataUtil.setFusedProviderService(fusedProviderService);
                }
            } else { // location manager Service start (샤오미 등 구글 플레이 가 작동하지 않는 폰)

                // 서비스 시작
                if (DataUtil.getLocationManagerService() != null) {

                    Log.e("krm0219", TAG + "  LocationManagerService - not null");
                    //   stopService(DataUtil.getLocationManagerService());
                } else {

                    Log.e("krm0219", TAG + "  LocationManagerService - null");
                    locationManagerService = new Intent(MainActivity.this, LocationManagerService.class);
                    ContextCompat.startForegroundService(this, locationManagerService); // 내부에서 알아서 분기
                    DataUtil.setLocationManagerService(locationManagerService);
                }
            }
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        return ConnectionResult.SUCCESS == status;
    }

    public void enableGPSSetting(LocationManager locationManager) {

        if (locationManager != null) {

            boolean gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gpsEnable) {

                DataUtil.enableLocationSettings(MainActivity.this, mContext);
            }
        }
    }

    public void setDestroyUserInfo() {

        String api_level = Integer.toString(Build.VERSION.SDK_INT);    // API Level
        String device_info = android.os.Build.DEVICE;           // Device
        String device_model = android.os.Build.MODEL;            // Model
        String device_product = android.os.Build.PRODUCT;          // Product
        String device_os_version = System.getProperty("os.version"); // OS version

        new QuickAppUserInfoUploadHelper.Builder(mContext, opID, "", api_level, device_info,
                device_model, device_product, device_os_version, "killapp")
                .setOnQuickQppUserInfoUploadEventListener(new QuickAppUserInfoUploadHelper.OnQuickAppUserInfoUploadEventListener() {

                    @Override
                    public void onServerResult() {

                    }

                }).build().execute();

        double accuracy = 0;

        try {

            if (gpsOnceEnable && gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                accuracy = gpsTrackerManager.getAccuracy();

                Log.e("krm0219", TAG + " setDestroyUserInfo  GPSTrackerManager : " + latitude + "  " + longitude + "  ");
            }
        } catch (Exception e) {

            latitude = 0;
            longitude = 0;
        }


        new DriverPerformanceLogUploadHelper.Builder(mContext, opID, latitude, longitude, accuracy, "killapp")
                .setOnDriverPerformanceLogUploadEventListener(new DriverPerformanceLogUploadHelper.OnDriverPerformanceLogUploadEventListener() {

                    @Override
                    public void onServerResult() {

                    }

                }).build().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("krm0219", TAG + "  onPause");
        DataUtil.stopGPSManager(gpsTrackerManager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("krm0219", TAG + "  onDestroy");

        if (!isHomeBtnClick) {  // home btn 누른게 아닐 때 작동해야 할 destroy method
            isHomeBtnClick = false;
            setDestroyUserInfo();
        }


        // Bluetooth Print 연결 초기화 (앱 종료시)
        BluetoothDeviceData.connectedPrinterAddress = null;


        if (DataUtil.getFusedProviderService() != null) {

            stopService(DataUtil.getFusedProviderService());
            DataUtil.setFusedProviderService(null);
        }

        if (DataUtil.getLocationManagerService() != null) {

            stopService(DataUtil.getLocationManagerService());
            DataUtil.setLocationManagerService(null);
        }

        dbHelper.close();

        if (!isHomeBtnClick) {  // home btn 누른게 아닐 때 작동해야 할 destroy method
            // Home 으로 가서 app 종료 할 때 구분해서 alert 창 안 띄우게 함
            isHomeBtnClick = false;
            Toast.makeText(this.getApplicationContext(), mContext.getResources().getString(R.string.msg_qdrive_app_exited), Toast.LENGTH_SHORT).show();
        } else {
            isHomeBtnClick = false;
        }

        isHomeBtnClick = false;
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int id = view.getId();

            switch (id) {

                case R.id.layout_home_list_count: {

                    Intent intent = new Intent(MainActivity.this, ListActivity.class);
                    intent.putExtra("position", 0);
                    startActivity(intent);
                }
                break;

                case R.id.layout_home_download: {
                    Download();
                }
                break;

                case R.id.layout_home_upload: {
                    Upload();
                }
                break;


                case R.id.btn_home_confirm_my_delivery_order: {

                    CaptureActivity.removeBarcodeListInstance();

                    Intent intentScan = new Intent(MainActivity.this, CaptureActivity.class);
                    intentScan.putExtra("title", mContext.getResources().getString(R.string.text_title_driver_assign));
                    intentScan.putExtra("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER);
                    startActivityForResult(intentScan, 1);
                }
                break;

                case R.id.btn_home_assign_pickup_driver: {

                    Intent intent = new Intent(MainActivity.this, RpcListActivity.class);
                    startActivity(intent);
                }
                break;

                case R.id.btn_home_scan_delivery_sheet: {

                    Intent intentScan = new Intent(MainActivity.this, CaptureActivity.class);
                    intentScan.putExtra("title", "(Step1) Scan Tracking No");
                    intentScan.putExtra("type", BarcodeType.SCAN_DELIVERY_SHEET);
                    startActivityForResult(intentScan, 2);
                }
                break;

                case R.id.btn_home_change_delivery_driver: {

                    if (gpsOnceEnable && gpsTrackerManager != null) {

                        CaptureActivity.removeBarcodeListInstance();

                        Intent intentScan = new Intent(MainActivity.this, CaptureActivity.class);
                        intentScan.putExtra("title", mContext.getResources().getString(R.string.button_change_delivery_driver));
                        intentScan.putExtra("type", BarcodeType.CHANGE_DELIVERY_DRIVER);
                        startActivity(intentScan);
                    } else {

                        DataUtil.enableLocationSettings(MainActivity.this, mContext);
                    }
                }
                break;

                case R.id.btn_home_outlet_order_status: {

                    //OutletOrderStatusActivity
                    Intent intent = new Intent(MainActivity.this, OutletOrderStatusActivity.class);
                    startActivity(intent);
                }
                break;
            }
        }
    };


    @Override
    public void onBackPressed() {
    }


    public void saveServerFCMToken() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {

                fcmToken = instanceIdResult.getToken();

                Log.e("FCM", TAG + "  Device Token : " + fcmToken);
                sendAPIkey();
            }
        });
    }

    private void sendAPIkey() {

        String opId = MyApplication.preferences.getUserId();
        String deviceID = MyApplication.preferences.getDeviceUUID();

        // TEST.  Galaxy Note5 / Huawei
        if (deviceID.equals("890525e99f30801a") || deviceID.equals("acd248681b26f53f")) {

            Log.e("krm0219", "TEST~~~~~");
        } else if (!opId.equals("") && !deviceID.equals("") && !fcmToken.equals("")) {

            Log.e("krm0219", "FCMTokenTask ~~~~~");
            saveFCMTokenTask = new saveFCMTokenTask().execute(fcmToken, opId, deviceID);
        }
    }


    private class saveFCMTokenTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            HttpPostData(params[0], params[1], params[2]);
            return null;
        }

        protected void onPostExecute(Void result) {

        }
    }

    public void HttpPostData(String token, String op_id, String device_id) {

        try {

            String apiURL = MyApplication.preferences.getServerURL() + DataUtil.API_ADDRESS;
            URL url = new URL(apiURL + "/SetGCMUserKeyRegister");

            HttpURLConnection http = (HttpURLConnection) url.openConnection(); // 접속
            http.setDefaultUseCaches(false);
            http.setDoInput(true);
            http.setDoOutput(true);
            http.setRequestMethod("POST");

            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            StringBuilder buffer = new StringBuilder();
            buffer.append("user_key").append("=").append(token).append("&");
            buffer.append("op_id").append("=").append(op_id).append("&");
            buffer.append("app_cd").append("=").append("01").append("&");   // 01.Qdrive / 02.QxQuick
            buffer.append("device_id").append("=").append(device_id).append("&");
            buffer.append("app_id").append("=").append(DataUtil.appID).append("&");
            buffer.append("nation_cd").append("=").append(DataUtil.nationCode);
            Log.i("FCM", "URL : " + url.toString());
            Log.i("FCM", "SetGCMUserKeyRegister Data : " + buffer.toString());

            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(buffer.toString());
            writer.flush();

            InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(tmp);
            StringBuilder builder = new StringBuilder();

            String str;

            while ((str = reader.readLine()) != null) {
                builder.append(str).append("\n");
            }

            myResult = builder.toString();
            // {"ResultCode":0,"ResultMsg":"Success"}
            Log.e("Server", "SetGCMUserKeyRegister  Result : " + myResult);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  HttpPostData Exception : " + e.toString());
        }
    }

    private RoundedBitmapDrawable createRoundedBitmapImageDrawableWithBorder(Bitmap bitmap) {
        int bitmapWidthImage = bitmap.getWidth();
        int bitmapHeightImage = bitmap.getHeight();
        int borderWidthHalfImage = 4;

        int bitmapRadiusImage = Math.min(bitmapWidthImage, bitmapHeightImage) / 2;
        int bitmapSquareWidthImage = Math.min(bitmapWidthImage, bitmapHeightImage);
        int newBitmapSquareWidthImage = bitmapSquareWidthImage + borderWidthHalfImage;

        Bitmap roundedImageBitmap = Bitmap.createBitmap(newBitmapSquareWidthImage, newBitmapSquareWidthImage, Bitmap.Config.ARGB_8888);
        Canvas mcanvas = new Canvas(roundedImageBitmap);
        mcanvas.drawColor(Color.RED);
        int i = borderWidthHalfImage + bitmapSquareWidthImage - bitmapWidthImage;
        int j = borderWidthHalfImage + bitmapSquareWidthImage - bitmapHeightImage;

        mcanvas.drawBitmap(bitmap, i, j, null);

        Paint borderImagePaint = new Paint();
        borderImagePaint.setStyle(Paint.Style.STROKE);
        borderImagePaint.setStrokeWidth(borderWidthHalfImage * 2);
        borderImagePaint.setColor(Color.GRAY);
        mcanvas.drawCircle(mcanvas.getWidth() / 2, mcanvas.getWidth() / 2, newBitmapSquareWidthImage / 2, borderImagePaint);

        RoundedBitmapDrawable roundedImageBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), roundedImageBitmap);
        roundedImageBitmapDrawable.setCornerRadius(bitmapRadiusImage);
        roundedImageBitmapDrawable.setAntiAlias(true);
        return roundedImageBitmapDrawable;
    }
}
