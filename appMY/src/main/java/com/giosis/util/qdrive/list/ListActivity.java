package com.giosis.util.qdrive.list;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.main.MainActivity;
import com.giosis.util.qdrive.settings.SettingActivity;
import com.giosis.util.qdrive.util.DataUtil;
import com.giosis.util.qdrive.util.ui.CommonActivity;

/* 수정된 Main Activity */
public class ListActivity extends CommonActivity implements OnClickListener, InProgressListFragment.OnCountListener,
        UploadFailedListFragment.OnFailedCountListener, TodayDoneListFragment.OnTodayDoneCountListener, InProgressListFragment.OnTodayDoneCountListener {
    String TAG = "ListActivity";


    // krm0219
    FrameLayout layout_top_back;
    TextView text_top_title;

    LinearLayout layout_list_in_progress;
    TextView text_list_in_progress_count;

    LinearLayout layout_list_upload_failed;
    TextView text_list_upload_failed_count;

    LinearLayout layout_list_today_done;
    TextView text_list_today_done_count;

    ViewPager viewpager_list;

    /* Fragment numbering */
    public final static int FRAGMENT_PAGE1 = 0;
    public final static int FRAGMENT_PAGE2 = 1;
    public final static int FRAGMENT_PAGE3 = 2;


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
        super.onCreate(savedInstanceState);        //LAYOUT
        setContentView(R.layout.activity_list);
        Log.e("krm0219", TAG + "  onCreate");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        layout_top_back.setOnClickListener(this);
        text_top_title.setText(getResources().getString(R.string.navi_list));
        //    initBottomMenu();


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
        viewpager_list.setAdapter(new pagerAdapter(getSupportFragmentManager()));

        int position = getIntent().getIntExtra("position", 0);
        viewpager_list.setCurrentItem(position); //첫 페이지 설정 (홈에서 카운트 클릭으로 넘어옴)

        viewpager_list.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                layout_list_in_progress.setSelected(false);
                layout_list_upload_failed.setSelected(false);
                layout_list_today_done.setSelected(false);

                switch (position) {
                    case 0:
                        layout_list_in_progress.setSelected(true);
                        break;
                    case 1:
                        layout_list_upload_failed.setSelected(true);
                        break;
                    case 2:
                        layout_list_today_done.setSelected(true);
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
        } else if (position == 2) {
            layout_list_today_done.setSelected(true);
        } else {
            layout_list_in_progress.setSelected(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
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

                // finish();
                DataUtil.inProgressListPosition = 0;
                DataUtil.uploadFailedListPosition = 0;
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
            break;
        }
    }


    // FragmentPageAdater : Fragment로써 각각의 페이지를 어떻게 보여줄지 정의한다.
    private class pagerAdapter extends FragmentStatePagerAdapter {

        pagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // 특정 위치에 있는 Fragment를 반환해준다.
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new InProgressListFragment();
                case 1:
                    return new UploadFailedListFragment();
                case 2:
                    return new TodayDoneListFragment();
                default:
                    return null;
            }
        }

        // 생성 가능한 페이지 개수를 반환해준다.
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

        }
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(MyApplication.localeManager.setLocale(base));
    }

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
        btn_bottom_bar_scan.setBackgroundResource(R.drawable.tab_icon_scan_selector);
        btn_bottom_bar_list.setBackgroundResource(R.drawable.qdrive_tab_list_h);
        btn_bottom_bar_setting.setBackgroundResource(R.drawable.tab_icon_settings_selector);


        layout_bottom_bar_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DataUtil.inProgressListPosition = 0;
                DataUtil.uploadFailedListPosition = 0;
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        layout_bottom_bar_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ListActivity.this, ListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        layout_bottom_bar_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ListActivity.this, SettingActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}