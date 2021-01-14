package com.giosis.library.main;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.giosis.library.R;


public class RpcListActivity extends FragmentActivity {
    public final static int FRAGMENT_PAGE1 = 0;
    String TAG = "RpcListActivity";
    //
    FrameLayout layout_top_back;
    TextView text_top_title;
    TextView text_rpc_list;
    ViewPager viewpager_rpc_list;
    private int NUM_PAGES = 1;        // 최대 페이지의 수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpc_list);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        text_rpc_list = findViewById(R.id.text_rpc_list);
        viewpager_rpc_list = findViewById(R.id.viewpager_rpc_list);


        //

        layout_top_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        text_top_title.setText(R.string.navi_list);


        text_rpc_list.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                viewpager_rpc_list.setCurrentItem(FRAGMENT_PAGE1);
            }
        });

        // ViewPager를 검색하고 Adapter를 달아주고, 첫 페이지를 선정해준다.
        viewpager_rpc_list.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        viewpager_rpc_list.setCurrentItem(FRAGMENT_PAGE1);

        viewpager_rpc_list.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                text_rpc_list.setSelected(false);

                switch (position) {
                    case 0:
                        text_rpc_list.setSelected(true);
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

        text_rpc_list.setSelected(true);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    // FragmentPageAdater : Fragment로써 각각의 페이지를 어떻게 보여줄지 정의한다.
    private class pagerAdapter extends FragmentStatePagerAdapter {

        public pagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // 특정 위치에 있는 Fragment를 반환해준다.
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                //    return new RpcListFragment();

                default:
                    return null;
            }
        }

        // 생성 가능한 페이지 개수를 반환해준다.
        @Override
        public int getCount() {
            return NUM_PAGES;
        }

    }
}
