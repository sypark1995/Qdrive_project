package com.giosis.library.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.giosis.library.R
import com.giosis.library.bluetooth.BluetoothClass
import com.giosis.library.bluetooth.BluetoothListener
import com.giosis.library.main.MainActivity
import com.giosis.library.util.CommonActivity
import com.giosis.library.util.DataUtil
import com.giosis.library.util.ListFragmentFactoryImpl
import com.giosis.library.util.Preferences

class ListActivity2 : CommonActivity(), ListInProgressFragment2.OnInProgressFragmentListener,
    ListUploadFailedFragment2.OnFailedCountListener,
    ListTodayDoneFragment2.OnTodayDoneCountListener {

    var TAG = "ListActivity"
    private var listInProgressCount: TextView? = null

    private var listUploadFailedCount: TextView? = null

    private var listTodayDoneCount: TextView? = null

    var bluetoothClass: BluetoothClass? = null

    /* Fragment numbering */
    private val fragmentPage1 = 0
    private val fragmentPage2 = 1
    private val fragmentPage3 = 2


    override fun onCountRefresh(count: Int) {
        listInProgressCount!!.text = count.toString()
    }

    override fun onFailedCountRefresh(count: Int) {
        listUploadFailedCount!!.text = count.toString()
    }

    override fun onTodayDoneCountRefresh(count: Int) {
        listTodayDoneCount!!.text = count.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = ListFragmentFactoryImpl(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        Log.e("Alarm", "ListActivity onCreate   " + Preferences.userId)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val layoutTopBack = findViewById<FrameLayout>(R.id.layout_top_back)
        val textTopTitle = findViewById<TextView>(R.id.text_top_title)
        textTopTitle.text = resources.getString(R.string.navi_list)

        val layoutListInProgress = findViewById<LinearLayout>(R.id.layout_list_in_progress)
        listInProgressCount = findViewById(R.id.text_list_in_progress_count)
        val layoutListUploadFailed = findViewById<LinearLayout>(R.id.layout_list_upload_failed)
        listUploadFailedCount = findViewById(R.id.text_list_upload_failed_count)
        val layoutListTodayDone = findViewById<LinearLayout>(R.id.layout_list_today_done)
        listTodayDoneCount = findViewById(R.id.text_list_today_done_count)
        val viewpager2List = findViewById<ViewPager2>(R.id.viewpager2_list)

        layoutListInProgress.setOnClickListener {
            viewpager2List.currentItem = fragmentPage1
        }

        layoutListUploadFailed.setOnClickListener {
            viewpager2List.currentItem = fragmentPage2
        }

        layoutListTodayDone.setOnClickListener {
            viewpager2List.currentItem = fragmentPage3
        }

        layoutTopBack.setOnClickListener {
            DataUtil.inProgressListPosition = 0
            DataUtil.uploadFailedListPosition = 0
            val intent = Intent(this@ListActivity2, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        val pagerAdapter2 = PagerAdapter2(this)
        viewpager2List.adapter = pagerAdapter2

        bluetoothClass = BluetoothClass(this)

        viewpager2List.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                layoutListInProgress.isSelected = false
                layoutListUploadFailed.isSelected = false
                layoutListTodayDone.isSelected = false
                when (position) {
                    0 -> {
                        layoutListInProgress.isSelected = true
                    }
                    1 -> {
                        layoutListUploadFailed.isSelected = true
                    }
                    2 -> {
                        layoutListTodayDone.isSelected = true
                    }
                    else -> {}
                }
            }
        })

    }

    override fun onResume() {
        super.onResume()

        if (Preferences.userId == "") {
            Toast.makeText(
                this@ListActivity2,
                resources.getString(R.string.msg_qdrive_auto_logout),
                Toast.LENGTH_SHORT
            ).show()
            try {
                val intent: Intent = if ("SG" == Preferences.userNation) {
                    Intent(
                        this@ListActivity2,
                        Class.forName("com.giosis.util.qdrive.singapore.LoginActivity")
                    )
                } else {
                    Intent(
                        this@ListActivity2,
                        Class.forName("com.giosis.util.qdrive.international.LoginActivity")
                    )
                }
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            } catch (ignore: Exception) {
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    inner class PagerAdapter2(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    ListInProgressFragment2(bluetoothClass as BluetoothListener)
                }
                1 -> ListUploadFailedFragment2()
                2 -> ListTodayDoneFragment2(bluetoothClass as BluetoothListener)
                else -> throw IllegalArgumentException("unKnown")
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            for (fragment in supportFragmentManager.fragments) {
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        } catch (e: java.lang.Exception) {
            Log.e("Exception", "$TAG  onActivityResult Exception : $e")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothClass!!.clearBluetoothAdapter()
    }
}
