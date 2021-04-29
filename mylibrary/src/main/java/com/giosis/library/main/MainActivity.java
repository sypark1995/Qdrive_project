package com.giosis.library.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
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

import com.giosis.library.R;
import com.giosis.library.UploadData;
import com.giosis.library.gps.FusedProviderService;
import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.library.gps.LocationManagerService;
import com.giosis.library.gps.QuickAppUserInfoUploadHelper;
import com.giosis.library.list.ListActivity;
import com.giosis.library.main.route.TodayMyRouteActivity;
import com.giosis.library.main.submenu.OutletOrderStatusActivity;
import com.giosis.library.main.submenu.RpcListActivity;
import com.giosis.library.pickup.CreatePickupOrderActivity;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.setting.bluetooth.BluetoothDeviceData;
import com.giosis.library.util.BarcodeType;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.library.util.DisplayUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.OnServerEventListener;
import com.giosis.library.util.PermissionActivity;
import com.giosis.library.util.PermissionChecker;
import com.giosis.library.util.Preferences;
import com.giosis.library.util.QDataUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class MainActivity extends AppBaseActivity {
    String TAG = "MainActivity";

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
    Button btn_home_create_pickup_order;
    Button btn_home_assign_pickup_driver;
    Button btn_home_today_my_route;


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

    Intent fusedProviderService = null;
    Intent locationManagerService = null;

    boolean isGooglePlayService = false;

    GPSTrackerManager gpsTrackerManager;
    boolean gpsOnceEnable = false;
    double latitude = 0;
    double longitude = 0;

    static Boolean isHomeBtnClick = false;

    //
    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE};


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int id = view.getId();

            if (id == R.id.layout_home_list_count) {

                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                intent.putExtra("position", 0);
                startActivity(intent);
            } else if (id == R.id.layout_home_download) {

                Download();
            } else if (id == R.id.layout_home_upload) {

                Upload();
            } else if (id == R.id.btn_home_confirm_my_delivery_order) {

                try {

                    Intent intent = new Intent(MainActivity.this, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivity"));
                    intent.putExtra("title", getResources().getString(R.string.text_title_driver_assign));
                    intent.putExtra("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER);
                    startActivity(intent);
                } catch (Exception e) {

                    Log.e("Exception", "  Exception : " + e.toString());
                    Toast.makeText(MainActivity.this, "Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.btn_home_assign_pickup_driver) {

                Intent intent = new Intent(MainActivity.this, RpcListActivity.class);
                startActivity(intent);
            } else if (id == R.id.btn_home_change_delivery_driver) {

                if (gpsOnceEnable && gpsTrackerManager != null) {

                    try {

                        Intent intent = new Intent(MainActivity.this, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivity"));
                        intent.putExtra("title", getResources().getString(R.string.button_change_delivery_driver));
                        intent.putExtra("type", BarcodeType.CHANGE_DELIVERY_DRIVER);
                        startActivity(intent);
                    } catch (Exception e) {

                        Log.e("Exception", "  Exception : " + e.toString());
                        Toast.makeText(MainActivity.this, "Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {

                    DataUtil.enableLocationSettings(MainActivity.this);
                }
            } else if (id == R.id.btn_home_outlet_order_status) {

                Intent intent = new Intent(MainActivity.this, OutletOrderStatusActivity.class);
                startActivity(intent);
            } else if (id == R.id.btn_home_create_pickup_order) {

                Intent intent = new Intent(MainActivity.this, CreatePickupOrderActivity.class);
                startActivity(intent);
            } else if (id == R.id.btn_home_today_my_route) {

                Intent intent = new Intent(MainActivity.this, TodayMyRouteActivity.class);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "User Agent : " + QDataUtil.Companion.getCustomUserAgent(MainActivity.this));
        DatabaseHelper.getInstance().getDbPath();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // FCM token
        saveServerFCMToken();


        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_main_home, container, false);
        drawerLayout.addView(contentView, 0);
        setTopTitle(getResources().getString(R.string.navi_home));


//        // TEST Outlet
//        Preferences.INSTANCE.setOutletDriver("Y");
//        Preferences.INSTANCE.setUserId("Syed_7E");      // 7Eleven.Ajib


        dbHelper = DatabaseHelper.getInstance();
        opID = Preferences.INSTANCE.getUserId();
        officeCode = Preferences.INSTANCE.getOfficeCode();
        officeName = Preferences.INSTANCE.getOfficeName();
        opDefault = Preferences.INSTANCE.getDefault();
        deviceID = Preferences.INSTANCE.getDeviceUUID();
        pickup_driver_yn = Preferences.INSTANCE.getPickupDriver();


        // Outlet
        String outletDriverYN;
        try {

            outletDriverYN = Preferences.INSTANCE.getOutletDriver();
        } catch (Exception e) {

            outletDriverYN = "N";
        }

        // 7ETB, FLTB push 받고 온 경우 확인
        String outletPush = getIntent().getStringExtra("outletPush");

        if (outletPush == null) {
            outletPush = "N";
        }
        Log.e(TAG, "  Outlet Driver : " + outletDriverYN + "  outletPush  : " + outletPush + "  Pickup Driver : " + pickup_driver_yn);


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
        btn_home_change_delivery_driver = findViewById(R.id.btn_home_change_delivery_driver);
        btn_home_outlet_order_status = findViewById(R.id.btn_home_outlet_order_status);    // 19.01 krm0219
        btn_home_create_pickup_order = findViewById(R.id.btn_home_create_pickup_order);
        btn_home_today_my_route = findViewById(R.id.btn_home_today_my_route);


        getLocalCount();
        if (DownloadCheck()) {

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

        if ("Y".equals(pickup_driver_yn)) {
            btn_home_create_pickup_order.setVisibility(View.VISIBLE);
        } else {
            btn_home_create_pickup_order.setVisibility(View.GONE);
        }

        // MY/ID Route
        if (!Preferences.INSTANCE.getUserNation().equalsIgnoreCase("SG")) {

            btn_home_today_my_route.setVisibility(View.VISIBLE);
        } else {

            btn_home_today_my_route.setVisibility(View.GONE);
        }

        if (outletDriverYN.equals("Y")) {

            btn_home_confirm_my_delivery_order.setText(getResources().getString(R.string.text_start_delivery_for_outlet));
            btn_home_outlet_order_status.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, DisplayUtil.dpTopx(this, 15), 0, 0);
            btn_home_change_delivery_driver.setLayoutParams(lp);
        } else {

            btn_home_confirm_my_delivery_order.setText(getResources().getString(R.string.button_confirm_my_delivery_order));
            btn_home_outlet_order_status.setVisibility(View.GONE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, DisplayUtil.dpTopx(this, 15), 0, DisplayUtil.dpTopx(this, 30));

            if (btn_home_create_pickup_order.getVisibility() == View.VISIBLE) {

                if (btn_home_today_my_route.getVisibility() == View.VISIBLE) {

                    btn_home_today_my_route.setLayoutParams(lp);
                } else {

                    btn_home_create_pickup_order.setLayoutParams(lp);
                }
            } else {

                if (btn_home_today_my_route.getVisibility() == View.VISIBLE) {

                    btn_home_today_my_route.setLayoutParams(lp);
                } else {

                    btn_home_change_delivery_driver.setLayoutParams(lp);
                }
            }
        }

        btn_home_confirm_my_delivery_order.setOnClickListener(clickListener);
        btn_home_change_delivery_driver.setOnClickListener(clickListener);
        btn_home_outlet_order_status.setOnClickListener(clickListener);
        btn_home_create_pickup_order.setOnClickListener(clickListener);
        btn_home_assign_pickup_driver.setOnClickListener(clickListener);
        btn_home_today_my_route.setOnClickListener(clickListener);


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

        opID = Preferences.INSTANCE.getUserId();
        opName = Preferences.INSTANCE.getUserName();

        setNaviHeader(opName, officeName);
        text_home_driver_name.setText(opName);
        text_home_driver_office.setText(officeName);

        DataUtil.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (isPermissionTrue) {

            gpsTrackerManager = new GPSTrackerManager(this);
            gpsOnceEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsOnceEnable && gpsTrackerManager != null) {

                gpsTrackerManager.GPSTrackerStart();
            } else {

                DataUtil.enableLocationSettings(MainActivity.this);
            }
        }

        initMessageCount(opID);

        // TODO_TEST  badge
//        MyApplication myApp = (MyApplication) getApplicationContext();
//        myApp.setBadgeCnt(0);
        setBadge(getApplicationContext(), 0);

        if (opID.equals("")) {

            Toast.makeText(MainActivity.this, getResources().getString(R.string.msg_qdrive_auto_logout), Toast.LENGTH_SHORT).show();

            try {

                if (Preferences.INSTANCE.getUserNation().equals("SG")) {

                    Intent intent = new Intent(MainActivity.this, Class.forName("com.giosis.util.qdrive.singapore.LoginActivity"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } else {

                    Intent intent = new Intent(MainActivity.this, Class.forName("com.giosis.util.qdrive.international.LoginActivity"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            } catch (Exception e) {

                Log.e("Exception", "  Exception : " + e.toString());
                Toast.makeText(MainActivity.this, "Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }


        //Qx 소속 드라이버는 Assign 된 픽업 조회
        if (officeName.contains("Qxpress")) {
            new PickupAssignCheckHelper.Builder(this, opID)
                    .setOnPickupAssignCheckListener(new PickupAssignCheckHelper.OnPickupAssignCheckListener() {
                        @Override
                        public void onDownloadResult(Integer result) {
                            getLocalCount();
                        }

                        @Override
                        public void onDownloadFailList(Integer result) {
                            //Deactivated 사용자
                            if (result == -1) {

                                String msg = getResources().getString(R.string.msg_your_account_deactivated);
                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage(msg)
                                        .setTitle(getResources().getString(R.string.text_alert))
                                        .setCancelable(false).setPositiveButton(getResources().getString(R.string.button_ok),
                                        (dialog, which) -> finish()).show();
                            }
                        }
                    }).build().execute();
        }

        getLocalCount();
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
                String rpcCnt = cs.getString(cs.getColumnIndex("InprogressRpcCnt"));
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

            Log.e("Exception", TAG + " - getLocalCount() Exception : " + e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

                isPermissionTrue = true;
                GPSTrackerServiceStart();
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    private Boolean DownloadCheck() {

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

            Log.e("Exception", TAG + " - DownloadCheck Exception : " + e.toString());
        }

        return true;
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
            Log.e("Location", TAG + " - Upload() > " + latitude + ", " + longitude);
        }

        if (songjanglist.size() > 0) {

            DataUtil.logEvent("button_click", TAG, DataUtil.requestSetUploadDeliveryData + "/" + DataUtil.requestSetUploadPickupData);

            new DeviceDataUploadHelper.Builder(this, opID, officeCode, deviceID,
                    songjanglist, "QH", latitude, longitude).
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

            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.msg_no_data_to_upload))
                    .setTitle(getResources().getString(R.string.button_upload))
                    .setCancelable(false).setPositiveButton(getResources().getString(R.string.button_ok),
                    (dialog, which) -> {
                    }).show();
        }
    }

    private void Download() {

        if (!NetworkUtil.isNetworkAvailable(MainActivity.this)) {

            Toast.makeText(MainActivity.this, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show();
            return;
        }

        // 2020.12  Failed Code 가져오기
        DataUtil.requestServerPickupFailedCode();
        DataUtil.requestServerDeliveryFailedCode();

        //  2020.02 휴무일 가져오기
        int delete = DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_REST_DAYS, "");
        Log.e(TAG, "DELETE  DB_TABLE_REST_DAYS  Count : " + delete);
        new GetRestDaysAsyncTask(Preferences.INSTANCE.getUserNation(), Calendar.getInstance().get(Calendar.YEAR)).execute();
        new GetRestDaysAsyncTask(Preferences.INSTANCE.getUserNation(), Calendar.getInstance().get(Calendar.YEAR) + 1).execute();


        if (gpsOnceEnable && gpsTrackerManager != null) {

            latitude = gpsTrackerManager.getLatitude();
            longitude = gpsTrackerManager.getLongitude();
        }
        Log.e("Location", TAG + " - Download() > " + latitude + ", " + longitude);

        //
        if (0 < Integer.parseInt(uploadFailedCount)) {

            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.msg_download_not_supported))
                    .setTitle(getResources().getString(R.string.text_alert))
                    .setCancelable(false).setPositiveButton(getResources().getString(R.string.button_ok),
                    (dialog, which) -> {
                    }).show();


            return;
        }

        try {

            dbHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "");
        } catch (Exception e) {

            dbHelper = DatabaseHelper.getInstance();
            dbHelper.delete(DatabaseHelper.DB_TABLE_INTEGRATION_LIST, "");
            Log.e("Exception", TAG + "  DB Delete Exception : " + e.toString());

            try {

                Bundle params = new Bundle();
                params.putString("Activity", TAG);
                params.putString("message", "" + " DB.delete > " + e.toString());
                DataUtil.mFirebaseAnalytics.logEvent("error_exception", params);
            } catch (Exception ignored) {

            }
        }

        new ServerDownloadHelper.Builder(this, opID, officeCode, deviceID)
                .setOnServerDownloadEventListener(this::getLocalCount).build().execute();
    }

    public void GPSTrackerServiceStart() {

        if (pickup_driver_yn != null && pickup_driver_yn.equals("Y")) {

            // gps 켜는 alert 창  -> 객체 하나에만 호출해도 GPS 설정창 문제 없음(기기는 하나)
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            enableGPSSetting(locationManager);

            int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
            isGooglePlayService = ConnectionResult.SUCCESS == status;

            if (Build.MANUFACTURER.equals("HUAWEI") && Preferences.INSTANCE.getServerURL().contains("staging")) {  // KR 화웨이폰 - google 위치정보 못가져옴
                isGooglePlayService = false;
            }
//            // TEST.
//            isGooglePlayService = false;

            if (isGooglePlayService) {  // Fused Provider Service start (Google play 에 클라이언트 객체 얻어 서비스)

                if (DataUtil.getFusedProviderService() != null) {

                    Log.e(TAG, "  FusedProviderService - not null");
                } else {

                    Log.e(TAG, "  FusedProviderService - null");

                    fusedProviderService = new Intent(MainActivity.this, FusedProviderService.class);
                    ContextCompat.startForegroundService(this, fusedProviderService); // 내부에서 알아서 분기
                    DataUtil.setFusedProviderService(fusedProviderService);
                }
            } else { // location manager Service start (샤오미 등 구글 플레이 가 작동하지 않는 폰)

                if (DataUtil.getLocationManagerService() != null) {

                    Log.e(TAG, "  LocationManagerService - not null");
                } else {

                    Log.e(TAG, "  LocationManagerService - null");

                    locationManagerService = new Intent(MainActivity.this, LocationManagerService.class);
                    ContextCompat.startForegroundService(this, locationManagerService); // 내부에서 알아서 분기
                    DataUtil.setLocationManagerService(locationManagerService);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        DataUtil.stopGPSManager(gpsTrackerManager);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, " ** onDestroy()");

        if (!isHomeBtnClick) {  // home btn 누른게 아닐 때 작동해야 할 destroy method
            isHomeBtnClick = false;
            setDestroyUserInfo();
        }

        //Bluetooth Setting 화면 connection 없애기
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
            //   Toast.makeText(this.getApplicationContext(), mContext.getResources().getString(R.string.msg_qdrive_app_exited), Toast.LENGTH_SHORT).show();
        } else {
            isHomeBtnClick = false;
        }

        isHomeBtnClick = false;
    }

    public void enableGPSSetting(LocationManager locationManager) {

        if (locationManager != null) {

            boolean gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gpsEnable) {

                DataUtil.enableLocationSettings(MainActivity.this);
            }
        }
    }

    public void setDestroyUserInfo() {

        String api_level = Integer.toString(Build.VERSION.SDK_INT);     // API Level
        String device_info = Build.DEVICE;                   // Device
        String device_model = Build.MODEL;                   // Model
        String device_product = Build.PRODUCT;               // Product
        String device_os_version = System.getProperty("os.version");    // OS version
        Log.e(TAG, " DATA " + api_level + " / " + device_info + " / " + device_model + " / " + device_product + " / " + device_os_version);

        new QuickAppUserInfoUploadHelper.Builder(this, opID, "", api_level, device_info,
                device_model, device_product, device_os_version, "killapp")
                .setOnQuickQppUserInfoUploadEventListener(() -> {
                }).build().execute();

        double accuracy = 0;

        try {

            if (gpsOnceEnable && gpsTrackerManager != null) {

                latitude = gpsTrackerManager.getLatitude();
                longitude = gpsTrackerManager.getLongitude();
                accuracy = gpsTrackerManager.getAccuracy();

                Log.e("Location", TAG + " - setDestroyUserInfo() > " + latitude + ", " + longitude);
            }
        } catch (Exception e) {

            latitude = 0;
            longitude = 0;
        }


        new DriverPerformanceLogUploadHelper.Builder(this, opID, latitude, longitude, accuracy)
                .setOnDriverPerformanceLogUploadEventListener(() -> {
                }).build().execute();
    }


    @Override
    public void onBackPressed() {
    }

    public void saveServerFCMToken() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {

            fcmToken = instanceIdResult.getToken();
            sendAPIkey();
        });
    }

    private void sendAPIkey() {

        String op_id = Preferences.INSTANCE.getUserId();
        String device_id = Preferences.INSTANCE.getDeviceUUID();

        Log.i("FCM", TAG + "  Device ID : " + device_id + "  Device Token : " + fcmToken);

        // TEST
        if (device_id.equals("890525e99f30801a") || device_id.equals("acd248681b26f53f") || device_id.equals("b843772197349df9")) {

            Log.i("FCM", "REAL TEST~~~~~");
        } else if (!op_id.equals("") && !device_id.equals("") && !fcmToken.equals("")) {

            Log.i("FCM", "REAL~~~~~");
            saveFCMTokenTask = new saveFCMTokenTask().execute(fcmToken, op_id, device_id);
        }
    }


    @SuppressLint("StaticFieldLeak")
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

            String apiURL = Preferences.INSTANCE.getServerURL() + DataUtil.API_ADDRESS;
            URL url = new URL(apiURL + "/SetGCMUserKeyRegister");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setDefaultUseCaches(false);
            http.setDoInput(true);
            http.setDoOutput(true);
            http.setRequestMethod("POST");

            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            StringBuilder buffer = new StringBuilder();
            buffer.append("user_key").append("=").append(token).append("&");
            buffer.append("op_id").append("=").append(op_id).append("&");
            buffer.append("app_cd").append("=").append("01").append("&");               // 01.Qdrive / 02.QxQuick
            buffer.append("device_id").append("=").append(device_id).append("&");
            buffer.append("app_id").append("=").append(DataUtil.appID).append("&");
            buffer.append("nation_cd").append("=").append(Preferences.INSTANCE.getUserNation());
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

    public void initMessageCount(String driver_id) {

        if (NetworkUtil.isNetworkAvailable(MainActivity.this)) {

            MessageCountAsyncTask messageCountAsynctask = new MessageCountAsyncTask(driver_id);
            messageCountAsynctask.execute();
        } else {

            Toast.makeText(MainActivity.this, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show();
        }
    }

    // NOTIFICATION.  MessageCountAsyncTask
    @SuppressLint("StaticFieldLeak")
    private class MessageCountAsyncTask extends AsyncTask<Void, Void, ArrayList<Integer>> {

        String driverId;

        MessageCountAsyncTask(String driverID) {

            driverId = driverID;
        }

        @Override
        protected ArrayList<Integer> doInBackground(Void... params) {

            ArrayList<Integer> resultArray = new ArrayList<>();

            resultArray.add(getCustomerMessageCount());
            resultArray.add(getAdminMessageCount());

            return resultArray;
        }

        @Override
        protected void onPostExecute(ArrayList<Integer> results) {


            int customerMessageCount = results.get(0);
            int adminMessageCount = results.get(1);

            if (0 < customerMessageCount || 0 < adminMessageCount) {

                setMessageCount(customerMessageCount, adminMessageCount);
            } else {

                goneMessageCount();
            }
        }


        int getCustomerMessageCount() {

            int result = 0;

            try {

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1); //minus number would decrement the days
                Date yDate = cal.getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String yesterday = dateFormat.format(yDate) + " 00:00:00";

                JSONObject job = new JSONObject();
                job.accumulate("qdriver_id", driverId);
                job.accumulate("start_date", URLEncoder.encode(yesterday, "UTF-8"));
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

                String methodName = "GetNewMessageCount";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultObject":0,"ResultCode":0,"ResultMsg":"OK"}

                JSONObject jsonObject = new JSONObject(jsonString);
                result = jsonObject.getInt("ResultObject");
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetNewMessageCount Exception : " + e.toString());
            }

            return result;
        }

        int getAdminMessageCount() {

            int result = 0;


            try {

                JSONObject job = new JSONObject();
                job.accumulate("qdriver_id", driverId);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

                String methodName = "GetNewMessageCountFromQxSystem";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultObject":5,"ResultCode":0,"ResultMsg":"OK"}

                JSONObject jsonObject = new JSONObject(jsonString);
                result = jsonObject.getInt("ResultObject");
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetNewMessageCountFromQxSystem Exception : " + e.toString());
            }

            return result;
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
