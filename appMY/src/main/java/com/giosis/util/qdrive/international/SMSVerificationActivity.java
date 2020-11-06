package com.giosis.util.qdrive.international;

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

import androidx.core.app.ActivityCompat;

import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.main.MainActivity;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;
import com.giosis.util.qdrive.util.ui.CommonActivity;

import org.json.JSONObject;

import java.util.regex.Pattern;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;

public class SMSVerificationActivity extends CommonActivity {
    String TAG = "SMSVerificationActivity";

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
    String nation;

    String mPhoneNumber;
    String phone_no = "";
    String authCode = "";
    String name = "";
    String email = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_verification);


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
        op_id = MyApplication.preferences.getUserId();
        deviceID = MyApplication.preferences.getDeviceUUID();
        nation = MyApplication.preferences.getUserNation();
        Log.e("krm0219", " DATA > " + op_id + " / " + deviceID + " / " + nation);


        text_top_title.setText(R.string.text_title_sms_verification);

        edit_verify_phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (10 <= charSequence.length()) {

                    btn_verify_request.setBackgroundResource(R.drawable.bg_rect_929292);
                } else {

                    btn_verify_request.setBackgroundResource(R.drawable.bg_rect_e2e2e2);
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

                    btn_verify_submit.setBackgroundResource(R.drawable.bg_radius_20_4fb648);
                } else {

                    btn_verify_submit.setBackgroundResource(R.drawable.bg_radius_20_cccccc);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_REQUEST_CODE) {   // permission
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("krm0219", TAG + "   onActivityResult  PERMISSIONS_GRANTED");

                isPermissionTrue = true;
            }
        }
    }


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
        // NOTIFICATION.  MY 전화번호 형식
        if (nation.equals("MY")) {

            if (phone_no.length() == 10) {       // 018 367 4700

                Log.e("Verification", TAG + "  MY - length 10 " + phone_no);
                String pattern = "^0[0-9]*$";
                boolean matches = Pattern.matches(pattern, phone_no);

                if (matches) {

                    builder.setTitle(context.getResources().getString(R.string.text_verification));
                    builder.setMessage(context.getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + context.getResources().getString(R.string.msg_is_this_ok));
                    builder.setCancelable(true);
                } else {

                    isvalid = false;
                    builder.setTitle(context.getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(context.getResources().getString(R.string.msg_enter_right_format_number));
                    builder.setCancelable(true);
                }
            } else if (phone_no.length() == 11) {        // 018 367X 4700

                Log.e("Verification", TAG + "  MY - length 11  " + phone_no);
                String pattern = "^0[0-9]*$";
                boolean matches = Pattern.matches(pattern, phone_no);

                if (matches) {

                    builder.setTitle(context.getResources().getString(R.string.text_verification));
                    builder.setMessage(context.getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + context.getResources().getString(R.string.msg_is_this_ok));
                    builder.setCancelable(true);
                } else {

                    isvalid = false;
                    builder.setTitle(context.getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(context.getResources().getString(R.string.msg_enter_right_format_number));
                    builder.setCancelable(true);
                }
            } else {

                isvalid = false;
                builder.setTitle(context.getResources().getString(R.string.text_invalidation_alert));
                builder.setMessage(context.getResources().getString(R.string.msg_please_enter_right_number));
                builder.setCancelable(true);
            }
        } else if (nation.equals("ID")) {
            // NOTIFICATION.  ID 전화번호 형식

            if (phone_no.length() == 11) {       // 0813 111 8569

                Log.e("Verification", TAG + " ID length 11 " + phone_no);
                String pattern = "^0[0-9]*$";
                boolean matches = Pattern.matches(pattern, phone_no);

                if (matches) {

                    builder.setTitle(context.getResources().getString(R.string.text_verification));
                    builder.setMessage(context.getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + context.getResources().getString(R.string.msg_is_this_ok));
                    builder.setCancelable(true);
                } else {

                    isvalid = false;
                    builder.setTitle(context.getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(context.getResources().getString(R.string.msg_enter_right_format_number));
                    builder.setCancelable(true);
                }
            } else if (phone_no.length() == 12) {        // 0813 1111 8569

                Log.e("Verification", TAG + " ID  length 12  " + phone_no);

                String pattern = "^0[0-9]*$";
                boolean matches = Pattern.matches(pattern, phone_no);

                if (matches) {

                    builder.setTitle(context.getResources().getString(R.string.text_verification));
                    builder.setMessage(context.getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + context.getResources().getString(R.string.msg_is_this_ok));
                    builder.setCancelable(true);
                } else {

                    isvalid = false;
                    builder.setTitle(context.getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(context.getResources().getString(R.string.msg_enter_right_format_number));
                    builder.setCancelable(true);
                }
            } else if (phone_no.length() == 13) {        // 0813 11111 8569

                Log.e("Verification", TAG + " ID  length 13  " + phone_no);
                String pattern = "^0[0-9]*$";
                boolean matches = Pattern.matches(pattern, phone_no);

                if (matches) {

                    builder.setTitle(context.getResources().getString(R.string.text_verification));
                    builder.setMessage(context.getResources().getString(R.string.msg_verify_phone_number) + " (" + phone_no + "). " + context.getResources().getString(R.string.msg_is_this_ok));
                    builder.setCancelable(true);
                } else {

                    isvalid = false;
                    builder.setTitle(context.getResources().getString(R.string.text_invalidation_alert));
                    builder.setMessage(context.getResources().getString(R.string.msg_enter_right_format_number));
                    builder.setCancelable(true);
                }
            } else {

                isvalid = false;
                builder.setTitle(context.getResources().getString(R.string.text_invalidation_alert));
                builder.setMessage(context.getResources().getString(R.string.msg_please_enter_right_number));
                builder.setCancelable(true);
            }
        }

        if (isvalid) {

            builder.setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GetAuthSMSRequestTask getAuthSMSRequestTask = new GetAuthSMSRequestTask();
                    getAuthSMSRequestTask.execute();
                    dialog.cancel();
                }
            });

            builder.setNeutralButton(context.getResources().getString(R.string.button_cancel), new OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

        } else {

            builder.setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

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
        builder.setTitle(context.getResources().getString(R.string.text_invalidation_alert));

        if (authCode.length() == 4) {
            Boolean isValidate = true;

            if (name.equals("")) {
                isValidate = false;

                builder.setMessage(context.getResources().getString(R.string.msg_please_enter_name));
                builder.setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        edit_verify_name.requestFocus();
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

            builder.setMessage(context.getResources().getString(R.string.msg_please_enter_right_number));
            builder.setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    class GetAuthSMSRequestTask extends AsyncTask<Void, Integer, StdResult> {


        @Override
        protected StdResult doInBackground(Void... params) {

            return requestSMSVerification(phone_no);
        }

        @Override
        protected void onPostExecute(StdResult result) {

            int resultCode = result.getResultCode();
            String resultMsg = result.getResultMsg();

            if (resultCode != 0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SMSVerificationActivity.this);
                builder.setTitle(context.getResources().getString(R.string.text_alert));
                builder.setMessage(context.getResources().getString(R.string.msg_sms_request_failed) + " " + resultMsg);
                builder.setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {

                edit_verify_4_digit.requestFocus();
            }
        }
    }


    private StdResult requestSMSVerification(String phone_no) {

        StdResult result = new StdResult();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            result.setResultCode(-16);
            result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
            return result;
        }

        try {

            JSONObject job = new JSONObject();
            job.accumulate("mobile", phone_no);
            job.accumulate("deviceID", deviceID);
            job.accumulate("op_id", op_id);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);


            String methodName = "GetAuthCodeRequest";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            // {"ResultCode":-1,"ResultMsg":"InvalidMobileNo"}
            // {"ResultCode":-5,"ResultMsg":"\r\nThis mobile number is already registered by another Qsign ID."}
            // {"ResultCode":0,"ResultMsg":"{\"umid\":\"6a3628ec-64bd-e911-8153-022a22cc1c71\",\"clientMessageId\":null,\"destination\":\"621083357170\",\"encoding\":\"GSM7\",\"status\":{\"code\":\"QUEUED\",\"description\":\"SMS is accepted and queued for processing\"}}"}

            JSONObject jsonObject = new JSONObject(jsonString);
            result.setResultCode(jsonObject.getInt("ResultCode"));
            result.setResultMsg(jsonObject.getString("ResultMsg"));
        } catch (Exception e) {

            Log.e("Exception", TAG + "  GetAuthCodeRequest Exception : " + e.toString());
            String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            result.setResultCode(-15);
            result.setResultMsg(msg);
        }

        return result;
    }


    class SetAuthCodeCheckTask extends AsyncTask<Void, Integer, StdResult> {

        @Override
        protected StdResult doInBackground(Void... params) {

            return submitSMSVerification(phone_no);
        }

        @Override
        protected void onPostExecute(StdResult result) {

            int resultCode = result.getResultCode();
            String resultMsg = result.getResultMsg();

            if (resultCode != 0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SMSVerificationActivity.this);
                builder.setTitle(context.getResources().getString(R.string.text_alert));
                builder.setMessage(context.getResources().getString(R.string.msg_sms_verification_failed) + "  " + resultMsg
                        + "\n" + context.getResources().getString(R.string.msg_verification_not_use));
                builder.setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(SMSVerificationActivity.this);
                builder.setTitle(context.getResources().getString(R.string.text_success));
                builder.setMessage(context.getResources().getString(R.string.msg_sms_verification_success));

                builder.setPositiveButton(context.getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                        MyApplication.preferences.setUserName(name);

                        Intent intent = new Intent();
                        intent.setClass(SMSVerificationActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }

    }


    private StdResult submitSMSVerification(String phone_no) {

        StdResult result = new StdResult();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            result.setResultCode(-16);
            result.setResultMsg(context.getResources().getString(R.string.msg_network_connect_error_saved));
            return result;
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
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            // {"ResultCode":-10,"ResultMsg":"The number is not matched"}

            JSONObject jsonObject = new JSONObject(jsonString);
            result.setResultCode(jsonObject.getInt("ResultCode"));
            result.setResultMsg(jsonObject.getString("ResultMsg"));
        } catch (Exception e) {

            Log.e("Exception", TAG + "  SetAuthCodeCheck Exception : " + e.toString());
            String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            result.setResultCode(-15);
            result.setResultMsg(msg);
        }

        return result;
    }
}