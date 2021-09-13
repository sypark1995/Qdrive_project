package com.giosis.library.main;

import android.app.AlertDialog;
import android.content.Context;
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

import com.giosis.library.R;
import com.giosis.library.server.Custom_JsonParser;
import com.giosis.library.util.CommonActivity;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.NetworkUtil;
import com.giosis.library.util.PermissionActivity;
import com.giosis.library.util.PermissionChecker;
import com.giosis.library.util.Preferences;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Pattern;

@Deprecated
public class SMSVerificationActivity1 extends CommonActivity {
    private static final String TAG = "SMSVerificationActivity";

    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.READ_PHONE_STATE};


    FrameLayout layout_top_back;
    TextView text_top_title;

    EditText edit_verify_phone_number;
    Button btn_verify_request;

    EditText edit_verify_4_digit;
    EditText edit_verify_name;
    EditText edit_verify_email;
    Button btn_verify_submit;

    String op_id = "";
    String deviceID = "";
    String nation;

    String phone_no = "";
    String authCode = "";
    String name = "";
    String email = "";

    String focusItem = "";
    String mPhoneNumber;


    View.OnClickListener clickListener = v -> {

        int id = v.getId();

        if (id == R.id.layout_top_back) {

            finish();
        } else if (id == R.id.btn_request) {

            requestAuthNoClick();
        } else if (id == R.id.btn_submit) {

            submitAuthNoClick();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_verification);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        edit_verify_phone_number = findViewById(R.id.edit_phone_number);
        btn_verify_request = findViewById(R.id.btn_request);

        edit_verify_4_digit = findViewById(R.id.edit_4_digit);
        edit_verify_name = findViewById(R.id.edit_name);
        edit_verify_email = findViewById(R.id.edit_email);
        btn_verify_submit = findViewById(R.id.btn_submit);


        layout_top_back.setOnClickListener(clickListener);
        btn_verify_request.setOnClickListener(clickListener);
        btn_verify_submit.setOnClickListener(clickListener);

        //
        op_id = Preferences.INSTANCE.getUserId();
        deviceID = Preferences.INSTANCE.getDeviceUUID();
        nation = Preferences.INSTANCE.getUserNation();
        Log.e("krm0219", " DATA > " + op_id + " / " + deviceID + " / " + nation);

        text_top_title.setText(R.string.text_title_sms_verification);

        edit_verify_phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (nation.equalsIgnoreCase("SG")) {

                    if (8 <= charSequence.length()) {

                        btn_verify_request.setBackgroundResource(R.drawable.bg_rect_929292);
                    } else {

                        btn_verify_request.setBackgroundResource(R.drawable.bg_rect_e2e2e2);
                    }
                } else {

                    if (10 <= charSequence.length()) {

                        btn_verify_request.setBackgroundResource(R.drawable.bg_rect_929292);
                    } else {

                        btn_verify_request.setBackgroundResource(R.drawable.bg_rect_e2e2e2);
                    }
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

                    btn_verify_submit.setBackgroundResource(R.drawable.bg_round_20_cccccc);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


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

    // SG, MY, ID 구분
    public void requestAuthNoClick() {

        phone_no = edit_verify_phone_number.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        boolean isvalid = true;

          /*
          MY 형식
          맨 앞자리 0을 포함한 10자리	// 018 367 4700
          맨 앞자리 0을 포함한 11자리 // 018 367X 4700

          ID 형식
          맨 앞자리 0을 포함한 11자리  // 0813 111 8569
          맨 앞자리 0을 포함한 12자리	 // 0813 1111 8569
          맨 앞자리 0을 포함한 13자리	 // 0813 11111 8569
          */

        if (nation.equalsIgnoreCase("SG")) {

            if (phone_no.length() != 8) {

                if (phone_no.indexOf("+65") == 0 && phone_no.length() == 11) {
                    String pattern = "\\+[0-9]+";
                    boolean matches = Pattern.matches(pattern, phone_no);

                    if (!matches) {

                        isvalid = false;
                        builder.setTitle(getResources().getString(R.string.text_invalidation_alert));
                        builder.setMessage(getResources().getString(R.string.msg_enter_right_format_number));
                        builder.setCancelable(true);
                    } else {

                        builder.setTitle(getResources().getString(R.string.text_verification));
                        builder.setMessage(getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + getResources().getString(R.string.msg_is_this_ok));
                        builder.setCancelable(true);
                    }
                } else {

                    isvalid = false;
                    builder.setTitle(getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(getResources().getString(R.string.msg_please_enter_right_number));
                    builder.setCancelable(true);
                }
            } else {

                String pattern2 = "[0-9]+";
                boolean matches2 = Pattern.matches(pattern2, phone_no);

                if (matches2) {

                    builder.setTitle(getResources().getString(R.string.text_verification));
                    builder.setMessage(getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + getResources().getString(R.string.msg_is_this_ok));
                    builder.setCancelable(true);
                } else {

                    isvalid = false;
                    builder.setTitle(getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(getResources().getString(R.string.msg_enter_right_format_number));
                    builder.setCancelable(true);
                }
            }
        } else if (nation.equals("MY")) {

            if (phone_no.length() == 10) {       // 018 367 4700

                Log.e("Verification", TAG + "  MY - length 10 " + phone_no);
                String pattern = "^0[0-9]*$";
                boolean matches = Pattern.matches(pattern, phone_no);

                if (matches) {

                    builder.setTitle(getResources().getString(R.string.text_verification));
                    builder.setMessage(getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + getResources().getString(R.string.msg_is_this_ok));
                    builder.setCancelable(true);
                } else {

                    isvalid = false;
                    builder.setTitle(getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(getResources().getString(R.string.msg_enter_right_format_number));
                    builder.setCancelable(true);
                }
            } else if (phone_no.length() == 11) {        // 018 367X 4700

                Log.e("Verification", TAG + "  MY - length 11  " + phone_no);
                String pattern = "^0[0-9]*$";
                boolean matches = Pattern.matches(pattern, phone_no);

                if (matches) {

                    builder.setTitle(getResources().getString(R.string.text_verification));
                    builder.setMessage(getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + getResources().getString(R.string.msg_is_this_ok));
                    builder.setCancelable(true);
                } else {

                    isvalid = false;
                    builder.setTitle(getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(getResources().getString(R.string.msg_enter_right_format_number));
                    builder.setCancelable(true);
                }
            } else {

                isvalid = false;
                builder.setTitle(getResources().getString(R.string.text_invalidation_alert));
                builder.setMessage(getResources().getString(R.string.msg_please_enter_right_number));
                builder.setCancelable(true);
            }
        } else if (nation.equals("ID")) {
            // NOTIFICATION.  ID 전화번호 형식

            if (phone_no.length() == 11) {       // 0813 111 8569

                Log.e("Verification", TAG + " ID length 11 " + phone_no);
                String pattern = "^0[0-9]*$";
                boolean matches = Pattern.matches(pattern, phone_no);

                if (matches) {

                    builder.setTitle(getResources().getString(R.string.text_verification));
                    builder.setMessage(getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + getResources().getString(R.string.msg_is_this_ok));
                    builder.setCancelable(true);
                } else {

                    isvalid = false;
                    builder.setTitle(getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(getResources().getString(R.string.msg_enter_right_format_number));
                    builder.setCancelable(true);
                }
            } else if (phone_no.length() == 12) {        // 0813 1111 8569

                Log.e("Verification", TAG + " ID  length 12  " + phone_no);

                String pattern = "^0[0-9]*$";
                boolean matches = Pattern.matches(pattern, phone_no);

                if (matches) {

                    builder.setTitle(getResources().getString(R.string.text_verification));
                    builder.setMessage(getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + getResources().getString(R.string.msg_is_this_ok));
                    builder.setCancelable(true);
                } else {

                    isvalid = false;
                    builder.setTitle(getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(getResources().getString(R.string.msg_enter_right_format_number));
                    builder.setCancelable(true);
                }
            } else if (phone_no.length() == 13) {        // 0813 11111 8569

                Log.e("Verification", TAG + " ID  length 13  " + phone_no);
                String pattern = "^0[0-9]*$";
                boolean matches = Pattern.matches(pattern, phone_no);

                if (matches) {

                    builder.setTitle(getResources().getString(R.string.text_verification));
                    builder.setMessage(getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + getResources().getString(R.string.msg_is_this_ok));
                    builder.setCancelable(true);
                } else {

                    isvalid = false;
                    builder.setTitle(getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(getResources().getString(R.string.msg_enter_right_format_number));
                    builder.setCancelable(true);
                }
            } else {

                isvalid = false;
                builder.setTitle(getResources().getString(R.string.text_invalidation_alert));
                builder.setMessage(getResources().getString(R.string.msg_please_enter_right_number));
                builder.setCancelable(true);
            }
        }


        if (isvalid) {

            builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog, which) -> {

                GetAuthSMSRequestTask getAuthSMSRequestTask = new GetAuthSMSRequestTask();
                getAuthSMSRequestTask.execute();
                dialog.cancel();
            });

            builder.setNeutralButton(getResources().getString(R.string.button_cancel), (dialog, id) -> dialog.cancel());

        } else {

            builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog, which) -> dialog.cancel());

        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    public void submitAuthNoClick() {

        authCode = edit_verify_4_digit.getText().toString();
        name = edit_verify_name.getText().toString();
        email = edit_verify_email.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.text_invalidation_alert));

        if (authCode.length() == 4) {
            boolean isValidate = true;

            if (name.equals("")) {

                isValidate = false;
                focusItem = "name";

                builder.setMessage(getResources().getString(R.string.msg_please_enter_name));
                builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog, which) -> {
                    if (focusItem.equals("name")) {
                        edit_verify_name.requestFocus();
                    }

                    dialog.cancel();
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

            if (isValidate) {

                SetAuthCodeCheckTask setAuthCodeCheckTask = new SetAuthCodeCheckTask();
                setAuthCodeCheckTask.execute();
            }

        } else {

            builder.setMessage(getResources().getString(R.string.msg_please_enter_right_number));
            builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog, which) -> {

                edit_verify_4_digit.requestFocus();
                dialog.cancel();
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private HashMap<String, String> requestSMSVerification(String phone_no) {

        HashMap<String, String> hashMap = new HashMap<>();

        if (!NetworkUtil.isNetworkAvailable(this)) {

            hashMap.put("ResultCode", "-16");
            hashMap.put("ResultMsg", getResources().getString(R.string.msg_network_connect_error));
            return hashMap;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("mobile", phone_no);
            job.accumulate("deviceID", deviceID);
            job.accumulate("op_id", op_id);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

            String methodName = "GetAuthCodeRequest";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
            // {"ResultCode":-1,"ResultMsg":"InvalidMobileNo"}
            // {"ResultCode":-5,"ResultMsg":"\r\nThis mobile number is already registered by another Qsign ID."}

            JSONObject jsonObject = new JSONObject(jsonString);
            hashMap.put("ResultCode", jsonObject.getString("ResultCode"));
            hashMap.put("ResultMsg", jsonObject.getString("ResultMsg"));
        } catch (Exception e) {

            String msg = String.format(getResources().getString(R.string.text_exception), e.toString());
            hashMap.put("ResultCode", "-15");
            hashMap.put("ResultMsg", msg);
        }

        return hashMap;
    }

    // submit 버튼 클릭시 실시간 웹서비스 호출
    private HashMap<String, String> submitSMSVerification(String phone_no) {

        HashMap<String, String> hashMap = new HashMap<>();

        if (!NetworkUtil.isNetworkAvailable(this)) {

            hashMap.put("ResultCode", "-16");
            hashMap.put("ResultMsg", getResources().getString(R.string.msg_network_connect_error));
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
            job.accumulate("nation_cd", Preferences.INSTANCE.getUserNation());

            String methodName = "SetAuthCodeCheck";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
            // {"ResultCode":-10,"ResultMsg":"The number is not matched"}

            JSONObject jsonObject = new JSONObject(jsonString);
            hashMap.put("ResultCode", jsonObject.getString("ResultCode"));
            hashMap.put("ResultMsg", jsonObject.getString("ResultMsg"));
        } catch (Exception e) {

            String msg = String.format(getResources().getString(R.string.text_exception), e.toString());
            hashMap.put("ResultCode", "-15");
            hashMap.put("ResultMsg", msg);
        }

        return hashMap;
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

                AlertDialog.Builder builder = new AlertDialog.Builder(SMSVerificationActivity1.this);
                builder.setTitle(getResources().getString(R.string.text_alert));
                builder.setMessage(getResources().getString(R.string.msg_sms_request_failed) + " " + resultMsg);
                builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog, which) -> dialog.cancel());

                try {

                    builder.show();
                } catch (Exception ignored) {
                }
            } else {

                edit_verify_4_digit.requestFocus();
            }
        }
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

                AlertDialog.Builder builder = new AlertDialog.Builder(SMSVerificationActivity1.this);
                builder.setTitle(getResources().getString(R.string.text_alert));
                builder.setMessage(getResources().getString(R.string.msg_sms_verification_failed) + " " + resultMsg
                        + "\n" + getResources().getString(R.string.msg_verification_not_use));
                builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog, which) -> dialog.cancel());

                if (!SMSVerificationActivity1.this.isFinishing()) {
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(SMSVerificationActivity1.this);
                builder.setTitle(getResources().getString(R.string.text_success));
                builder.setMessage(getResources().getString(R.string.msg_sms_verification_success));

                builder.setPositiveButton(getResources().getString(R.string.button_ok), (dialog, which) -> {

                    dialog.cancel();

                    Preferences.INSTANCE.setUserName(name);

                    Intent intent = new Intent(SMSVerificationActivity1.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);

                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                });

                if (!SMSVerificationActivity1.this.isFinishing()) {
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        }
    }
}