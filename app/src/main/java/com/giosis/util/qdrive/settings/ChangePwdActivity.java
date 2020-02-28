package com.giosis.util.qdrive.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.giosis.util.qdrive.barcodescanner.StdResult;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.giosis.util.qdrive.barcodescanner.ManualHelper.MOBILE_SERVER_URL;

public class ChangePwdActivity extends AppCompatActivity {
    String TAG = "ChangePwdActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    EditText edit_setting_change_old_password;
    EditText edit_setting_change_new_password;
    EditText edit_setting_change_confirm_password;

    Button btn_setting_change_confirm;


    Context context = null;
    String op_id = "";

    String old_pwd = "";
    String new_pwd = "";
    String confirm_pwd = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        edit_setting_change_old_password = findViewById(R.id.edit_setting_change_old_password);
        edit_setting_change_new_password = findViewById(R.id.edit_setting_change_new_password);
        edit_setting_change_confirm_password = findViewById(R.id.edit_setting_change_confirm_password);

        btn_setting_change_confirm = findViewById(R.id.btn_setting_change_confirm);


        //
        context = getApplicationContext();
        op_id = SharedPreferencesHelper.getSigninOpID(getApplicationContext());

        text_top_title.setText(R.string.text_title_change_password);

        //
        layout_top_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                finish();
            }
        });

        btn_setting_change_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chgPwdConfirmClick();
            }
        });
    }


    public void chgPwdConfirmClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.text_title_change_password));
        builder.setMessage(getResources().getString(R.string.msg_want_change_password));
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                old_pwd = edit_setting_change_old_password.getText().toString().trim();
                new_pwd = edit_setting_change_new_password.getText().toString().trim();
                confirm_pwd = edit_setting_change_confirm_password.getText().toString().trim();

                boolean isValid = isValidData();

                if (isValid) {

                    ChangePasswordTask changePasswordTask = new ChangePasswordTask();
                    changePasswordTask.execute();
                }

                dialog.cancel();
            }
        });

        builder.setNeutralButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean isValidData() {

        boolean isValid;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (!old_pwd.equals("")) {

            if (new_pwd.length() >= 11) {
//	    		Log.d(Tag, "hi ..new_pwd ."+new_pwd);
                String pwd_pattern = "((?=.*\\d)(?=.*[A-Za-z])(?=.*[!@#$%]).{11,20})";
                Pattern pattern = Pattern.compile(pwd_pattern);
                Matcher matcher = pattern.matcher(new_pwd);
                boolean isValidPwd = matcher.matches();
                if (isValidPwd) {

                    if (new_pwd.equals(confirm_pwd)) {
                        isValid = true;
//				    		Log.d(Tag, "new_pwd is the same as  confirm_pwd!!");

                    } else {
                        isValid = false;
                        builder.setTitle(getResources().getString(R.string.text_invalidation));
                        builder.setMessage(getResources().getString(R.string.msg_same_password_error));
                        builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                edit_setting_change_confirm_password.requestFocus();
                                dialog.cancel();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                } else {  // pwd 유효성에 맞지 않을 때
                    isValid = false;
                    builder.setTitle(getResources().getString(R.string.text_invalidation));
                    builder.setMessage(getResources().getString(R.string.msg_password_symbols_error));
                    builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            edit_setting_change_new_password.requestFocus();
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            } else { // 11자리
                isValid = false;
                builder.setTitle(getResources().getString(R.string.text_invalidation));
                builder.setMessage(getResources().getString(R.string.msg_password_length_error));
                builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        edit_setting_change_new_password.requestFocus();
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        } else {
            isValid = false;
            builder.setTitle(getResources().getString(R.string.text_invalidation));
            builder.setMessage(getResources().getString(R.string.msg_empty_password_error));
            builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    edit_setting_change_old_password.requestFocus();
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }


        return isValid;
    }


    class ChangePasswordTask extends AsyncTask<Void, Void, StdResult> {

        @Override
        protected StdResult doInBackground(Void... params) {

            return changePwdServerAPI(old_pwd, new_pwd, op_id);
        }

        @Override
        protected void onPostExecute(StdResult result) {

            int resultCode = result.getResultCode();
            String resultMsg = result.getResultMsg();

            if (resultCode != 0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ChangePwdActivity.this);
                builder.setTitle(getResources().getString(R.string.text_alert));
                builder.setMessage(resultMsg);
                builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(ChangePwdActivity.this);
                builder.setTitle(getResources().getString(R.string.text_alert));
                builder.setMessage(resultMsg);
                builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                        finish();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }

    public StdResult changePwdServerAPI(String old_pwd, String new_pwd, String op_id) {

        StdResult result = new StdResult();

        if (!NetworkUtil.isNetworkAvailable(context)) {

            result.setResultCode(-16);
            result.setResultMsg("Please check your network connection");
            return result;
        }


        try {

            JSONObject job = new JSONObject();
            job.accumulate("old_pwd", old_pwd);
            job.accumulate("new_pwd", new_pwd);
            job.accumulate("op_id", op_id);
            job.accumulate("app_id", DataUtil.appID);
            job.accumulate("nation_cd", DataUtil.nationCode);

            String methodName = "changePassword";
            String jsonString = Custom_JsonParser.requestServerDataReturnJSON(MOBILE_SERVER_URL, methodName, job);
            // {"ResultCode":0,"ResultMsg":"Change Password job is completed successfully"}
            // {"ResultCode":-31,"ResultMsg":"Change Password job is failed."}

            JSONObject jsonObject = new JSONObject(jsonString);
            result.setResultCode(jsonObject.getInt("ResultCode"));
            result.setResultMsg(jsonObject.getString("ResultMsg"));
        } catch (Exception e) {

            Log.e("Exception", TAG + "  changePassword Exception : " + e.toString());

            String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
            result.setResultCode(-15);
            result.setResultMsg(msg);
        }

        return result;
    }
}