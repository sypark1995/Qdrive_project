package com.giosis.util.qdrive.message;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.ui.CommonActivity;

/**
 * @author krm0219
 */

public class MessageListActivity extends CommonActivity {
    String TAG = "MessageListActivity";

    public final static int FRAGMENT_PAGE1 = 0;
    public final static int FRAGMENT_PAGE2 = 1;


    FrameLayout layout_top_back;
    TextView text_top_title;

    RelativeLayout layout_message_list_customer;
    TextView text_message_list_customer;
    ImageView img_message_list_customer_new;
    RelativeLayout layout_message_list_admin;
    TextView text_message_list_admin;
    ImageView img_message_list_admin_new;

    ViewPager viewpager_message_list;

    //
    CustomerMessageListFragment customerMessageListFragment;
    AdminMessageListFragment adminMessageListFragment;

    MessageListActivity.pagerAdapter pagerAdapter;
    int viewpager_position = 0;

    int customerMessageCount = 0;
    int adminMessageCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        layout_message_list_customer = findViewById(R.id.layout_message_list_customer);
        text_message_list_customer = findViewById(R.id.text_message_list_customer);
        img_message_list_customer_new = findViewById(R.id.img_message_list_customer_new);
        layout_message_list_admin = findViewById(R.id.layout_message_list_admin);
        text_message_list_admin = findViewById(R.id.text_message_list_admin);
        img_message_list_admin_new = findViewById(R.id.img_message_list_admin_new);

        viewpager_message_list = findViewById(R.id.viewpager_message_list);

        //
        layout_top_back.setOnClickListener(clickListener);
        layout_message_list_customer.setOnClickListener(clickListener);
        layout_message_list_admin.setOnClickListener(clickListener);

        pagerAdapter = new pagerAdapter(getSupportFragmentManager());
        viewpager_message_list.setAdapter(pagerAdapter);

        viewpager_position = getIntent().getIntExtra("position", FRAGMENT_PAGE1);
        customerMessageCount = getIntent().getIntExtra("customer_count", 0);
        adminMessageCount = getIntent().getIntExtra("admin_count", 0);

        text_top_title.setText(R.string.text_title_message);
        setCustomerNewImage(customerMessageCount);
        setAdminNewImage(adminMessageCount);

        viewpager_message_list.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                layout_message_list_customer.setSelected(false);
                layout_message_list_admin.setSelected(false);

                switch (position) {
                    case 0: {

                        viewpager_position = 0;
                        layout_message_list_customer.setSelected(true);
                    }
                    break;
                    case 1: {

                        viewpager_position = 1;
                        layout_message_list_admin.setSelected(true);
                    }
                    break;
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });


        if (viewpager_position == 0) {

            layout_message_list_customer.setSelected(true);
        } else {

            layout_message_list_admin.setSelected(true);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        DataUtil.setMessageListActivity(this);
        viewpager_message_list.setCurrentItem(viewpager_position);
    }

    OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.layout_top_back: {

                    finish();
                }
                break;

                case R.id.layout_message_list_customer: {

                    viewpager_message_list.setCurrentItem(FRAGMENT_PAGE1);
                }
                break;

                case R.id.layout_message_list_admin: {

                    viewpager_message_list.setCurrentItem(FRAGMENT_PAGE2);
                }
                break;
            }
        }
    };


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void setCustomerNewImage(int customer_count) {
        Log.e("krm0219", TAG + "  setCustomerNewImage : " + customer_count);

        if (customer_count != 0) {

            img_message_list_customer_new.setVisibility(View.VISIBLE);
        } else {

            img_message_list_customer_new.setVisibility(View.GONE);
        }
    }

    public void setAdminNewImage(int admin_count) {
        Log.e("krm0219", TAG + "  setAdminNewImage : " + admin_count);

        if (admin_count != 0) {

            img_message_list_admin_new.setVisibility(View.VISIBLE);
        } else {

            img_message_list_admin_new.setVisibility(View.GONE);
        }
    }

    private class pagerAdapter extends FragmentStatePagerAdapter {

        public pagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0: {

                    customerMessageListFragment = new CustomerMessageListFragment();
                    return customerMessageListFragment;
                }
                case 1: {

                    adminMessageListFragment = new AdminMessageListFragment();
                    return adminMessageListFragment;
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return super.getItemPosition(object);
        }
    }

    public void refreshCustomerData() {

        Log.e("krm0219", TAG + "  refreshCustomerData");
        customerMessageListFragment.refreshData();
    }

    public void refreshAdminData() {

        Log.e("krm0219", TAG + "  refreshAdminData");
        adminMessageListFragment.refreshData();
    }
}