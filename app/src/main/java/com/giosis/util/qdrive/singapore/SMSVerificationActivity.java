package com.giosis.util.qdrive.singapore;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.giosis.util.qdrive.main.MainActivity;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.ui.CommonActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Pattern;

public class SMSVerificationActivity extends CommonActivity {
    private static final String TAG = "SMSVerificationActivity";

    // krm0219
    FrameLayout layout_top_back;
    TextView text_top_title;


    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.READ_PHONE_STATE};
    //

    EditText edit_verify_phone_number;
    Button btn_verify_request;

    EditText edit_verify_4_digit;
    EditText edit_verify_name;
    EditText edit_verify_email;
    Button btn_verify_submit;

    Context context;
    String op_id = "";
    String deviceID = "";

    String phone_no = "";
    String authCode = "";
    String name = "";
    String email = "";

    String focusItem = "";
    String mPhoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_verification);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        edit_verify_phone_number = findViewById(R.id.edit_verify_phone_number);
        btn_verify_request = findViewById(R.id.btn_verify_request);

        edit_verify_4_digit = findViewById(R.id.edit_verify_4_digit);
        edit_verify_name = findViewById(R.id.edit_verify_name);
        edit_verify_email = findViewById(R.id.edit_verify_email);
        btn_verify_submit = findViewById(R.id.btn_verify_submit);


        layout_top_back.setOnClickListener(clickListener);
        btn_verify_request.setOnClickListener(clickListener);
        btn_verify_submit.setOnClickListener(clickListener);

        //
        context = getApplicationContext();
//        deviceID = SharedPreferencesHelper.getSigninDeviceID(getApplicationContext());
//        op_id = SharedPreferencesHelper.getSigninOpID(getApplicationContext());
        deviceID = MyApplication.preferences.getDeviceUUID();
        op_id = MyApplication.preferences.getUserId();

        text_top_title.setText(R.string.text_title_sms_verification);

        edit_verify_phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (8 <= charSequence.length()) {

                    btn_verify_request.setBackgroundResource(R.drawable.back_rect_929292);
                } else {

                    btn_verify_request.setBackgroundResource(R.drawable.back_rect_e2e2e2);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        edit_verify_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (1 <= charSequence.length()) {

                    btn_verify_submit.setBackgroundResource(R.drawable.bg_round_20_4fb648);
                } else {

                    btn_verify_submit.setBackgroundResource(R.drawable.back_round_20_cccccc);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        //
        PermissionChecker checker = new PermissionChecker(this);

        // 권한 여부 체크 (없으면 true, 있으면 false)
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

        mPhoneNumber = "";

        if (isPermissionTrue) {
            try {

                mPhoneNumber = getMyPhoneNumber();
            } catch (Exception e) {

                mPhoneNumber = "";
            }
        }

        edit_verify_phone_number.setText(mPhoneNumber);
    }

    private String getMyPhoneNumber() {

        String temp_phone_no = "";

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return temp_phone_no;
        }

        TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (!mTelephonyMgr.getLine1Number().equals("")) {
            temp_phone_no = mTelephonyMgr.getLine1Number();
        }

        return temp_phone_no;
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.layout_top_back: {

                    finish();
                }
                break;

                case R.id.btn_verify_request: {

                    requestAuthNoClick();
                }
                break;

                case R.id.btn_verify_submit: {

                    submitAuthNoClick();
                }
                break;
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("Permission", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

                isPermissionTrue = true;
            }
        }
    }


    public void requestAuthNoClick() {

        phone_no = edit_verify_phone_number.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        boolean isvalid = true;

        if (phone_no.length() != 8) {

            if (phone_no.indexOf("+65") == 0 && phone_no.length() == 11) {
                String pattern = "\\+[0-9]+";
                boolean matches = Pattern.matches(pattern, phone_no);

                if (!matches) {

                    isvalid = false;
                    builder.setTitle("Invalidation alert");
                    builder.setMessage("please enter your right format number.");
                    builder.setCancelable(true);
                } else {

                    builder.setTitle("Verification");
                    builder.setMessage("Qdrive will be verifying the phone number(" + phone_no + "). Is this OK?");
                    builder.setCancelable(true);
                }
            } else {

                isvalid = false;
                builder.setTitle("Invalidation alert");
                builder.setMessage("please enter your right number.");
                builder.setCancelable(true);
            }
        } else {

            String pattern2 = "[0-9]+";
            boolean matches2 = Pattern.matches(pattern2, phone_no);

            if (matches2) {

                builder.setTitle("Verification");
                builder.setMessage("Qdrive will be verifying the phone number(" + phone_no + "). Is this OK?");
                builder.setCancelable(true);
            } else {

                isvalid = false;
                builder.setTitle("Invalidation alert");
                builder.setMessage("please enter your right format number.");
                builder.setCancelable(true);
            }
        }

        if (isvalid) {

            builder.setPositiveButton("OK", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GetAuthSMSRequestTask getAuthSMSRequestTask = new GetAuthSMSRequestTask();
                    getAuthSMSRequestTask.execute();
                    dialog.cancel();
                }
            });

            builder.setNeutralButton("CANCEL", new OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

        } else {

            builder.setPositiveButton("OK", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void submitAuthNoClick() {

        authCode = edit_verify_4_digit.getText().toString();
        name = edit_verify_name.getText().toString();
        email = edit_verify_email.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invalidation alert");

        if (authCode.length() == 4) {
            boolean isValidate = true;

            if (name.equals("")) {
                isValidate = false;

                String alertMsg = "";
                focusItem = "name";
                alertMsg += "name";

                builder.setMessage("please enter your " + alertMsg + ".");
                builder.setPositiveButton("OK", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (focusItem.equals("name")) {
                            edit_verify_name.requestFocus();
                        }

                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

            if (isValidate) {
                SetAuthCodeCheckTask setAuthCodeCheckTask = new SetAuthCodeCheckTask();
                setAuthCodeCheckTask.execute();
            }

        } else {

            builder.setMessage("please enter your right number.");
            builder.setPositiveButton("OK", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    edit_verify_4_digit.requestFocus();
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }


    class GetAuthSMSRequestTask extends AsyncTask<Void, Integer, HashMap<String, String>> {


        @Override
        protected HashMap<String, String> doInBackground(Void... params) {

            return requestSMSVerification(phone_no);
        }

        @Override
        protected void onPostExecute(HashMap<String, String> result) {

            String resultCode = result.get("ResultCode");
            String resultMsg = result.get("ResultMsg");

            if (!resultCode.equals("0")) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SMSVerificationActivity.this);
                builder.setTitle("alert");
                builder.setMessage("SMS request failed. " + resultMsg);
                builder.setPositiveButton("OK", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                try {

                    builder.show();
                } catch (Exception ignored) {
                }
            } else {

                edit_verify_4_digit.requestFocus();
            }
        }
    }


    private HashMap<String, String> requestSMSVerification(String phone_no) {

        HashMap<String, String> hashMap = new HashMap<>();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            hashMap.put("ResultCode", "-16");
            hashMap.put("ResultMsg", "\nPlease check your network connection. Saved at local device");
            return hashMap;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("mobile", phone_no);
            job.accumulate("deviceID", deviceID);
            job.accumulate("op_id", op_id);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "GetAuthCodeRequest";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
            // {"ResultCode":-1,"ResultMsg":"InvalidMobileNo"}
            // {"ResultCode":-5,"ResultMsg":"\r\nThis mobile number is already registered by another Qsign ID."}

            JSONObject jsonObject = new JSONObject(jsonString);
            hashMap.put("ResultCode", jsonObject.getString("ResultCode"));
            hashMap.put("ResultMsg", jsonObject.getString("ResultMsg"));
        } catch (Exception e) {

            hashMap.put("ResultCode", "-15");
            hashMap.put("ResultMsg", "\nException : " + e.toString());
        }

        return hashMap;
    }


    class SetAuthCodeCheckTask extends AsyncTask<Void, Integer, HashMap<String, String>> {

        @Override
        protected HashMap<String, String> doInBackground(Void... params) {

            return submitSMSVerification(phone_no);
        }

        @Override
        protected void onPostExecute(HashMap<String, String> result) {

            String resultCode = result.get("ResultCode");
            String resultMsg = result.get("ResultMsg");


            if (!resultCode.equals("0")) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SMSVerificationActivity.this);
                builder.setTitle("alert");
                builder.setMessage("SMS verification failed. " + resultMsg
                        + ".\nIf you doesn't get the verification, you can not use Qdrive App.");
                builder.setPositiveButton("OK", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                if (!SMSVerificationActivity.this.isFinishing()) {
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(SMSVerificationActivity.this);
                builder.setTitle("Success");
                builder.setMessage("SMS verification Success.");

                builder.setPositiveButton("OK", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                        Intent intent = new Intent();

//                        // 사용자 이름 바꾸기
//                        SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);
//                        Editor edit = settings.edit();
//                        edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_OP_NM, name);
//                        edit.apply();

                        MyApplication.preferences.setUserName(name);


//                        if (SharedPreferencesHelper.getSigninState(getApplicationContext())) {
                        intent.setClass(SMSVerificationActivity.this, MainActivity.class);
//                        } else {
//                            intent.setClass(SMSVerificationActivity.this, LoginActivity.class);
//                        }

                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                });

                if (!SMSVerificationActivity.this.isFinishing()) {
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        }
    }


    // submit 버튼 클릭시 실시간 웹서비스 호출
    private HashMap<String, String> submitSMSVerification(String phone_no) {

        HashMap<String, String> hashMap = new HashMap<>();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            hashMap.put("ResultCode", "-16");
            hashMap.put("ResultMsg", "Please check your network connection. Saved at local device");
            return hashMap;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("mobile", phone_no);
            job.accumulate("deviceID", deviceID);
            job.accumulate("authCode", authCode);
            job.accumulate("name", name);
            job.accumulate("email", email);
            job.accumulate("op_id", op_id);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "SetAuthCodeCheck";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
            // {"ResultCode":-10,"ResultMsg":"The number is not matched"}

            JSONObject jsonObject = new JSONObject(jsonString);
            hashMap.put("ResultCode", jsonObject.getString("ResultCode"));
            hashMap.put("ResultMsg", jsonObject.getString("ResultMsg"));
        } catch (Exception e) {

            hashMap.put("ResultCode", "-15");
            hashMap.put("ResultMsg", "Exception : " + e.toString());
        }

        return hashMap;
    }
}