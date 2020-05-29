package com.giosis.util.qdrive.list;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.giosis.util.qdrive.main.MainActivity;
import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.ServerResult;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/* 수정된 Main Activity */
public class ListActivity extends FragmentActivity implements OnClickListener, List_InProgressFragment.OnCountListener,
        List_UploadFailedFragment.OnFailedCountListener, List_TodayDoneFragment.OnTodayDoneCountListener, List_InProgressFragment.OnTodayDoneCountListener {
    String TAG = "ListActivity";
    Context context;

    // krm0219
    FrameLayout layout_top_back;
    TextView text_top_title;
    FrameLayout layout_top_smart_route;

    LinearLayout layout_list_in_progress;
    TextView text_list_in_progress_count;

    LinearLayout layout_list_upload_failed;
    TextView text_list_upload_failed_count;

    LinearLayout layout_list_today_done;
    TextView text_list_today_done_count;

    ViewPager viewpager_list;
    PagerAdapter pagerAdapter;

    List_InProgressFragment inProgressFragment;

    /* Fragment numbering */
    public final static int FRAGMENT_PAGE1 = 0;
    public final static int FRAGMENT_PAGE2 = 1;
    public final static int FRAGMENT_PAGE3 = 2;


    //
    SharedPreferences sharedPreferences;
    DatabaseHelper dbHelper;
    String opID;


    //In Progress 리스트 카운트
    @Override
    public void onCountRefresh(int count) {

        text_list_in_progress_count.setText(String.valueOf(count));
    }

    // Failed 리스트 카운트
    @Override
    public void onFailedCountRefresh(int count) {

        text_list_upload_failed_count.setText(String.valueOf(count));
    }

    @Override
    public void onTodayDoneCountRefresh(int count) {

        text_list_today_done_count.setText(String.valueOf(count));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        context = getApplicationContext();
        sharedPreferences = getSharedPreferences(DataUtil.SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        dbHelper = DatabaseHelper.getInstance();
        opID = SharedPreferencesHelper.getSigninOpID(context);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);
        layout_top_smart_route = findViewById(R.id.layout_top_smart_route);

        layout_top_back.setOnClickListener(this);
        text_top_title.setText(context.getResources().getString(R.string.navi_list));
        layout_top_smart_route.setOnClickListener(this);

        layout_list_in_progress = findViewById(R.id.layout_list_in_progress);
        text_list_in_progress_count = findViewById(R.id.text_list_in_progress_count);
        layout_list_in_progress.setOnClickListener(this);

        layout_list_upload_failed = findViewById(R.id.layout_list_upload_failed);
        text_list_upload_failed_count = findViewById(R.id.text_list_upload_failed_count);
        layout_list_upload_failed.setOnClickListener(this);

        layout_list_today_done = findViewById(R.id.layout_list_today_done);
        text_list_today_done_count = findViewById(R.id.text_list_today_done_count);
        layout_list_today_done.setOnClickListener(this);


        viewpager_list = findViewById(R.id.viewpager_list);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewpager_list.setAdapter(pagerAdapter);

        int position = getIntent().getIntExtra("position", 0);
        viewpager_list.setCurrentItem(position); //첫 페이지 설정 (홈에서 카운트 클릭으로 넘어옴)

        viewpager_list.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                layout_list_in_progress.setSelected(false);
                layout_list_upload_failed.setSelected(false);
                layout_list_today_done.setSelected(false);

                switch (position) {
                    case 0: {
                        layout_list_in_progress.setSelected(true);
                        layout_top_smart_route.setVisibility(View.VISIBLE);
                    }
                    break;
                    case 1: {
                        layout_list_upload_failed.setSelected(true);
                        layout_top_smart_route.setVisibility(View.GONE);
                    }
                    break;
                    case 2: {
                        layout_list_today_done.setSelected(true);
                        layout_top_smart_route.setVisibility(View.GONE);
                    }
                    break;
                    default:
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

        if (position == 1) {
            layout_list_upload_failed.setSelected(true);
            layout_top_smart_route.setVisibility(View.GONE);
        } else if (position == 2) {
            layout_list_today_done.setSelected(true);
            layout_top_smart_route.setVisibility(View.GONE);
        } else {
            layout_list_in_progress.setSelected(true);
            layout_top_smart_route.setVisibility(View.VISIBLE);
        }


        String createdSRDate = sharedPreferences.getString("createdSRDate", null);
        Log.e("krm0219", TAG + "  SmartRoute createdDate : " + createdSRDate);

        // 2019.07  -  SmartRoute 생성 후, 하루가 지나면 Route 재생성을 위해 초기화 (일반적으로 당일배송/당일픽업)
        if (createdSRDate != null) {

            try {

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                String strToday = dateFormat.format(date);

                Date today = dateFormat.parse(strToday);
                Date createdDate = dateFormat.parse(createdSRDate);

                Log.e("krm0219", TAG + "  SmartRoute date " + dateFormat.format(today) + " / " + dateFormat.format(createdDate));
                Log.e("krm0219", TAG + "  SmartRoute compare : " + today.compareTo(createdDate));
                if (today.compareTo(createdDate) > 0) {

                    // 'Smart Route' sort 되어 있을 때, 다음날이면 route 초기화가 되기 때문에 보여줄 게 없으므로
                    // 기본 zip_code sort로 전환
                    initSortIndex("compareDate");

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("createdSRCount", 0);
                    editor.putInt("clickedSRCount", 0);
                    editor.apply();
                }
            } catch (Exception e) {

                Log.e("Exception", TAG + "  Exception : " + e.toString());
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.layout_list_in_progress:
                viewpager_list.setCurrentItem(FRAGMENT_PAGE1);
                break;
            case R.id.layout_list_upload_failed:
                viewpager_list.setCurrentItem(FRAGMENT_PAGE2);
                break;
            case R.id.layout_list_today_done:
                viewpager_list.setCurrentItem(FRAGMENT_PAGE3);
                break;

            case R.id.layout_top_back: {

                DataUtil.inProgressListPosition = 0;
                DataUtil.uploadFailedListPosition = 0;
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
            break;

            case R.id.layout_top_smart_route: {

                Cursor cursor = dbHelper.get("SELECT * FROM " + DatabaseHelper.DB_TABLE_INTEGRATION_LIST + " WHERE punchOut_stat = 'N' and chg_dt is null and reg_id='" + opID + "'");

                Log.e("krm0219", "Smart Route Count > " + cursor.getCount());


                if (cursor.getCount() == 0) {

                    Toast.makeText(context, context.getResources().getString(R.string.msg_orders_not_found), Toast.LENGTH_SHORT).show();
                } else {

                    // 'smart route' 화면 보여주고 있을 때 버튼을 누르면  새롭게 Refresh 필요!!  (새로운 Smart Route 생성되기 때문에)
                    initSortIndex("makeSRBtn");

                    new MakeSmartRouteAsyncTask(context, opID, new MakeSmartRouteAsyncTask.AsyncTaskCallback() {

                        @Override
                        public void onSuccess(ServerResult result) {      // resultCode '0000'   성공

                            Toast.makeText(ListActivity.this, context.getResources().getString(R.string.msg_smart_route_creating), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(ServerResult result) {

                            String msg = context.getResources().getString(R.string.text_error) + "\n" + result.getResultMsg();
                            Toast.makeText(ListActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }).execute();
                }
            }
            break;
        }
    }


    private void initSortIndex(String called) {

        int selectedSort = MyApplication.preferences.getSortIndex();

        if (selectedSort == 6) {

            MyApplication.preferences.setSortIndex(0);
            if (called.equalsIgnoreCase("makeSRBtn")) {
                inProgressFragment.setSortSpinner();
            }
        }
    }


    // FragmentPageAdapter : Fragment로써 각각의 페이지를 어떻게 보여줄지 정의한다.
    private class PagerAdapter extends FragmentStatePagerAdapter {

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // 특정 위치에 있는 Fragment 반환해준다.
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0: {
                    inProgressFragment = new List_InProgressFragment();

                    return inProgressFragment;
                }
                case 1:
                    return new List_UploadFailedFragment();
                case 2:
                    return new List_TodayDoneFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 3;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {

                fragment.onActivityResult(requestCode, resultCode, data);
            }
        } catch (Exception e) {

            Log.e("Exception", TAG + "  onActivityResult Exception : " + e.toString());
        }
    }
}