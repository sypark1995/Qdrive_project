package com.giosis.library.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.giosis.library.R;
import com.giosis.library.barcodescanner.CaptureActivity;
import com.giosis.library.list.ListActivity;
import com.giosis.library.main.route.TodayMyRouteActivity;
import com.giosis.library.main.submenu.ListNotInHousedActivity;
import com.giosis.library.main.submenu.OutletOrderStatusActivity;
import com.giosis.library.main.submenu.StatisticsActivity;
import com.giosis.library.message.MessageListActivity;
import com.giosis.library.pickup.CreatePickupOrderActivity;
import com.giosis.library.setting.SettingActivity;
import com.giosis.library.util.BarcodeType;
import com.giosis.library.util.CommonActivity;
import com.giosis.library.util.Preferences;

import java.util.ArrayList;
import java.util.Arrays;


public class AppBaseActivity extends CommonActivity {
    String TAG = "AppBaseActivity";

    public DrawerLayout drawerLayout;
    Toolbar toolbar;
    TextView text_top_title;
    public RelativeLayout container;

    LinearLayout layout_bottom_bar_home;
    LinearLayout layout_bottom_bar_scan;
    LinearLayout layout_bottom_bar_list;
    LinearLayout layout_bottom_bar_setting;


    ExpandableListView nav_list;
    NavListViewAdapter adapter;
    TextView text_nav_header_driver_name;
    TextView text_nav_header_driver_office;

    String top_title_string;


    // Only SG
    RelativeLayout layout_message;
    ImageView btn_message;
    TextView text_message_count;

    int customerMessageCount;
    int adminMessageCount;
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int id = view.getId();
            if (id == R.id.btn_message) {

                Intent intent = new Intent(AppBaseActivity.this, MessageListActivity.class);
                intent.putExtra("customer_count", customerMessageCount);
                intent.putExtra("admin_count", adminMessageCount);
                startActivity(intent);

                drawerLayout.closeDrawers();
            } else if (id == R.id.layout_bottom_bar_home) {

                if (!(top_title_string.contains(getString(R.string.navi_home)))) {

                    Intent intent = new Intent(AppBaseActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else if (id == R.id.layout_bottom_bar_scan) {

                Intent intent = new Intent(AppBaseActivity.this, ScanActivity.class);
                startActivity(intent);
            } else if (id == R.id.layout_bottom_bar_list) {

                Intent intent = new Intent(AppBaseActivity.this, ListActivity.class);
                startActivity(intent);
            } else if (id == R.id.layout_bottom_bar_setting) {

                Intent intent = new Intent(AppBaseActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        }
    };

    public void setTopTitle(String title) {
        top_title_string = title;
        text_top_title.setText(top_title_string);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        text_top_title = findViewById(R.id.text_top_title);
        drawerLayout = findViewById(R.id.drawer_layout);
        container = findViewById(R.id.container);

        drawerLayout.setScrimColor(Color.parseColor("#4D000000"));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.button_open, R.string.button_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        layout_bottom_bar_home = findViewById(R.id.layout_bottom_bar_home);
        layout_bottom_bar_scan = findViewById(R.id.layout_bottom_bar_scan);
        layout_bottom_bar_list = findViewById(R.id.layout_bottom_bar_list);
        layout_bottom_bar_setting = findViewById(R.id.layout_bottom_bar_setting);

        layout_bottom_bar_home.setOnClickListener(clickListener);
        layout_bottom_bar_scan.setOnClickListener(clickListener);
        layout_bottom_bar_list.setOnClickListener(clickListener);
        layout_bottom_bar_setting.setOnClickListener(clickListener);


        adapter = new NavListViewAdapter(this);
        nav_list = findViewById(R.id.nav_list);

        View header = getLayoutInflater().inflate(R.layout.view_nav_list_header, null, false);
        text_nav_header_driver_name = header.findViewById(R.id.text_nav_header_driver_name);
        text_nav_header_driver_office = header.findViewById(R.id.text_nav_header_driver_office);

        nav_list.addHeaderView(header);
        nav_list.setAdapter(adapter);

        //
        layout_message = header.findViewById(R.id.layout_message);
        btn_message = header.findViewById(R.id.btn_message);
        text_message_count = header.findViewById(R.id.text_message_count);
        btn_message.setOnClickListener(clickListener);


        if (Preferences.INSTANCE.getUserNation().equalsIgnoreCase("SG")) {

            layout_message.setVisibility(View.VISIBLE);
        } else {

            layout_message.setVisibility(View.GONE);
        }


        // sub divider 칼라 없앰
        nav_list.setChildDivider(getResources().getDrawable(R.color.transparent));


        String pickup_driver_yn = Preferences.INSTANCE.getPickupDriver();
        String outletDriverYN;
        try {

            outletDriverYN = Preferences.INSTANCE.getOutletDriver();
        } catch (Exception e) {

            outletDriverYN = "N";
        }

        ArrayList<String> arrayList;
        ArrayList<String> arrayList1;

        if (outletDriverYN.equals("Y")) {

            arrayList = new ArrayList<>(Arrays.asList(getString(R.string.text_start_delivery_for_outlet), getString(R.string.navi_sub_delivery_done),
                    getString(R.string.navi_sub_pickup), getString(R.string.navi_sub_self)));
            arrayList1 = new ArrayList<>(Arrays.asList(getString(R.string.navi_sub_in_progress), getString(R.string.navi_sub_upload_fail), getString(R.string.navi_sub_today_done),
                    getString(R.string.navi_sub_not_in_housed), getString(R.string.text_outlet_order_status)));
        } else {

            arrayList = new ArrayList<>(Arrays.asList(getString(R.string.navi_sub_confirm_delivery), getString(R.string.navi_sub_delivery_done),
                    getString(R.string.navi_sub_pickup), getString(R.string.navi_sub_self)));
            arrayList1 = new ArrayList<>(Arrays.asList(getString(R.string.navi_sub_in_progress), getString(R.string.navi_sub_upload_fail), getString(R.string.navi_sub_today_done),
                    getString(R.string.navi_sub_not_in_housed)));
        }
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.side_icon_home_selector), getString(R.string.navi_home), null, -1);
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.side_icon_scan_selector), getString(R.string.navi_scan), arrayList, -1);
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.side_icon_list_selector), getString(R.string.navi_list), arrayList1, -1);
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.side_icon_statistics_selector), getString(R.string.navi_statistics), null, -1);
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.side_icon_my_route_selector), getString(R.string.text_today_my_route), null, -1);
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.side_icon_settings_selector), getString(R.string.navi_setting), null, -1);

        if (Preferences.INSTANCE.getUserNation().equalsIgnoreCase("SG") && pickup_driver_yn.equals("Y")) {
            adapter.addItem(ContextCompat.getDrawable(this, R.drawable.side_icon_create_pickup_order_selector), getString(R.string.text_create_pickup_order), null, 5);
        }




        nav_list.setOnGroupClickListener((expandableListView, view, position, l) -> {

            String title = adapter.getItem(position).getTitle();

            if (title.equals(getString(R.string.navi_home))) {

                drawerLayout.closeDrawers();
                if (!(top_title_string.contains(getString(R.string.navi_home)))) {

                    Intent intent = new Intent(AppBaseActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else if (title.equals(getString(R.string.navi_statistics))) {

                drawerLayout.closeDrawers();
                Intent intent = new Intent(AppBaseActivity.this, StatisticsActivity.class);
                startActivity(intent);

                if (!(top_title_string.contains(getString(R.string.navi_home)))) {
                    finish();
                }
            } else if (title.equals(getString(R.string.navi_setting))) {


                drawerLayout.closeDrawers();
                Intent intent = new Intent(AppBaseActivity.this, SettingActivity.class);
                // TEST.
                //Intent intent = new Intent(AppBaseActivity.this, SMSVerificationActivity.class);
                startActivity(intent);

                if (!(top_title_string.contains(getString(R.string.navi_home)))) {
                    finish();
                }
            } else if (title.equals(getString(R.string.text_create_pickup_order))) {

                drawerLayout.closeDrawers();
                Intent intent = new Intent(AppBaseActivity.this, CreatePickupOrderActivity.class);
                startActivity(intent);

                if (!(top_title_string.contains(getString(R.string.navi_home)))) {
                    finish();
                }
            } else if (title.equals(getString(R.string.text_today_my_route))) {

                drawerLayout.closeDrawers();
                // TODO_ROUTE
                Intent intent = new Intent(AppBaseActivity.this, TodayMyRouteActivity.class);
                startActivity(intent);

                if (!(top_title_string.contains(getString(R.string.navi_home)))) {
                    finish();
                }
            }

            return false;
        });


        nav_list.setOnGroupExpandListener(position -> {

            for (int i = 0; i < adapter.getGroupCount(); i++) {
                if (position != i) {
                    nav_list.collapseGroup(i);
                }
            }
        });


        nav_list.setOnChildClickListener((expandableListView, view, group_position, child_position, l) -> {

            // SCAN, LIST
            if (group_position == 1) {           // SCAN

                switch (child_position) {
                    case 0: {   // Confirm my delivery order  / Start Delivery for Outlet

//                        try {
//
//                            Intent intent = new Intent(AppBaseActivity.this, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivityTemp"));
//                            intent.putExtra("title", getResources().getString(R.string.text_title_driver_assign));
//                            intent.putExtra("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER);
//                            startActivity(intent);
//                        } catch (Exception e) {
//
//                            Log.e("Exception", "  Exception : " + e.toString());
//                            Toast.makeText(AppBaseActivity.this, "Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
//                        }

                        // FIXME_ New CaptureActivity
                        Intent intent = new Intent(AppBaseActivity.this, CaptureActivity.class);
                        intent.putExtra("title", getResources().getString(R.string.text_title_driver_assign));
                        intent.putExtra("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER);
                        startActivity(intent);
                    }
                    break;

                    case 1: {   // Delivery done

//                        try {
//
//                            Intent intent = new Intent(AppBaseActivity.this, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivityTemp"));
//                            intent.putExtra("title", getResources().getString(R.string.text_delivered));
//                            intent.putExtra("type", BarcodeType.DELIVERY_DONE);
//                            startActivity(intent);
//                        } catch (Exception e) {
//
//                            Log.e("Exception", "  Exception : " + e.toString());
//                            Toast.makeText(AppBaseActivity.this, "Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
//                        }

                        // FIXME_ New CaptureActivity
                        Intent intent = new Intent(AppBaseActivity.this, CaptureActivity.class);
                        intent.putExtra("title", getResources().getString(R.string.text_delivered));
                        intent.putExtra("type", BarcodeType.DELIVERY_DONE);
                        startActivity(intent);
                    }
                    break;

                    case 2: {   // Pickup C&R Parcels

//                        try {
//
//                            Intent intent = new Intent(AppBaseActivity.this, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivityTemp"));
//                            intent.putExtra("title", getResources().getString(R.string.text_title_scan_pickup_cnr));
//                            intent.putExtra("type", BarcodeType.PICKUP_CNR);
//                            startActivity(intent);
//                        } catch (Exception e) {
//
//                            Log.e("Exception", "  Exception : " + e.toString());
//                            Toast.makeText(AppBaseActivity.this, "Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
//                        }

                        // FIXME_ New CaptureActivity
                        Intent intent = new Intent(AppBaseActivity.this, CaptureActivity.class);
                        intent.putExtra("title", getResources().getString(R.string.text_title_scan_pickup_cnr));
                        intent.putExtra("type", BarcodeType.PICKUP_CNR);
                        startActivity(intent);
                    }
                    break;

                    case 3: {   // Self-collection

//                        try {
//
//                            Intent intent = new Intent(AppBaseActivity.this, Class.forName("com.giosis.util.qdrive.barcodescanner.CaptureActivityTemp"));
//                            intent.putExtra("title", getResources().getString(R.string.navi_sub_self));
//                            intent.putExtra("type", BarcodeType.SELF_COLLECTION);
//                            startActivity(intent);
//                        } catch (Exception e) {
//
//                            Log.e("Exception", "  Exception : " + e.toString());
//                            Toast.makeText(AppBaseActivity.this, "Exception : " + e.toString(), Toast.LENGTH_SHORT).show();
//                        }

                        // FIXME_ New CaptureActivity
                        Intent intent = new Intent(AppBaseActivity.this, CaptureActivity.class);
                        intent.putExtra("title", getResources().getString(R.string.navi_sub_self));
                        intent.putExtra("type", BarcodeType.SELF_COLLECTION);
                        startActivity(intent);
                    }
                    break;
                }
            } else if (group_position == 2) {           // LIST

                switch (child_position) {
                    case 0:
                    case 1:
                    case 2: {

                        Intent intent = new Intent(AppBaseActivity.this, ListActivity.class);
                        intent.putExtra("position", child_position);
                        startActivity(intent);
                    }
                    break;

                    case 3: {   // Not In-housed

                        Intent intent = new Intent(AppBaseActivity.this, ListNotInHousedActivity.class);
                        startActivity(intent);
                    }
                    break;

                    case 4: {       // Outlet - Outlet Order Status

                        Intent intent = new Intent(AppBaseActivity.this, OutletOrderStatusActivity.class);
                        startActivity(intent);
                    }
                    break;
                }
            }

            for (int i = 0; i < adapter.getGroupCount(); i++) {
                nav_list.collapseGroup(i);
            }

            drawerLayout.closeDrawers();
            return false;
        });
    }

    public void setMessageCount(int customer_count, int admin_count) {

        customerMessageCount = customer_count;
        adminMessageCount = admin_count;

        int count = customer_count + admin_count;
        text_message_count.setVisibility(View.VISIBLE);
        text_message_count.setText(Integer.toString(count));
    }

    public void goneMessageCount() {

        text_message_count.setVisibility(View.GONE);
    }

    public void setNaviHeader(String name, String office) {

        text_nav_header_driver_name.setText(name);
        text_nav_header_driver_office.setText(office);
    }
}