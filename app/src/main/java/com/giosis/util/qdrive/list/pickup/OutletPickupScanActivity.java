package com.giosis.util.qdrive.list.pickup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.barcodescanner.CaptureActivity;
import com.giosis.util.qdrive.list.OutletInfo;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.Custom_XmlPullParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import gmkt.inc.android.common.GMKT_SyncHttpTask;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;

/**
 * @author krm0219
 * LIST > In-Progress > Outlet Pickup Done (Step 1)
 */
public class OutletPickupScanActivity extends AppCompatActivity {
    String TAG = "OutletPickupScanActivity";

    //krm0219
    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_sign_p_outlet_address_title;
    TextView text_sign_p_outlet_address;
    LinearLayout layout_sign_p_outlet_operation_hour;
    TextView text_sign_p_outlet_operation_time;
    TextView text_sign_p_outlet_pickup_no;
    TextView text_sign_p_outlet_applicant;
    TextView text_sign_p_outlet_total_qty;

    RelativeLayout layout_sign_p_outlet_7e_info;
    RelativeLayout layout_sign_p_outlet_qrcode;
    TextView text_sign_p_outlet_date;
    TextView text_sign_p_outlet_job_id;
    TextView text_sign_p_outlet_vendor_code;
    ImageView img_sign_p_outlet_qrcode;
    LinearLayout layout_sign_p_outlet_qrcode_error;
    Button btn_sign_p_outlet_reload;

    ListView list_sign_p_outlet_tracking_no;
    Button btn_sign_p_outlet_next;


    //
    Context context;
    String opID = "";
    String officeCode = "";
    String deviceID = "";

    String mTitle;
    String mPickupNo;
    String mApplicant;
    String mQty;
    String mRoute;

    OutletInfo outletInfo;
    String jobID;
    String vendorCode;
    boolean showQRCode = false;
    OutletPickupDoneResult result = null;
    OutletPickupDoneTrackingNoAdapter outletPickupDoneTrackingNoAdapter;

    ProgressDialog progressDialog = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet_pickup_scan);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_sign_p_outlet_address_title = findViewById(R.id.text_sign_p_outlet_address_title);
        text_sign_p_outlet_address = findViewById(R.id.text_sign_p_outlet_address);
        layout_sign_p_outlet_operation_hour = findViewById(R.id.layout_sign_p_outlet_operation_hour);
        text_sign_p_outlet_operation_time = findViewById(R.id.text_sign_p_outlet_operation_time);
        text_sign_p_outlet_pickup_no = findViewById(R.id.text_sign_p_outlet_pickup_no);
        text_sign_p_outlet_applicant = findViewById(R.id.text_sign_p_outlet_applicant);
        text_sign_p_outlet_total_qty = findViewById(R.id.text_sign_p_outlet_total_qty);

        layout_sign_p_outlet_7e_info = findViewById(R.id.layout_sign_p_outlet_7e_info);
        layout_sign_p_outlet_qrcode = findViewById(R.id.layout_sign_p_outlet_qrcode);
        text_sign_p_outlet_date = findViewById(R.id.text_sign_p_outlet_date);
        text_sign_p_outlet_job_id = findViewById(R.id.text_sign_p_outlet_job_id);
        text_sign_p_outlet_vendor_code = findViewById(R.id.text_sign_p_outlet_vendor_code);
        img_sign_p_outlet_qrcode = findViewById(R.id.img_sign_p_outlet_qrcode);
        layout_sign_p_outlet_qrcode_error = findViewById(R.id.layout_sign_p_outlet_qrcode_error);
        btn_sign_p_outlet_reload = findViewById(R.id.btn_sign_p_outlet_reload);

        list_sign_p_outlet_tracking_no = findViewById(R.id.list_sign_p_outlet_tracking_no);
        btn_sign_p_outlet_next = findViewById(R.id.btn_sign_p_outlet_next);

        layout_top_back.setOnClickListener(clickListener);
        btn_sign_p_outlet_reload.setOnClickListener(clickListener);
        btn_sign_p_outlet_next.setOnClickListener(clickListener);


        //
        context = getApplicationContext();
        opID = SharedPreferencesHelper.getSigninOpID(context);
        officeCode = SharedPreferencesHelper.getSigninOfficeCode(context);
        deviceID = SharedPreferencesHelper.getSigninDeviceID(context);

        mTitle = getIntent().getStringExtra("title");
        mPickupNo = getIntent().getStringExtra("pickup_no");
        mApplicant = getIntent().getStringExtra("applicant");
        mQty = getIntent().getStringExtra("qty");

        outletInfo = getOutletInfo(mPickupNo);

        mRoute = outletInfo.route.substring(0, 2);
        Log.e("krm0219", TAG + " Data : " + mRoute + " / " + mPickupNo);

        text_top_title.setText(mTitle);
        if (mRoute.equals("FL")) {
            text_top_title.setText(R.string.text_title_fl_pickup);
        }
        text_sign_p_outlet_address.setText("(" + outletInfo.zip_code + ") " + outletInfo.address);

        // 2019.04
        String outletAddress = outletInfo.address.toUpperCase();
        String operationHour = null;
        Log.e("krm0219", "TEST  Operation : " + outletInfo.address + " / " + context.getResources().getString(R.string.text_operation_hours));

        if (outletAddress.contains(context.getResources().getString(R.string.text_operation_hours).toUpperCase())) {

            String indexString = "(" + context.getResources().getString(R.string.text_operation_hours).toUpperCase() + ":";
            int operationHourIndex = outletAddress.indexOf(indexString);

            operationHour = outletInfo.address.substring(operationHourIndex + indexString.length(), outletAddress.length() - 1);
            outletAddress = outletInfo.address.substring(0, operationHourIndex);
            Log.e("krm0219", "TEST  Operation Hour : " + operationHour + " / " + operationHourIndex);
        } else if (outletAddress.contains(context.getResources().getString(R.string.text_operation_hour).toUpperCase())) {

            String indexString = "(" + context.getResources().getString(R.string.text_operation_hour).toUpperCase() + ":";
            int operationHourIndex = outletAddress.indexOf(indexString);

            operationHour = outletInfo.address.substring(operationHourIndex + indexString.length(), outletAddress.length() - 1);
            outletAddress = outletInfo.address.substring(0, operationHourIndex);
            Log.e("krm0219", "TEST  Operation Hour : " + operationHour + " / " + operationHourIndex);
        }

        if (operationHour != null) {

            layout_sign_p_outlet_operation_hour.setVisibility(View.VISIBLE);
            text_sign_p_outlet_operation_time.setText(operationHour);
        }

        text_sign_p_outlet_address.setText("(" + outletInfo.zip_code + ") " + outletAddress);


        text_sign_p_outlet_pickup_no.setText(mPickupNo);
        text_sign_p_outlet_applicant.setText(mApplicant);
        text_sign_p_outlet_total_qty.setText(mQty);

        progressDialog = new ProgressDialog(OutletPickupScanActivity.this);

        if (mRoute.contains("7E")) {

            text_sign_p_outlet_address_title.setText(R.string.text_7e_store_address);
            layout_sign_p_outlet_7e_info.setVisibility(View.VISIBLE);

            OutletPickupDoneAsyncTask outletPickupDoneAsyncTask = new OutletPickupDoneAsyncTask(getString(R.string.text_outlet_7e), mPickupNo);
            outletPickupDoneAsyncTask.execute();
        } else if (mRoute.contains("FL")) {

            text_sign_p_outlet_address_title.setText(R.string.text_federated_locker_address);
            layout_sign_p_outlet_7e_info.setVisibility(View.GONE);

            OutletPickupDoneAsyncTask outletPickupDoneAsyncTask = new OutletPickupDoneAsyncTask(getString(R.string.text_fl), mPickupNo);
            outletPickupDoneAsyncTask.execute();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (result != null) {

                for (int i = 0; i < result.getTrackingNoList().size(); i++) {
                    result.getTrackingNoList().get(i).setScanned(false);
                }
            }
        } catch (Exception e) {

            Log.e("krm0219", TAG + "  onResume Exception : " + e.toString() + "  ::  PickupNo : " + mPickupNo);
            progressDialog = new ProgressDialog(OutletPickupScanActivity.this);

            if (mRoute.contains("7E")) {

                layout_sign_p_outlet_7e_info.setVisibility(View.VISIBLE);

                OutletPickupDoneAsyncTask outletPickupDoneAsyncTask = new OutletPickupDoneAsyncTask(getString(R.string.text_outlet_7e), mPickupNo);
                outletPickupDoneAsyncTask.execute();
            } else if (mRoute.contains("FL")) {

                layout_sign_p_outlet_7e_info.setVisibility(View.GONE);

                OutletPickupDoneAsyncTask outletPickupDoneAsyncTask = new OutletPickupDoneAsyncTask(getString(R.string.text_fl), mPickupNo);
                outletPickupDoneAsyncTask.execute();
            }
        }
    }

    public OutletInfo getOutletInfo(String barcodeNo) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        Cursor cursor = dbHelper.get("SELECT route, zip_code, address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        OutletInfo outletInfo = new OutletInfo();

        if (cursor.moveToFirst()) {
            outletInfo.route = cursor.getString(cursor.getColumnIndexOrThrow("route"));
            outletInfo.zip_code = cursor.getString(cursor.getColumnIndexOrThrow("zip_code"));
            outletInfo.address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        }

        if (cursor != null) cursor.close();

        return outletInfo;
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.layout_top_back: {

                    finish();
                }
                break;

                case R.id.btn_sign_p_outlet_reload: {

                    progressDialog = new ProgressDialog(OutletPickupScanActivity.this);

                    if (mRoute.contains("7E")) {

                        layout_sign_p_outlet_7e_info.setVisibility(View.VISIBLE);

                        OutletPickupDoneAsyncTask outletPickupDoneAsyncTask = new OutletPickupDoneAsyncTask(getString(R.string.text_outlet_7e), mPickupNo);
                        outletPickupDoneAsyncTask.execute();
                    } else if (mRoute.contains("FL")) {

                        layout_sign_p_outlet_7e_info.setVisibility(View.GONE);

                        OutletPickupDoneAsyncTask outletPickupDoneAsyncTask = new OutletPickupDoneAsyncTask(getString(R.string.text_fl), mPickupNo);
                        outletPickupDoneAsyncTask.execute();
                    }
                }
                break;

                case R.id.btn_sign_p_outlet_next: {

                    Log.e("krm0219", "count : " + result.getTrackingNoList().size());

                    if (showQRCode) {        // QR Code Show

                        if (0 < result.getTrackingNoList().size()) {
                            Intent intentScan = new Intent(OutletPickupScanActivity.this, CaptureActivity.class);

                            intentScan.putExtra("title", mTitle);
                            intentScan.putExtra("type", BarcodeType.OUTLET_PICKUP_SCAN);
                            intentScan.putExtra("pickup_no", mPickupNo);
                            intentScan.putExtra("applicant", mApplicant);
                            intentScan.putExtra("qty", mQty);
                            intentScan.putExtra("tracking_data", result);
                            intentScan.putExtra("route", mRoute);

                            CaptureActivity.removeBarcodeListInstance();

                            startActivityForResult(intentScan, 13);
                        } else {

                            Toast.makeText(OutletPickupScanActivity.this, "Not Exist Tracking No.", Toast.LENGTH_SHORT).show();
                        }
                    } else {                // QR Code Not Show... > 진행 불가능

                        if (mRoute.contains("7E")) {

                        } else if (mRoute.contains("FL")) {

                            Toast.makeText(OutletPickupScanActivity.this, "Reload the QR code", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            }
        }
    };


    public class OutletPickupDoneAsyncTask extends AsyncTask<Void, Void, OutletPickupDoneResult> {

        String outlet_type;
        String pickup_no;

        public OutletPickupDoneAsyncTask(String outletType, String trackingNo) {

            this.outlet_type = outletType;
            this.pickup_no = trackingNo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected OutletPickupDoneResult doInBackground(Void... params) {

            try {

                GMKT_SyncHttpTask httpTask = new GMKT_SyncHttpTask("QSign");
                HashMap<String, String> hmActionParam = new HashMap<>();
                hmActionParam.put("outletType", outlet_type);
                hmActionParam.put("pickupNo", pickup_no);
                hmActionParam.put("app_id", DataUtil.appID);
                hmActionParam.put("nation_cd", DataUtil.nationCode);
                Log.e("Server", TAG + " data : " + outlet_type + " / " + pickup_no);

                String methodName = "GetCollectionPickupNoList";

                GMKT_HTTPResponseMessage response = httpTask.requestServerDataReturnString(MOBILE_SERVER_URL, methodName, hmActionParam);
                String resultString = response.getResultString();
                Log.e("Server", methodName + "  Result : " + resultString);
                // <ResultCode>0</ResultCode><ResultMsg>Success</ResultMsg><ResultObject><PickupNo>7E48528</PickupNo><JobNumber>CC20190821002</JobNumber><QRCode>{"Q":"C","J":"CC20190821002","V":"QX","S":"260","C":1}</QRCode><ListTrackingNo>["SGP148789305"]</ListTrackingNo></ResultObject>

                result = Custom_XmlPullParser.getOutletPickupDoneData(resultString);

                if (result != null && outlet_type.equals("7E")) {

                    JSONObject jsonObject = new JSONObject(result.getQRCode());
                    String type = jsonObject.getString("Q");

                    if (!type.equals("C")) {

                        return null;
                    }

                    jobID = jsonObject.getString("J");
                    if (jobID == null || jobID.equalsIgnoreCase("")) {

                        return null;
                    }

                    vendorCode = jsonObject.getString("V");
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetCollectionPickupNoList Exception : " + e.toString());
                return null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(OutletPickupDoneResult result) {
            super.onPostExecute(result);

            try {
                if (result != null && result.getResultCode().equals("0")) {

                    if (outlet_type.equals("7E")) {

                        QRCodeAsyncTask qrCodeAsyncTask = new QRCodeAsyncTask(result.getQRCode());
                        qrCodeAsyncTask.execute();
                    } else if (outlet_type.equals("FL")) {

                        if (progressDialog.isShowing())
                            progressDialog.dismiss();

                        showQRCode = true;
                        /*
                        // TODO TEST
                        result.setTrackingNumbers("SGP1234,SGP1235,SGP1236");*/
                    }

                    if (mQty.equals("0")) {

                        mQty = Integer.toString(result.getTrackingNoList().size());
                        text_sign_p_outlet_total_qty.setText(mQty);
                    }

                    outletPickupDoneTrackingNoAdapter = new OutletPickupDoneTrackingNoAdapter(context, result, mRoute);
                    outletPickupDoneTrackingNoAdapter.notifyDataSetChanged();
                    list_sign_p_outlet_tracking_no.setAdapter(outletPickupDoneTrackingNoAdapter);
                    setListViewHeightBasedOnChildren(list_sign_p_outlet_tracking_no);
                } else {

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();

                    showQRCode = false;

                    if (outlet_type.equals("7E")) {

                        layout_sign_p_outlet_qrcode.setVisibility(View.GONE);
                        layout_sign_p_outlet_qrcode_error.setVisibility(View.VISIBLE);
                    } else {

                        Toast.makeText(OutletPickupScanActivity.this, "GetCollectionPickupNoList  Error..", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                showQRCode = false;

                if (outlet_type.equals("7E")) {

                    layout_sign_p_outlet_qrcode.setVisibility(View.GONE);
                    layout_sign_p_outlet_qrcode_error.setVisibility(View.VISIBLE);
                } else {

                    Toast.makeText(OutletPickupScanActivity.this, "GetCollectionPickupNoList  Error..\n" + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public class QRCodeAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        String qrcode_data;

        String imgUrl;

        public QRCodeAsyncTask(String data) {

            this.qrcode_data = data;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            try {

                imgUrl = DataUtil.qrcode_url + qrcode_data;
                Log.e("krm0219", TAG + " QR CODE ULR = " + imgUrl);

                URL url = new URL(imgUrl);
                trustAllHosts();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                return myBitmap;
            } catch (Exception e) {

                Log.e("krm0219", "QRCodeAsyncTask Exception : " + e.toString());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (bitmap != null) {

                showQRCode = true;

                layout_sign_p_outlet_qrcode.setVisibility(View.VISIBLE);
                layout_sign_p_outlet_qrcode_error.setVisibility(View.GONE);

                text_sign_p_outlet_date.setText(jobID.substring(2, 6) + "-" + jobID.substring(6, 8) + "-" + jobID.substring(8, 10));
                text_sign_p_outlet_job_id.setText(jobID);
                text_sign_p_outlet_vendor_code.setText(vendorCode);
                img_sign_p_outlet_qrcode.setImageBitmap(bitmap);
            } else {

                showQRCode = false;

                layout_sign_p_outlet_qrcode.setVisibility(View.GONE);
                layout_sign_p_outlet_qrcode_error.setVisibility(View.VISIBLE);
            }
        }

        private void trustAllHosts() {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            }};

            // Install the all-trusting trust manager
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }
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