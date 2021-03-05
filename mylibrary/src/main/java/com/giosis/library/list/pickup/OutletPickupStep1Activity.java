package com.giosis.library.list.pickup;

import android.app.ProgressDialog;
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

import com.giosis.library.R;
import com.giosis.library.barcodescanner.CaptureActivity;
import com.giosis.library.list.OutletInfo;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.BarcodeType;
import com.giosis.library.util.CommonActivity;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.library.util.DisplayUtil;
import com.giosis.library.util.Preferences;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author krm0219
 * LIST > In-Progress > Outlet Pickup Done (Step 1)
 */
public class OutletPickupStep1Activity extends CommonActivity {
    String TAG = "OutletPickupStep1Activity";

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
    Gson gson = new Gson();

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
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int id = view.getId();
            if (id == R.id.layout_top_back) {

                finish();
            } else if (id == R.id.btn_sign_p_outlet_reload) {

                progressDialog = new ProgressDialog(OutletPickupStep1Activity.this);

                if (mRoute.contains("7E")) {

                    layout_sign_p_outlet_7e_info.setVisibility(View.VISIBLE);

                    OutletPickupDoneAsyncTask outletPickupDoneAsyncTask = new OutletPickupDoneAsyncTask(getString(R.string.text_outlet_7e), mPickupNo);
                    outletPickupDoneAsyncTask.execute();
                } else if (mRoute.contains("FL")) {


                    layout_sign_p_outlet_7e_info.setVisibility(View.GONE);

                    OutletPickupDoneAsyncTask outletPickupDoneAsyncTask = new OutletPickupDoneAsyncTask(getString(R.string.text_fl), mPickupNo);
                    outletPickupDoneAsyncTask.execute();
                }
            } else if (id == R.id.btn_sign_p_outlet_next) {

                Log.e("krm0219", "count : " + result.getResultObject().getTrackingNoList().size());

                if (showQRCode) {        // QR Code Show

                    if (0 < result.getResultObject().getTrackingNoList().size()) {

//                        try {
//
//                            Intent intent = new Intent(OutletPickupStep1Activity.this, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivityTemp"));
//                            intent.putExtra("title", mTitle);
//                            intent.putExtra("type", BarcodeType.OUTLET_PICKUP_SCAN);
//                            intent.putExtra("pickup_no", mPickupNo);
//                            intent.putExtra("applicant", mApplicant);
//                            intent.putExtra("qty", mQty);
//                            intent.putExtra("tracking_data", result);
//                            intent.putExtra("route", mRoute);
//                            startActivity(intent);
//                        } catch (Exception e) {
//
//                            Log.e("Exception", "  Exception : " + e.toString());
//                            Toast.makeText(OutletPickupStep1Activity.this, "Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
//                        }

                        // FIXME_ New CaptureActivity
                        Intent intent = new Intent(OutletPickupStep1Activity.this, CaptureActivity.class);
                        intent.putExtra("title", mTitle);
                        intent.putExtra("type", BarcodeType.OUTLET_PICKUP_SCAN);
                        intent.putExtra("pickup_no", mPickupNo);
                        intent.putExtra("applicant", mApplicant);
                        intent.putExtra("qty", mQty);
                        intent.putExtra("tracking_data", result);
                        intent.putExtra("route", mRoute);
                        startActivity(intent);
                    } else {

                        Toast.makeText(OutletPickupStep1Activity.this, "Not Exist Tracking No.", Toast.LENGTH_SHORT).show();
                    }
                } else {                // QR Code Not Show... > 진행 불가능

                    if (mRoute.contains("7E")) {

                    } else if (mRoute.contains("FL")) {

                        Toast.makeText(OutletPickupStep1Activity.this, "Reload the QR code", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet_pickup_step1);

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
        mTitle = getIntent().getStringExtra("title");
        mPickupNo = getIntent().getStringExtra("pickup_no");
        mApplicant = getIntent().getStringExtra("applicant");
        mQty = getIntent().getStringExtra("qty");

        outletInfo = getOutletInfo(mPickupNo);

       /* // TEST
        outletInfo.route = "7E";
        outletInfo.zip_code = "123";
        outletInfo.address = "address";*/


        mRoute = outletInfo.getRoute().substring(0, 2);
        Log.e("krm0219", TAG + " Data : " + mRoute + " / " + mPickupNo);

        text_top_title.setText(mTitle);
        if (mRoute.equals("FL")) {
            text_top_title.setText(R.string.text_title_fl_pickup);
        }
        text_sign_p_outlet_address.setText("(" + outletInfo.getZip_code() + ") " + outletInfo.getAddress());

        // 2019.04
        String outletAddress = outletInfo.getAddress().toUpperCase();
        String operationHour = null;
        Log.e("krm0219", "TEST  Operation : " + outletInfo.getAddress() + " / " + getResources().getString(R.string.text_operation_hours));

        if (outletAddress.contains(getResources().getString(R.string.text_operation_hours).toUpperCase())) {

            String indexString = "(" + getResources().getString(R.string.text_operation_hours).toUpperCase() + ":";
            int operationHourIndex = outletAddress.indexOf(indexString);

            operationHour = outletInfo.getAddress().substring(operationHourIndex + indexString.length(), outletAddress.length() - 1);
            outletAddress = outletInfo.getAddress().substring(0, operationHourIndex);
            Log.e("krm0219", "TEST  Operation Hour : " + operationHour + " / " + operationHourIndex);
        } else if (outletAddress.contains(getResources().getString(R.string.text_operation_hour).toUpperCase())) {

            String indexString = "(" + getResources().getString(R.string.text_operation_hour).toUpperCase() + ":";
            int operationHourIndex = outletAddress.indexOf(indexString);

            operationHour = outletInfo.getAddress().substring(operationHourIndex + indexString.length(), outletAddress.length() - 1);
            outletAddress = outletInfo.getAddress().substring(0, operationHourIndex);
            Log.e("krm0219", "TEST  Operation Hour : " + operationHour + " / " + operationHourIndex);
        }

        if (operationHour != null) {

            layout_sign_p_outlet_operation_hour.setVisibility(View.VISIBLE);
            text_sign_p_outlet_operation_time.setText(operationHour);
        }

        text_sign_p_outlet_address.setText("(" + outletInfo.getZip_code() + ") " + outletAddress);


        text_sign_p_outlet_pickup_no.setText(mPickupNo);
        text_sign_p_outlet_applicant.setText(mApplicant);
        text_sign_p_outlet_total_qty.setText(mQty);

        progressDialog = new ProgressDialog(OutletPickupStep1Activity.this);

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

                for (int i = 0; i < result.getResultObject().getTrackingNoList().size(); i++) {
                    result.getResultObject().getTrackingNoList().get(i).setScanned(false);
                }
            }
        } catch (Exception e) {

            Log.e("krm0219", TAG + "  onResume Exception : " + e.toString() + "  ::  PickupNo : " + mPickupNo);
            progressDialog = new ProgressDialog(OutletPickupStep1Activity.this);

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

        Cursor cursor = DatabaseHelper.getInstance().get("SELECT route, zip_code, address FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE invoice_no='" + barcodeNo + "' COLLATE NOCASE");

        OutletInfo outletInfo = new OutletInfo();

        if (cursor.moveToFirst()) {
            outletInfo.setRoute(cursor.getString(cursor.getColumnIndexOrThrow("route")));
            outletInfo.setZip_code(cursor.getString(cursor.getColumnIndexOrThrow("zip_code")));
            outletInfo.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
        }

        if (cursor != null) cursor.close();

        return outletInfo;
    }

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
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected OutletPickupDoneResult doInBackground(Void... params) {

            try {

                JSONObject job = new JSONObject();
                job.accumulate("outletType", outlet_type);
                job.accumulate("pickupNo", pickup_no);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());
                Log.e("Server", TAG + " data : " + outlet_type + " / " + pickup_no);


                String methodName = "GetCollectionPickupNoList";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);

                result = gson.fromJson(jsonString, OutletPickupDoneResult.class);

                Log.e("krm0219", "111");

                if (result != null && outlet_type.equals("7E")) {

                    Log.e("krm0219", "222   " + result.getResultObject().TrackingNumbers);

                    result.getResultObject().setTrackingNumbers(result.getResultObject().getTrackingNumbers());
                    JSONObject jsonObject = new JSONObject(result.getResultObject().getQRCode());
                    String type = jsonObject.getString("Q");

                    if (!type.equals("C")) {

                        return null;
                    }

                    jobID = jsonObject.getString("J");
                    if (jobID.equalsIgnoreCase("")) {

                        return null;
                    }

                    vendorCode = jsonObject.getString("V");
                }

            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetCollectionPickupNoList Json Exception : " + e.toString());
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

                        QRCodeAsyncTask qrCodeAsyncTask = new QRCodeAsyncTask(result.getResultObject().getQRCode());
                        qrCodeAsyncTask.execute();
                    } else if (outlet_type.equals("FL")) {

                        DisplayUtil.dismissProgressDialog(progressDialog);

                        showQRCode = true;

                        /* //  TEST
                        result.setTrackingNumbers("SGP1234,SGP1235,SGP1236");*/
                    }

                    if (mQty.equals("0")) {

                        mQty = Integer.toString(result.getResultObject().getTrackingNoList().size());
                        text_sign_p_outlet_total_qty.setText(mQty);
                    }

                    outletPickupDoneTrackingNoAdapter = new OutletPickupDoneTrackingNoAdapter(OutletPickupStep1Activity.this, result, mRoute);
                    outletPickupDoneTrackingNoAdapter.notifyDataSetChanged();
                    list_sign_p_outlet_tracking_no.setAdapter(outletPickupDoneTrackingNoAdapter);
                    setListViewHeightBasedOnChildren(list_sign_p_outlet_tracking_no);
                } else {

                    DisplayUtil.dismissProgressDialog(progressDialog);

                    showQRCode = false;

                    if (outlet_type.equals("7E")) {

                        layout_sign_p_outlet_qrcode.setVisibility(View.GONE);
                        layout_sign_p_outlet_qrcode_error.setVisibility(View.VISIBLE);
                    } else {

                        Toast.makeText(OutletPickupStep1Activity.this, "GetCollectionPickupNoList  Error..", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  onPostExecute Exception : " + e.toString());
                DisplayUtil.dismissProgressDialog(progressDialog);

                showQRCode = false;

                if (outlet_type.equals("7E")) {

                    layout_sign_p_outlet_qrcode.setVisibility(View.GONE);
                    layout_sign_p_outlet_qrcode_error.setVisibility(View.VISIBLE);
                } else {

                    Toast.makeText(OutletPickupStep1Activity.this, "GetCollectionPickupNoList  Error..\n" + e.toString(), Toast.LENGTH_SHORT).show();
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

            DisplayUtil.dismissProgressDialog(progressDialog);

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