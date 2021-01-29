package com.giosis.library.list;


import android.content.Intent;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.giosis.library.R;
import com.giosis.library.bluetooth.BluetoothClass;
import com.giosis.library.main.MainActivity;
import com.giosis.library.util.CommonActivity;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.DatabaseHelper;
import com.giosis.library.util.Preferences;
import com.giosis.library.util.ServerResult;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @editor krm0219
 * In progress  // Not Upload  //  Today Done
 */

public class ListActivity extends CommonActivity implements OnClickListener,
        ListInProgressFragment.OnInProgressFragmentListener, ListUploadFailedFragment.OnFailedCountListener,
        ListTodayDoneFragment.OnTodayDoneCountListener {

    String TAG = "ListActivity";

    FrameLayout layout_top_back;
    TextView text_top_title;

    LinearLayout layout_list_in_progress;
    TextView text_list_in_progress_count;

    LinearLayout layout_list_upload_failed;
    TextView text_list_upload_failed_count;

    LinearLayout layout_list_today_done;
    TextView text_list_today_done_count;

    ViewPager viewpager_list;
    PagerAdapter pagerAdapter;

    BluetoothClass bluetoothClass;

    /* Fragment numbering */
    public final static int FRAGMENT_PAGE1 = 0;
    public final static int FRAGMENT_PAGE2 = 1;
    public final static int FRAGMENT_PAGE3 = 2;

    @Override
    public void onCountRefresh(int count) {
        text_list_in_progress_count.setText(String.valueOf(count));
    }

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

        Log.e("Alarm", "ListActivity onCreate   " + Preferences.INSTANCE.getUserId());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_top_title.setText(getResources().getString(R.string.navi_list));
        layout_top_back.setOnClickListener(this);

        layout_list_in_progress = findViewById(R.id.layout_list_in_progress);
        text_list_in_progress_count = findViewById(R.id.text_list_in_progress_count);
        layout_list_upload_failed = findViewById(R.id.layout_list_upload_failed);
        text_list_upload_failed_count = findViewById(R.id.text_list_upload_failed_count);
        layout_list_today_done = findViewById(R.id.layout_list_today_done);
        text_list_today_done_count = findViewById(R.id.text_list_today_done_count);
        layout_list_in_progress.setOnClickListener(this);
        layout_list_upload_failed.setOnClickListener(this);
        layout_list_today_done.setOnClickListener(this);

        viewpager_list = findViewById(R.id.viewpager_list);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewpager_list.setAdapter(pagerAdapter);

        bluetoothClass = new BluetoothClass(this);

        int position = getIntent().getIntExtra("position", 0);
        viewpager_list.setCurrentItem(position);  //첫 페이지 설정 (홈에서 카운트 클릭으로 넘어옴)


        viewpager_list.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                layout_list_in_progress.setSelected(false);
                layout_list_upload_failed.setSelected(false);
                layout_list_today_done.setSelected(false);

                switch (position) {
                    case 0: {
                        layout_list_in_progress.setSelected(true);
                    }
                    break;
                    case 1: {
                        layout_list_upload_failed.setSelected(true);
                    }
                    break;
                    case 2: {
                        layout_list_today_done.setSelected(true);
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
        } else if (position == 2) {
            layout_list_today_done.setSelected(true);
        } else {
            layout_list_in_progress.setSelected(true);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Preferences.INSTANCE.getUserId().equals("")) {

            Toast.makeText(ListActivity.this, getResources().getString(R.string.msg_qdrive_auto_logout), Toast.LENGTH_SHORT).show();

            try {
                Intent intent;
                if ("SG".equals(Preferences.INSTANCE.getUserNation())) {
                    intent = new Intent(ListActivity.this, Class.forName("com.giosis.util.qdrive.singapore.LoginActivity"));
                } else {
                    intent = new Intent(ListActivity.this, Class.forName("com.giosis.util.qdrive.international.LoginActivity"));
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } catch (Exception e) {

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

        int id = v.getId();
        if (id == R.id.layout_list_in_progress) {
            viewpager_list.setCurrentItem(FRAGMENT_PAGE1);

        } else if (id == R.id.layout_list_upload_failed) {
            viewpager_list.setCurrentItem(FRAGMENT_PAGE2);

        } else if (id == R.id.layout_list_today_done) {
            viewpager_list.setCurrentItem(FRAGMENT_PAGE3);

        } else if (id == R.id.layout_top_back) {

            DataUtil.inProgressListPosition = 0;
            DataUtil.uploadFailedListPosition = 0;
            Intent intent = new Intent(ListActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

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
                    return new ListInProgressFragment(bluetoothClass);
                }
                case 1:
                    return new ListUploadFailedFragment();

                case 2:
                    return new ListTodayDoneFragment(bluetoothClass);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothClass.clearBluetoothAdapter();
    }
}