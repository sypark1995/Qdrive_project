package com.giosis.util.qdrive.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Environment;
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

import com.giosis.util.qdrive.barcodescanner.CaptureActivity;
import com.giosis.util.qdrive.barcodescanner.FailListActivity;
import com.giosis.util.qdrive.gps.FusedProviderService;
import com.giosis.util.qdrive.gps.GPSTrackerManager;
import com.giosis.util.qdrive.gps.LocationManagerService;
import com.giosis.util.qdrive.gps.QuickAppUserInfoUploadHelper;
import com.giosis.util.qdrive.list.ListActivity;
import com.giosis.util.qdrive.main.pickupOrder.ChoosePickupTypeActivity;
import com.giosis.util.qdrive.settings.BluetoothDeviceData;
import com.giosis.util.qdrive.singapore.LoginActivity;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.OnServerEventListener;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.UploadData;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;

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
    Button btn_home_create_pickup_order;
    Button btn_home_assign_pickup_driver;


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
    String authNo = "";

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

    //
    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION,
            PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        mContext = getApplicationContext();
        // FCM token
        saveServerFCMToken();

        // Settings - Scan Setting (vibration)
        SharedPreferences sharedPreferences = getSharedPreferences("PREF_SCAN_SETTING", Activity.MODE_PRIVATE);
        String vibrationString = sharedPreferences.getString("vibration", "0");

        if (vibrationString.equals("0")) {

            vibrationString = "OFF";
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("vibration", vibrationString);
            editor.apply();
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_main_home, container, false);
        drawerLayout.addView(contentView, 0);
        setTopTitle(mContext.getResources().getString(R.string.navi_home));


        // TEST.
        String imgDirName = "/QdrivePickup";
        String signName = "123";

        String dirPath = Environment.getExternalStorageDirectory().toString() + imgDirName;
        String filePath = dirPath + "/" + signName + ".png";
        String saveAbsolutePath = "file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                imgDirName + "/" + signName + ".png";
        Log.e("krm0219", "PATH 1 : " + dirPath + " / " + filePath + " / " + saveAbsolutePath);

        // FIXME.  API 29  deprecated
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String path1 = getApplication().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        Log.e("krm0219", "PATH 2 : " + path + " / " + path1);
        // ---


        dbHelper = DatabaseHelper.getInstance();

        opID = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
        opName = SharedPreferencesHelper.getSigninOpName(getApplicationContext());
        officeCode = SharedPreferencesHelper.getSigninOfficeCode(getApplicationContext());
        officeName = SharedPreferencesHelper.getSigninOfficeName(getApplicationContext());
        opDefault = SharedPreferencesHelper.getSigninOpDefaultYN(getApplicationContext());
        deviceID = SharedPreferencesHelper.getSigninDeviceID(getApplicationContext());
        authNo = SharedPreferencesHelper.getSigninAuthNo(getApplicationContext());
        pickup_driver_yn = SharedPreferencesHelper.getSigninPickupDriverYN(getApplicationContext());
        setNaviHeader(opName, officeName);

        // Outlet
        String outletDriverYN;
        try {

            outletDriverYN = SharedPreferencesHelper.getPrefSignInOutletDriver(getApplicationContext());
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
        btn_home_change_delivery_driver = findViewById(R.id.btn_home_change_delivery_driver);
        btn_home_outlet_order_status = findViewById(R.id.btn_home_outlet_order_status);    // 19.01 krm0219
        btn_home_create_pickup_order = findViewById(R.id.btn_home_create_pickup_order);

        getLocalCount();
        if (DownloadCheck()) {

            Download();
        } else if (outletPush.equals("Y")) {

            Download();
        }

        text_home_driver_name.setText(opName);
        text_home_driver_office.setText(officeName);

        layout_home_list_count.setOnClickListener(clickListener);
        layout_home_download.setOnClickListener(clickListener);
        layout_home_upload.setOnClickListener(clickListener);

        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qdrive_img_default);
        img_home_driver_profile.setImageBitmap(mBitmap);
        RoundedBitmapDrawable roundedImageDrawable = createRoundedBitmapImageDrawableWithBorder(mBitmap);
        img_home_driver_profile.setImageDrawable(roundedImageDrawable);

       /* if ("Y".equals(pickup_driver_yn)) {
            btn_home_create_pickup_order.setVisibility(View.VISIBLE);
        } else {
            btn_home_create_pickup_order.setVisibility(View.GONE);
        }*/

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

            if (btn_home_create_pickup_order.getVisibility() == View.VISIBLE) {
                btn_home_create_pickup_order.setLayoutParams(lp);
            } else {
                btn_home_change_delivery_driver.setLayoutParams(lp);
            }
        }

        // NOTIFICATION. 2020.06  POD Scan 제거
       /* //POD Scan 권한 : 파트너 사장 또는 Auth:91
        if (opDefault.equals("Y") || authNo.contains("91") || opID.equals("karam.kim")) {
            btn_home_scan_delivery_sheet.setVisibility(View.VISIBLE);
            btn_home_scan_delivery_sheet.setOnClickListener(clickListener);
        }*/
        btn_home_confirm_my_delivery_order.setOnClickListener(clickListener);
        btn_home_change_delivery_driver.setOnClickListener(clickListener);
        btn_home_outlet_order_status.setOnClickListener(clickListener);
        btn_home_create_pickup_order.setOnClickListener(clickListener);
        btn_home_assign_pickup_driver.setOnClickListener(clickListener);


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

        try {

            DataUtil.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  FA Exception : " + e.toString());
        }

        if (isPermissionTrue) {

            gpsTrackerManager = new GPSTrackerManager(mContext);
            gpsOnceEnable = gpsTrackerManager.enableGPSSetting();

            if (gpsOnceEnable && gpsTrackerManager != null) {

                gpsTrackerManager.GPSTrackerStart();
            } else {

                DataUtil.enableLocationSettings(MainActivity.this, mContext);
            }
        }

        opID = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
        initMessageCount(opID);

        MyApplication myApp = (MyApplication) getApplicationContext();
        myApp.setBadgeCnt(0);
        setBadge(getApplicationContext(), 0);

        if (opID.equals("")) {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

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
                if (failList != null && 0 < failList.length()) {
                    Intent failIntent = new Intent(MainActivity.this, FailListActivity.class);
                    failIntent.putExtra("failList", failList);
                    startActivity(failIntent);
                }
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

                isPermissionTrue = true;
                GPSTrackerServiceStart();
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

    private void Upload() {

        if (!NetworkUtil.isNetworkAvailable(MainActivity.this)) {

            Toast.makeText(MainActivity.this, getString(R.string.wifi_connect_failed), Toast.LENGTH_SHORT).show();
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

            DataUtil.logEvent("button_click", TAG, "SetDeliveryUploadData/SetPickupUploadData");

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
                    .setMessage(mContext.getResources().getString(R.string.msg_no_data_to_upload))
                    .setTitle(mContext.getResources().getString(R.string.button_upload))
                    .setCancelable(false).setPositiveButton(mContext.getResources().getString(R.string.button_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
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

    private void Download() {

       /* // TEST.  많은 데이터필요 / 하루에 한번만 데이터받고 테스트하기 !
        if (opID.equals("YuMin.Dwl"))
            return;*/

        //
        if (!NetworkUtil.isNetworkAvailable(MainActivity.this)) {

            Toast.makeText(MainActivity.this, getString(R.string.msg_network_connect_error), Toast.LENGTH_SHORT).show();
            return;
        }

        //  NOTIFICATION. 2020.02 login.js 삭제대비 - 휴무일 가져오기
        int delete = DatabaseHelper.getInstance().delete(DatabaseHelper.DB_TABLE_REST_DAYS, "");
        Log.e("krm0219", "DELETE  DB_TABLE_REST_DAYS  Count : " + delete);
        new GetRestDaysAsyncTask("SG", Calendar.getInstance().get(Calendar.YEAR)).execute();
        new GetRestDaysAsyncTask("SG", Calendar.getInstance().get(Calendar.YEAR) + 1).execute();


        if (gpsOnceEnable && gpsTrackerManager != null) {

            latitude = gpsTrackerManager.getLatitude();
            longitude = gpsTrackerManager.getLongitude();
        }
        Log.e("Location", TAG + " - Download() > " + latitude + ", " + longitude);

        //
        if (0 < Integer.parseInt(uploadFailedCount)) {

            new AlertDialog.Builder(this)
                    .setMessage(mContext.getResources().getString(R.string.msg_download_not_supported))
                    .setTitle(mContext.getResources().getString(R.string.text_alert))
                    .setCancelable(false).setPositiveButton(mContext.getResources().getString(R.string.button_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
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

        new ServerDownloadHelper.Builder(MainActivity.this, this, opID, officeCode, deviceID)
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

            int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
            isGooglePlayService = ConnectionResult.SUCCESS == status;

            //    Log.e("krm0219", TAG + "   MANUFACTURER = " + Build.MANUFACTURER); //제조사
            if (Build.MANUFACTURER.equals("HUAWEI") && MOBILE_SERVER_URL.contains("staging")) {  // KR 화웨이폰 - google 위치정보 못가져옴
                isGooglePlayService = false;
            }
            /*// TEST.
            isGooglePlayService = false;*/

            if (isGooglePlayService) {  // Fused Provider Service start (Google play 에 클라이언트 객체 얻어 서비스)

                if (DataUtil.getFusedProviderService() != null) {

                    Log.e("krm0219", TAG + "  FusedProviderService - not null");
                } else {

                    Log.e("krm0219", TAG + "  FusedProviderService - null");
                    fusedProviderService = new Intent(MainActivity.this, FusedProviderService.class);
                    ContextCompat.startForegroundService(this, fusedProviderService); // 내부에서 알아서 분기
                    DataUtil.setFusedProviderService(fusedProviderService);
                }
            } else { // location manager Service start (샤오미 등 구글 플레이 가 작동하지 않는 폰)

                if (DataUtil.getLocationManagerService() != null) {

                    Log.e("krm0219", TAG + "  LocationManagerService - not null");
                } else {

                    Log.e("krm0219", TAG + "  LocationManagerService - null");
                    locationManagerService = new Intent(MainActivity.this, LocationManagerService.class);
                    ContextCompat.startForegroundService(this, locationManagerService); // 내부에서 알아서 분기
                    DataUtil.setLocationManagerService(locationManagerService);
                }
            }
        }
    }

    public void enableGPSSetting(LocationManager locationManager) {

        if (locationManager != null) {

            boolean gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gpsEnable) {

                DataUtil.enableLocationSettings(MainActivity.this, mContext);
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

    public void setDestroyUserInfo() {

        String api_level = Integer.toString(Build.VERSION.SDK_INT);     // API Level
        String device_info = android.os.Build.DEVICE;                   // Device
        String device_model = android.os.Build.MODEL;                   // Model
        String device_product = android.os.Build.PRODUCT;               // Product
        String device_os_version = System.getProperty("os.version");    // OS version
        Log.e(TAG, " DATA " + api_level + " / " + device_info + " / " + device_model + " / " + device_product + " / " + device_os_version);

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

                Log.e("Location", TAG + " - setDestroyUserInfo() > " + latitude + ", " + longitude);
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

                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    intent.putExtra("title", mContext.getResources().getString(R.string.text_title_driver_assign));
                    intent.putExtra("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER);
                    startActivityForResult(intent, 1);
                }
                break;

                case R.id.btn_home_assign_pickup_driver: {

                    Intent intent = new Intent(MainActivity.this, RpcListActivity.class);
                    startActivity(intent);
                }
                break;

                case R.id.btn_home_change_delivery_driver: {

                    if (gpsOnceEnable && gpsTrackerManager != null) {

                        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                        intent.putExtra("title", mContext.getResources().getString(R.string.button_change_delivery_driver));
                        intent.putExtra("type", BarcodeType.CHANGE_DELIVERY_DRIVER);
                        startActivity(intent);
                    } else {

                        DataUtil.enableLocationSettings(MainActivity.this, mContext);
                    }
                }
                break;

                case R.id.btn_home_outlet_order_status: {

                    Intent intent = new Intent(MainActivity.this, OutletOrderStatusActivity.class);
                    startActivity(intent);
                }
                break;

                case R.id.btn_home_create_pickup_order: {
                    Intent intent = new Intent(MainActivity.this, ChoosePickupTypeActivity.class);
                    startActivity(intent);
                }
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

                sendAPIkey();
            }
        });
    }

    private void sendAPIkey() {

        String op_id = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
        String device_id = SharedPreferencesHelper.getSigninDeviceID(getApplicationContext());
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

            URL url = new URL(MOBILE_SERVER_URL + "/SetGCMUserKeyRegister");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
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
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "GetNewMessageCount";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
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
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "GetNewMessageCountFromQxSystem";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
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