package com.giosis.library.main;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.LocationManager;
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

import com.giosis.library.R;
import com.giosis.library.UploadData;
import com.giosis.library.barcodescanner.CaptureActivity1;
import com.giosis.library.gps.FusedProviderService;
import com.giosis.library.gps.GPSTrackerManager;
import com.giosis.library.gps.LocationManagerService;
import com.giosis.library.list.ListActivity;
import com.giosis.library.main.route.TodayMyRouteActivity;
import com.giosis.library.main.submenu.OutletOrderStatusActivity;
import com.giosis.library.main.submenu.RpcListActivity;
import com.giosis.library.pickup.CreatePickupOrderActivity;
import com.giosis.library.server.RetrofitClient;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;


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

    DatabaseHelper dbHelper;
    String uploadFailedCount = "0";
    String opID = "";

    Intent fusedProviderService = null;
    Intent locationManagerService = null;

    GPSTrackerManager gpsTrackerManager;
    boolean gpsEnable = false;
    double latitude = 0;
    double longitude = 0;

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
                startActivity(intent);
            } else if (id == R.id.layout_home_download) {

                Download();
            } else if (id == R.id.layout_home_upload) {

                Upload();
            } else if (id == R.id.btn_home_confirm_my_delivery_order) {

                Intent intent = new Intent(MainActivity.this, CaptureActivity1.class);
                intent.putExtra("title", getResources().getString(R.string.text_title_driver_assign));
                intent.putExtra("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER);
                startActivity(intent);
            } else if (id == R.id.btn_home_assign_pickup_driver) {

                Intent intent = new Intent(MainActivity.this, RpcListActivity.class);
                startActivity(intent);
            } else if (id == R.id.btn_home_change_delivery_driver) {

                if (gpsEnable && gpsTrackerManager != null) {

                    Intent intent = new Intent(MainActivity.this, CaptureActivity1.class);
                    intent.putExtra("title", getResources().getString(R.string.button_change_delivery_driver));
                    intent.putExtra("type", BarcodeType.CHANGE_DELIVERY_DRIVER);
                    startActivity(intent);
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

        QDataUtil.INSTANCE.setCustomUserAgent(MainActivity.this);
        DatabaseHelper.getInstance().getDbPath();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // FCM token
        saveServerFCMToken();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_main_home, getBinding().container, false);
        getBinding().drawerLayout.addView(contentView, 0);
        setTopTitle(getResources().getString(R.string.navi_home));

        // TEST_ Outlet
//        Preferences.INSTANCE.setOutletDriver("Y");
//        Preferences.INSTANCE.setUserId("Johari_ISM_Shuttle");      // 7Eleven.Ajib

        dbHelper = DatabaseHelper.getInstance();
        opID = Preferences.INSTANCE.getUserId();

        // 7ETB, FLTB push 받고 온 경우 확인
        String outletPush = getIntent().getStringExtra("outletPush");
        if (outletPush == null) {
            outletPush = "N";
        }

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
        btn_home_outlet_order_status = findViewById(R.id.btn_home_outlet_order_status);
        btn_home_create_pickup_order = findViewById(R.id.btn_home_create_pickup_order);
        btn_home_today_my_route = findViewById(R.id.btn_home_today_my_route);

        if (DownloadCheck()) {
            Download();
        } else if (outletPush.equals("Y")) {
            Download();
        }

        layout_home_list_count.setOnClickListener(clickListener);
        layout_home_download.setOnClickListener(clickListener);
        layout_home_upload.setOnClickListener(clickListener);

        img_home_driver_profile.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.qdrive_img_default));
        img_home_driver_profile.setBackground(new ShapeDrawable(new OvalShape()));
        img_home_driver_profile.setClipToOutline(true);

        if (Preferences.INSTANCE.getPickupDriver().equals("Y") &&
                Preferences.INSTANCE.getUserNation().equals("SG")) {
            btn_home_create_pickup_order.setVisibility(View.VISIBLE);
        } else {
            btn_home_create_pickup_order.setVisibility(View.GONE);
        }

        // NOTIFICATION.  추후 반영  MY/ID Route
//        if (!Preferences.INSTANCE.getUserNation().equalsIgnoreCase("SG")) {
//            btn_home_today_my_route.setVisibility(View.VISIBLE);
//        } else {
//            btn_home_today_my_route.setVisibility(View.GONE);
//        }

        // Outlet
        if (Preferences.INSTANCE.getOutletDriver().equals("Y")) {
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

        setNaviHeader(Preferences.INSTANCE.getUserName(), Preferences.INSTANCE.getOfficeName());
        text_home_driver_name.setText(Preferences.INSTANCE.getUserName());
        text_home_driver_office.setText(Preferences.INSTANCE.getOfficeName());

        DataUtil.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (isPermissionTrue) {

            gpsTrackerManager = new GPSTrackerManager(this);
            gpsEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsEnable && gpsTrackerManager != null) {

                gpsTrackerManager.gpsTrackerStart();
            } else {

                DataUtil.enableLocationSettings(MainActivity.this);
            }
        }

        initMessageCount();

        // TODO_TEST  badge
//        MyApplication myApp = (MyApplication) getApplicationContext();
//        myApp.setBadgeCnt(0);

        setBadge(getApplicationContext(), 0);

        if (Preferences.INSTANCE.getUserId().equals("")) {

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

        MainActivityServer.INSTANCE.getLocalCount(MainActivity.this);
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

        // TODO_kjyoo MainActivityServer.INSTANCE.upload() 로 변경중....

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

        if (gpsEnable && gpsTrackerManager != null) {
            latitude = gpsTrackerManager.getLatitude();
            longitude = gpsTrackerManager.getLongitude();
            Log.e("Location", TAG + " - Upload() > " + latitude + ", " + longitude);
        }

        if (songjanglist.size() > 0) {

            DataUtil.logEvent("button_click", TAG, "SetDeliveryUploadData / SetPickupUploadData");

            new DeviceDataUploadHelper.Builder(this, opID, Preferences.INSTANCE.getOfficeCode(), Preferences.INSTANCE.getDeviceUUID(),
                    songjanglist, "QH", latitude, longitude).
                    setOnServerEventListener(new OnServerEventListener() {

                        @Override
                        public void onPostResult() {
                            MainActivityServer.INSTANCE.getLocalCount(MainActivity.this);
                        }

                        @Override
                        public void onPostFailList() {
                            MainActivityServer.INSTANCE.getLocalCount(MainActivity.this);
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
        } else {

            MainActivityServer.INSTANCE.download(MainActivity.this);
        }

        if (0 < Integer.parseInt(uploadFailedCount)) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.msg_download_not_supported))
                    .setTitle(getResources().getString(R.string.text_alert))
                    .setCancelable(false).setPositiveButton(getResources().getString(R.string.button_ok),
                    (dialog, which) -> {
                    }).show();
        }
    }


    public void GPSTrackerServiceStart() {

        if (Preferences.INSTANCE.getPickupDriver().equals("Y")) {

            // gps 켜는 alert 창  -> 객체 하나에만 호출해도 GPS 설정창 문제 없음(기기는 하나)
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            enableGPSSetting(locationManager);

            int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
            Boolean isGooglePlayService = (ConnectionResult.SUCCESS == status);

            // TEST_
            // isGooglePlayService = false;

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

        MainActivityServer.INSTANCE.setDestroyUserInfo(this);
//        setDestroyUserInfo();

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
    }

    public void enableGPSSetting(LocationManager locationManager) {

        if (locationManager != null) {

            boolean gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gpsEnable) {

                DataUtil.enableLocationSettings(MainActivity.this);
            }
        }
    }


    @Override
    public void onBackPressed() {
        //
    }

    public void saveServerFCMToken() {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get new FCM registration token
                String fcmToken = task.getResult();

                RetrofitClient.INSTANCE.instanceDynamic().requestSetFCMToken(
                        fcmToken, Preferences.INSTANCE.getUserId(), "01",
                        Preferences.INSTANCE.getDeviceUUID(), DataUtil.appID, Preferences.INSTANCE.getUserNation()
                ).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(it -> {

                        }, it -> {

                        });
            }
        });
    }

    public void initMessageCount() {

        if (NetworkUtil.isNetworkAvailable(MainActivity.this)) {

            try {

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1); //minus number would decrement the days
                Date yDate = cal.getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String yesterday = dateFormat.format(yDate) + " 00:00:00";
                String date = URLEncoder.encode(yesterday, "UTF-8");

                RetrofitClient.INSTANCE.instanceDynamic().requestGetNewMessageCount(date, Preferences.INSTANCE.getUserId(),
                        DataUtil.appID, Preferences.INSTANCE.getUserNation())
                        .subscribeOn(Schedulers.io())
                        .subscribe(it -> {

                            if (it.getResultObject() != null) {
                                int count = new Gson().fromJson(it.getResultObject(), new TypeToken<Integer>() {
                                }.getType());

                                RetrofitClient.INSTANCE.instanceDynamic().requestGetNewMessageCountFromQxSystem(Preferences.INSTANCE.getUserId(),
                                        DataUtil.appID, Preferences.INSTANCE.getUserNation())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(it1 -> {
                                            if (it1.getResultObject() != null) {
                                                int adminCount = new Gson().fromJson(it1.getResultObject(), new TypeToken<Integer>() {
                                                }.getType());

                                                Log.e("Message", "count >>>> " + count + " / " + adminCount);

                                                if (0 < count || 0 < adminCount) {
                                                    setMessageCount(count, adminCount);
                                                } else {
                                                    goneMessageCount();
                                                }
                                            }
                                        }, it1 -> Log.e(RetrofitClient.errorTag, TAG + " - " + it1.toString()));
                            }
                        }, it -> Log.e(RetrofitClient.errorTag, TAG + " - " + it.toString()));
            } catch (Exception ignore) {
            }
        } else {

            Toast.makeText(MainActivity.this, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show();
        }
    }

}
