package com.giosis.util.qdrive.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.Custom_JsonParser;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.NetworkUtil;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModifyUserInfoActivity extends AppCompatActivity {
    String TAG = "ModifyUserInfoActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_setting_change_id;
    EditText edit_setting_change_name;
    EditText edit_setting_change_email;

    Button btn_setting_change_confirm;


    Context context = null;
    String op_id = "";

    String opName = "";
    String opEmail = "";

    String name = "";
    String email = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_user_info);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_setting_change_id = findViewById(R.id.text_setting_change_id);
        edit_setting_change_name = findViewById(R.id.edit_setting_change_name);
        edit_setting_change_email = findViewById(R.id.edit_setting_change_email);

        btn_setting_change_confirm = findViewById(R.id.btn_setting_change_confirm);


        //
        context = getApplicationContext();
        op_id = MyApplication.preferences.getUserId();
        opName = MyApplication.preferences.getUserName();
        opEmail = MyApplication.preferences.getUserEmail();

        if (opEmail.equals("null")) {
            opEmail = "";
        }


        text_top_title.setText(R.string.text_title_my_info);

        text_setting_change_id.setText(op_id);
        edit_setting_change_name.setText(opName);
        edit_setting_change_email.setText(opEmail);

        edit_setting_change_name.setSelection(edit_setting_change_name.getText().length());
        edit_setting_change_email.setSelection(edit_setting_change_email.getText().length());

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

                modifyConfirmClick();
            }
        });
    }

    public void modifyConfirmClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.text_modify_my_info));
        builder.setMessage(getResources().getString(R.string.msg_want_change_info));
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                name = edit_setting_change_name.getText().toString().trim();
                email = edit_setting_change_email.getText().toString().trim();


                boolean isValid = isValidData();

                if (isValid) {

                    ChangeMyInfoAsyncTask changeMyInfoAsyncTask = new ChangeMyInfoAsyncTask();
                    changeMyInfoAsyncTask.execute();
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

        if (name.length() >= 6) {
            if (!email.equals("")) {  // email에 입력값이 있을 때만 유효성 검사

                String email_pattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
                Pattern pattern = Pattern.compile(email_pattern);
                Matcher matcher = pattern.matcher(email);
                boolean isEmail = matcher.matches();
                if (isEmail) {
                    isValid = true;
                } else {
                    isValid = false;
                    builder.setTitle(getResources().getString(R.string.text_invalidation));
                    builder.setMessage(getResources().getString(R.string.msg_email_format_error));
                    builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            edit_setting_change_email.requestFocus();
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            } else {
                isValid = true;
            }
        } else {
            isValid = false;
            builder.setTitle(getResources().getString(R.string.text_invalidation));
            builder.setMessage(getResources().getString(R.string.text_full_name_info));
            builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    edit_setting_change_name.requestFocus();
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        return isValid;
    }


    public class ChangeMyInfoAsyncTask extends AsyncTask<Void, Void, StdResult> {

        @Override
        protected StdResult doInBackground(Void... params) {

            return sendServerAPI(name, email);
        }

        @Override
        protected void onPostExecute(StdResult result) {

            int resultCode = result.getResultCode();
            String resultMsg = result.getResultMsg();

            if (resultCode != 0) {


                AlertDialog.Builder builder = new AlertDialog.Builder(ModifyUserInfoActivity.this);
                builder.setTitle(getResources().getString(R.string.text_alert));
                builder.setMessage(resultMsg);
                builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

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

                AlertDialog.Builder builder = new AlertDialog.Builder(ModifyUserInfoActivity.this);
                builder.setTitle(getResources().getString(R.string.text_alert));
                builder.setMessage(resultMsg);
                builder.setPositiveButton(getResources().getString(R.string.button_ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        updateSharedPreference();
                        finish();
                    }
                });

                try {

                    builder.show();
                } catch (Exception ignored) {
                }
            }
        }


        void updateSharedPreference() {

            MyApplication.preferences.setUserName(name);
            MyApplication.preferences.setUserEmail(email);


            SharedPreferences settings = context.getSharedPreferences(SharedPreferencesHelper.SHARED_PREF_FILE, Context.MODE_PRIVATE);

            Editor edit = settings.edit();
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_OP_NM, name);
            edit.putString(SharedPreferencesHelper.PREF_SIGN_IN_OP_EMAIL, email);
            edit.apply();
        }


        StdResult sendServerAPI(String name, String email) {

            StdResult result = new StdResult();

            if (!NetworkUtil.isNetworkAvailable(context)) {

                result.setResultCode(-16);
                result.setResultMsg(getResources().getString(R.string.msg_network_connect_error_saved));
                return result;
            }

            try {

                JSONObject job = new JSONObject();
                job.accumulate("name", name);
                job.accumulate("email", email);
                job.accumulate("op_id", op_id);
                job.accumulate("app_id", DataUtil.appID);
                job.accumulate("nation_cd", DataUtil.nationCode);

                String methodName = "changeMyInfo";
                String jsonString = Custom_JsonParser.requestServerDataReturnJSON(methodName, job);
                // {"ResultCode":0,"ResultMsg":"Modification job is completed successfully."}
                // {"ResultCode":-5,"ResultMsg":"This email is already registered by another Qsign ID."}

                JSONObject jsonObject = new JSONObject(jsonString);
                result.setResultCode(jsonObject.getInt("ResultCode"));
                result.setResultMsg(jsonObject.getString("ResultMsg"));
            } catch (Exception e) {

                Log.e("Exception", TAG + "  changeMyInfo Exception : " + e.toString());

                String msg = String.format(context.getResources().getString(R.string.text_exception), e.toString());
                result.setResultCode(-15);
                result.setResultMsg(msg);
            }

            return result;
        }
    }
}