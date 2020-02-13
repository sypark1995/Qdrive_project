package com.giosis.util.qdrive.settings;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import java.lang.reflect.Method;

public class ModifyDeviceInfoActivity extends AppCompatActivity {
    String TAG = "ModifyUserInfoActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    TextView text_setting_printer_deviceName;
    EditText edit_setting_printer_device_rename;
    Button btn_setting_printer_device_rename_confirm;


    Context context;
    String opId;
    BluetoothDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_device_info);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_setting_printer_deviceName = findViewById(R.id.text_setting_printer_device_name);
        edit_setting_printer_device_rename = findViewById(R.id.edit_setting_printer_device_rename);
        btn_setting_printer_device_rename_confirm = findViewById(R.id.btn_setting_printer_device_rename_confirm);


        //-------------
        context = getApplicationContext();
        opId = SharedPreferencesHelper.getSigninOpID(getApplicationContext());

        String strDevice = getIntent().getStringExtra(BluetoothDeviceData.DEVICE_ID);
        device = BluetoothDeviceData.mBluetoothAdapter.getRemoteDevice(strDevice);

        String deviceName = device.getName();

        try {

            Method method = device.getClass().getMethod("getAliasName");
            if (method != null)
                deviceName = (String) method.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }


        text_top_title.setText(R.string.text_title_device_info);
        text_setting_printer_deviceName.setText(deviceName);
        edit_setting_printer_device_rename.setText(deviceName);


        //
        layout_top_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                setResult(RESULT_CANCELED);
                finish();
            }
        });

        btn_setting_printer_device_rename_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                modifyConfirmClick();
            }
        });
    }


    public void modifyConfirmClick() {

        String rename = edit_setting_printer_device_rename.getText().toString().trim();

        if (rename.equals("")) {
            Toast.makeText(context, context.getResources().getString(R.string.text_device_name_info), Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            Method method = device.getClass().getMethod("setAlias", String.class);
            if (method != null)
                method.invoke(device, rename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setResult(RESULT_CANCELED);
        finish();
    }
}