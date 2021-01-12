package com.giosis.util.qdrive.international;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.barcodescanner.CaptureActivity;
import com.giosis.util.qdrive.list.ListActivity;
import com.giosis.library.setting.SettingActivity;
import com.giosis.util.qdrive.util.BarcodeType;
import com.giosis.util.qdrive.util.ui.CommonActivity;

/**
 * @author krm0219
 **/
public class ScanActivity extends CommonActivity {
    String TAG = "ScanActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    RelativeLayout layout_scan_confirm_my_delivery_order;
    TextView text_scan_confirm_my_delivery_order;
    RelativeLayout layout_scan_delivery_done;
    TextView text_scan_delivery_scan_msg;
    RelativeLayout layout_scan_pickup_cnr;
    LinearLayout layout_scan_self_collection_shown;
    RelativeLayout layout_scan_self_collection;


    Context context;
    String officeName;
    String outletDriverYN = "N";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        layout_scan_confirm_my_delivery_order = findViewById(R.id.layout_scan_confirm_my_delivery_order);
        text_scan_confirm_my_delivery_order = findViewById(R.id.text_scan_confirm_my_delivery_order);
        layout_scan_delivery_done = findViewById(R.id.layout_scan_delivery_done);
        text_scan_delivery_scan_msg = findViewById(R.id.text_scan_delivery_scan_msg);
        layout_scan_pickup_cnr = findViewById(R.id.layout_scan_pickup_cnr);
        layout_scan_self_collection_shown = findViewById(R.id.layout_scan_self_collection_shown);
        layout_scan_self_collection = findViewById(R.id.layout_scan_self_collection);

        //   initBottomMenu();

        //
        text_top_title.setText(R.string.text_title_delivery_scan);

        context = getApplicationContext();
        officeName = MyApplication.preferences.getOfficeName();

        // krm0219 Outlet
        try {

            outletDriverYN = MyApplication.preferences.getOutletDriver();
        } catch (Exception e) {

            outletDriverYN = "N";
        }

        if (outletDriverYN.equals("Y")) {

            String msg = String.format(context.getResources().getString(R.string.msg_delivery_scan2), context.getResources().getString(R.string.text_start_delivery_for_outlet));
            text_scan_confirm_my_delivery_order.setText(R.string.text_start_delivery_for_outlet);
            text_scan_delivery_scan_msg.setText(msg);
        } else {

            String msg = String.format(context.getResources().getString(R.string.msg_delivery_scan2), context.getResources().getString(R.string.button_confirm_my_delivery_order));
            text_scan_confirm_my_delivery_order.setText(R.string.button_confirm_my_delivery_order);
            text_scan_delivery_scan_msg.setText(msg);
        }


        if (officeName.contains("Qxpress SG") || officeName.contains("Qxpress MY")) {

            layout_scan_self_collection_shown.setVisibility(View.VISIBLE);
        } else {

            layout_scan_self_collection_shown.setVisibility(View.GONE);
        }


        layout_top_back.setOnClickListener(clickListener);
        layout_scan_confirm_my_delivery_order.setOnClickListener(clickListener);
        layout_scan_delivery_done.setOnClickListener(clickListener);
        layout_scan_pickup_cnr.setOnClickListener(clickListener);
        layout_scan_self_collection.setOnClickListener(clickListener);
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.layout_top_back: {

                    finish();
                }
                break;

                case R.id.layout_scan_confirm_my_delivery_order: {

                    Intent intent = new Intent(ScanActivity.this, CaptureActivity.class);
                    intent.putExtra("title", context.getResources().getString(R.string.text_title_driver_assign));
                    intent.putExtra("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER);
                    startActivity(intent);
                }
                break;

                case R.id.layout_scan_delivery_done: {

                    Intent intent = new Intent(ScanActivity.this, CaptureActivity.class);
                    intent.putExtra("title", context.getResources().getString(R.string.text_delivered));
                    intent.putExtra("type", BarcodeType.DELIVERY_DONE);
                    startActivity(intent);
                }
                break;

                case R.id.layout_scan_pickup_cnr: {

                    Intent intent = new Intent(ScanActivity.this, CaptureActivity.class);
                    intent.putExtra("title", context.getResources().getString(R.string.text_title_scan_pickup_cnr));
                    intent.putExtra("type", BarcodeType.PICKUP_CNR);
                    startActivity(intent);
                }
                break;

                case R.id.layout_scan_self_collection: {

                    Intent intent = new Intent(ScanActivity.this, CaptureActivity.class);
                    intent.putExtra("title", context.getResources().getString(R.string.navi_sub_self));
                    intent.putExtra("type", BarcodeType.SELF_COLLECTION);
                    startActivity(intent);
                }
                break;
            }
        }
    };


    void initBottomMenu() {

        LinearLayout layout_bottom_bar_home = findViewById(R.id.layout_bottom_bar_home);
        ImageView btn_bottom_bar_home = findViewById(R.id.btn_bottom_bar_home);
        LinearLayout layout_bottom_bar_scan = findViewById(R.id.layout_bottom_bar_scan);
        ImageView btn_bottom_bar_scan = findViewById(R.id.btn_bottom_bar_scan);
        LinearLayout layout_bottom_bar_list = findViewById(R.id.layout_bottom_bar_list);
        ImageView btn_bottom_bar_list = findViewById(R.id.btn_bottom_bar_list);
        LinearLayout layout_bottom_bar_setting = findViewById(R.id.layout_bottom_bar_setting);
        ImageView btn_bottom_bar_setting = findViewById(R.id.btn_bottom_bar_setting);


        btn_bottom_bar_home.setBackgroundResource(R.drawable.tab_icon_home_selector);
        btn_bottom_bar_scan.setBackgroundResource(R.drawable.qdrive_tab_scan_h);
        btn_bottom_bar_list.setBackgroundResource(R.drawable.tab_icon_list_selector);
        btn_bottom_bar_setting.setBackgroundResource(R.drawable.tab_icon_settings_selector);


        layout_bottom_bar_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        layout_bottom_bar_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ScanActivity.this, ListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        layout_bottom_bar_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ScanActivity.this, SettingActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}