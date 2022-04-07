package com.giosis.library.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import com.giosis.library.R
import com.giosis.library.databinding.ActivityMainBinding
import com.giosis.library.databinding.ViewNavListHeaderBinding
import com.giosis.library.list.ListActivity
import com.giosis.library.setting.SettingActivity
import com.giosis.library.util.CommonActivity
import com.giosis.library.util.Preferences
import java.util.*

open class AppBaseActivity : CommonActivity() {
    var TAG = "AppBaseActivity"

    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val headerBinding by lazy {
        ViewNavListHeaderBinding.inflate(LayoutInflater.from(this), null, false)
    }

    private var titleString: String = ""


    // Only SG
    private var customerMessageCount = 0
    private var adminMessageCount = 0

    fun setTopTitle(title: String) {
        titleString = title
        binding.appBar.textTopTitle.text = titleString
    }

    fun leftMenuGone() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            binding.drawerLayout.closeDrawer(Gravity.LEFT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.appBar.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        binding.drawerLayout.setScrimColor(Color.parseColor("#4D000000"))
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appBar.toolbar,
            R.string.button_open,
            R.string.button_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

//        binding.navList.addHeaderView(headerBinding.root)
//        val adapter2 = NavListViewAdapter2()
//        binding.navList.setAdapter(adapter2)

        //
        binding.layoutBottomBarHome.setOnClickListener {

            if (!titleString.contains(getString(R.string.navi_home))) {
                val intent = Intent(it.context, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.layoutBottomBarScan.setOnClickListener {
            val intent = Intent(it.context, ScanActivity::class.java)
            startActivity(intent)
        }

        binding.layoutBottomBarList.setOnClickListener {
            val intent = Intent(it.context, ListActivity::class.java)
            startActivity(intent)
        }

        binding.layoutBottomBarSetting.setOnClickListener {
            val intent = Intent(it.context, SettingActivity::class.java)
            startActivity(intent)
        }

        val leftViewAdapter = LeftViewAdapter()
        binding.navList.adapter = leftViewAdapter

        val leftSubList: ArrayList<String>
        val leftSubList2: ArrayList<String>

        if (Preferences.outletDriver == "Y") {
            leftSubList = ArrayList(
                listOf(
                    getString(R.string.text_start_delivery_for_outlet),
                    getString(R.string.navi_sub_delivery_done),
                    getString(R.string.navi_sub_pickup),
                    getString(R.string.navi_sub_self)
                )
            )
            leftSubList2 = ArrayList(
                listOf(
                    getString(R.string.navi_sub_in_progress),
                    getString(R.string.navi_sub_upload_fail),
                    getString(R.string.navi_sub_today_done),
                    getString(R.string.navi_sub_not_in_housed),
                    getString(R.string.text_outlet_order_status)
                )
            )
        } else {
            leftSubList = ArrayList(
                listOf(
                    getString(R.string.navi_sub_confirm_delivery),
                    getString(R.string.navi_sub_delivery_done),
                    getString(R.string.navi_sub_pickup),
                    getString(R.string.navi_sub_self)
                )
            )
            leftSubList2 = ArrayList(
                listOf(
                    getString(R.string.navi_sub_in_progress),
                    getString(R.string.navi_sub_upload_fail),
                    getString(R.string.navi_sub_today_done),
                    getString(R.string.navi_sub_not_in_housed)
                )
            )
        }

        leftViewAdapter.addItem(
            getDrawable(R.drawable.side_icon_home_selector),
            getString(R.string.navi_home),
            null,
        )

        leftViewAdapter.addItem(
            getDrawable(R.drawable.side_icon_home_selector),
            getString(R.string.navi_home),
            null,
        )

        leftViewAdapter.addItem(
            ContextCompat.getDrawable(this, R.drawable.side_icon_scan_selector),
            getString(R.string.navi_scan),
            leftSubList,
        )

        leftViewAdapter.addItem(
            ContextCompat.getDrawable(this, R.drawable.side_icon_list_selector),
            getString(R.string.navi_list),
            leftSubList2,
        )

        leftViewAdapter.addItem(
            ContextCompat.getDrawable(this, R.drawable.side_icon_statistics_selector),
            getString(R.string.navi_statistics),
            null,
        )

        if (Preferences.userNation == "SG" && Preferences.pickupDriver == "Y") {
            leftViewAdapter.addItem(
                ContextCompat.getDrawable(this, R.drawable.icon_pickup_order),
                getString(R.string.text_create_pickup_order),
                null,
            )
        }

        leftViewAdapter.addItem(
            ContextCompat.getDrawable(this, R.drawable.side_icon_settings_selector),
            getString(R.string.navi_setting),
            null,
        )
    }

    fun setMessageCount(customer_count: Int, admin_count: Int) {
        customerMessageCount = customer_count
        adminMessageCount = admin_count
        val count = customer_count + admin_count

        headerBinding.textMessageCount.visibility = View.VISIBLE
        headerBinding.textMessageCount.text = count.toString()
    }

    fun goneMessageCount() {

        headerBinding.textMessageCount.visibility = View.GONE
    }

    fun setNaviHeader(name: String?, office: String?) {

        headerBinding.textNavHeaderDriverName.text = name
        headerBinding.textNavHeaderDriverOffice.text = office
    }
}
