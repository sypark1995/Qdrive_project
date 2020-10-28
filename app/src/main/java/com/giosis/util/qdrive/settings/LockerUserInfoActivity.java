package com.giosis.util.qdrive.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DisplayUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LockerUserInfoActivity extends AppCompatActivity {
    String TAG = "LockerUserInfoActivity";

    Gson gson = new Gson();
    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_locker_user_user_key;
    TextView text_locker_user_status;
    TextView text_locker_user_mobile_no;
    TextView text_locker_user_expiry_pin_date;

    LinearLayout layout_locker_user_barcode;
    ImageView img_locker_user_barcode;
    TextView text_locker_user_user_key_1;
    TextView text_locker_user_barcode_error;
    Button btn_locker_user_go;


    Context context;
    String op_id;
    ProgressDialog progressDialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker_user_info);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_locker_user_user_key = findViewById(R.id.text_locker_user_user_key);
        text_locker_user_status = findViewById(R.id.text_locker_user_status);
        text_locker_user_mobile_no = findViewById(R.id.text_locker_user_mobile_no);
        text_locker_user_expiry_pin_date = findViewById(R.id.text_locker_user_expiry_pin_date);

        layout_locker_user_barcode = findViewById(R.id.layout_locker_user_barcode);
        img_locker_user_barcode = findViewById(R.id.img_locker_user_barcode);
        text_locker_user_user_key_1 = findViewById(R.id.text_locker_user_user_key_1);
        text_locker_user_barcode_error = findViewById(R.id.text_locker_user_barcode_error);
        btn_locker_user_go = findViewById(R.id.btn_locker_user_go);


        //
        context = getApplicationContext();
        op_id = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
        progressDialog = new ProgressDialog(LockerUserInfoActivity.this);

        text_top_title.setText(R.string.text_title_locker_user_info);

        layout_top_back.setOnClickListener(clickListener);
        text_locker_user_barcode_error.setOnClickListener(clickListener);
        btn_locker_user_go.setOnClickListener(clickListener);


        SpannableString content = new SpannableString(getResources().getString(R.string.text_error_retry));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        text_locker_user_barcode_error.setText(content);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (NetworkUtil.isNetworkAvailable(LockerUserInfoActivity.this)) {

            LockerUserInfoAsyncTask lockerUserInfoAsyncTask = new LockerUserInfoAsyncTask(op_id);
            lockerUserInfoAsyncTask.execute();
        } else {

            Toast.makeText(LockerUserInfoActivity.this, getString(R.string.wifi_connect_failed), Toast.LENGTH_SHORT).show();
        }
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.layout_top_back: {

                    finish();
                }
                break;

                case R.id.text_locker_user_barcode_error: {

                    op_id = SharedPreferencesHelper.getSigninOpID(getApplicationContext());

                    if (NetworkUtil.isNetworkAvailable(LockerUserInfoActivity.this)) {

                        LockerUserInfoAsyncTask lockerUserInfoAsyncTask = new LockerUserInfoAsyncTask(op_id);
                        lockerUserInfoAsyncTask.execute();
                    } else {

                        Toast.makeText(LockerUserInfoActivity.this, getString(R.string.wifi_connect_failed), Toast.LENGTH_SHORT).show();
                    }
                }
                break;

                case R.id.btn_locker_user_go: {

                    Uri webpage = Uri.parse(DataUtil.locker_pin_url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

                    if (intent.resolveActivity(getPackageManager()) != null) {

                        startActivity(intent);
                    }
                }
                break;
            }
        }
    };


    private class LockerUserInfoAsyncTask extends AsyncTask<Void, Void, LockerUserInfoResult> {

        String op_id;


        LockerUserInfoAsyncTask(String op_id) {

            this.op_id = op_id;

            // TEST
           // this.op_id = "7Eleven.Ajib";
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
        protected LockerUserInfoResult doInBackground(Void... params) {

            LockerUserInfoResult resultObj;


            try {

                JSONObject job = new JSONObject();
                job.accumulate("op_id", op_id);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);


                String methodName = "GetShuttleDriverForFederatedlockerInfo";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);

                resultObj = gson.fromJson(jsonString, LockerUserInfoResult.class);
            } catch (Exception e) {

                Log.e("Exception", TAG + "  GetShuttleDriverForFederatedlockerInfo Json Exception : " + e.toString());
                resultObj = null;
            }


            return resultObj;
        }

        @Override
        protected void onPostExecute(LockerUserInfoResult result) {

            try {
                if (result != null) {

                    if (result.getResult_code().equals("0")) {

                        LockerUserInfoResult.LockerResultObject.LockerResultRow resultRow = result.getResultObject().getResultRows().get(0);

                        DataUtil.copyClipBoard(context, resultRow.getUser_key());
                        text_locker_user_user_key.setText(resultRow.getUser_key());
                        text_locker_user_status.setText(resultRow.getUser_status());
                        text_locker_user_mobile_no.setText(resultRow.getUser_mobile());

                        try {

                            String result_date = resultRow.getUser_expiry_date();
                            DateFormat old_format = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss", Locale.KOREA);
                            DateFormat new_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                            Date old_date = old_format.parse(result_date);

                            text_locker_user_expiry_pin_date.setText(new_format.format(old_date));
                        } catch (ParseException e) {

                            Log.e("krm0219", "Error : " + e.toString());
                            text_locker_user_expiry_pin_date.setText(resultRow.getUser_expiry_date());
                        }

                        BarcodeAsyncTask barcodeAsyncTask = new BarcodeAsyncTask(resultRow);
                        barcodeAsyncTask.execute();
                    } else {

                        DisplayUtil.dismissProgressDialog(progressDialog);

                        Toast.makeText(LockerUserInfoActivity.this, getResources().getString(R.string.msg_download_locker_info_error)
                                + " - " + result.getResult_msg(), Toast.LENGTH_SHORT).show();
                        Log.e("krm0219", "LockerUserInfoAsyncTask  ResultCode : " + result.getResult_code() + " / " + result.getResult_msg());
                    }
                } else {

                    DisplayUtil.dismissProgressDialog(progressDialog);

                    Toast.makeText(LockerUserInfoActivity.this, getResources().getString(R.string.msg_download_locker_info_error)
                            + "\n " + getResources().getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show();
                    Log.e("krm0219", "LockerUserInfoAsyncTask  result null");
                }
            } catch (Exception e) {

                DisplayUtil.dismissProgressDialog(progressDialog);

                Toast.makeText(LockerUserInfoActivity.this, getResources().getString(R.string.msg_download_locker_info_error)
                        + "\n" + e.toString(), Toast.LENGTH_SHORT).show();
                Log.e("krm0219", TAG + "  LockerUserInfoAsyncTask Exception : " + e.toString());
            }
        }
    }

    public class BarcodeAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        LockerUserInfoResult.LockerResultObject.LockerResultRow result;
        String barcode_data;

        String imgUrl;

        public BarcodeAsyncTask(LockerUserInfoResult.LockerResultObject.LockerResultRow data) {

            this.result = data;
            this.barcode_data = data.getUser_key();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            try {

                imgUrl = DataUtil.barcode_url + barcode_data;
                Log.e("krm0219", TAG + " Barcode URL = " + imgUrl);

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

                layout_locker_user_barcode.setVisibility(View.VISIBLE);
                text_locker_user_barcode_error.setVisibility(View.GONE);

                text_locker_user_user_key_1.setText(result.getUser_key());

                Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, dpTopx(260), dpTopx(100), true);
                BitmapDrawable ob = new BitmapDrawable(getResources(), resizeBitmap);
                img_locker_user_barcode.setBackground(ob);
            } else {

                layout_locker_user_barcode.setVisibility(View.GONE);
                text_locker_user_barcode_error.setVisibility(View.VISIBLE);
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

    private int dpTopx(float dp) {

        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return pixel;
    }
}
