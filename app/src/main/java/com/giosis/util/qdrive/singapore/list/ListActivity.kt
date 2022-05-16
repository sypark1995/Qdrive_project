package com.giosis.util.qdrive.singapore.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.bluetooth.BluetoothClass
import com.giosis.util.qdrive.singapore.bluetooth.BluetoothListener
import com.giosis.util.qdrive.singapore.main.MainActivity
import com.giosis.util.qdrive.singapore.util.CommonActivity
import com.giosis.util.qdrive.singapore.util.DataUtil
import com.giosis.util.qdrive.singapore.util.ListFragmentFactoryImpl
import com.giosis.util.qdrive.singapore.util.Preferences
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.top_title.*

class ListActivity : CommonActivity(), ListInProgressFragment.OnInProgressFragmentListener,
    ListTodayDoneFragment.OnTodayDoneCountListener, ListUploadFailedFragment.OnFailedCountListener {

    var TAG = "ListActivity"

    var bluetoothClass: BluetoothClass? = null

    /* Fragment numbering */
    private val fragmentPage1 = 0
    private val fragmentPage2 = 1
    private val fragmentPage3 = 2


    override fun onCountRefresh(count: Int) {
        text_list_in_progress_count.text = count.toString()
    }

    override fun onFailedCountRefresh(count: Int) {
        text_list_upload_failed_count.text = count.toString()
    }

    override fun onTodayDoneCountRefresh(count: Int) {
        text_list_today_done_count.text = count.toString()
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

        text_top_title.text = resources.getString(R.string.navi_list)

        layout_in_progress.setOnClickListener {
            viewpager.currentItem = fragmentPage1
        }

        layout_upload_failed.setOnClickListener {
            viewpager.currentItem = fragmentPage2
        }

        layout_today_done.setOnClickListener {
            viewpager.currentItem = fragmentPage3
        }

        layout_top_back.setOnClickListener {
            DataUtil.inProgressListPosition = 0
            DataUtil.uploadFailedListPosition = 0
            val intent = Intent(this@ListActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        val pagerAdapter2 = PagerAdapter2(this)
        viewpager.adapter = pagerAdapter2

        bluetoothClass = BluetoothClass(this)

        viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                layout_in_progress.isSelected = false
                layout_upload_failed.isSelected = false
                layout_today_done.isSelected = false
                when (position) {
                    0 -> {
                        layout_in_progress.isSelected = true
                    }
                    1 -> {
                        layout_upload_failed.isSelected = true
                    }
                    2 -> {
                        layout_today_done.isSelected = true
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
                this@ListActivity,
                resources.getString(R.string.msg_qdrive_auto_logout),
                Toast.LENGTH_SHORT
            ).show()

            try {
                val intent = Intent(
                    this@ListActivity,
                    Class.forName("com.giosis.util.qdrive.singapore.LoginActivity")
                )
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
                    ListInProgressFragment(bluetoothClass as BluetoothListener)
                }
                1 -> ListUploadFailedFragment()
                2 -> ListTodayDoneFragment(bluetoothClass as BluetoothListener)
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
