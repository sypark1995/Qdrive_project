package com.giosis.library.main

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
import com.giosis.library.message.MessageListActivity
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

//    val adapter by lazy {
//        NavListViewAdapter(this)
//    }


    private var titleString: String = ""


    // Only SG
    private var customerMessageCount = 0
    private var adminMessageCount = 0

    fun setTopTitle(title: String) {
        titleString = title
        binding.appBar.textTopTitle.text = titleString
    }
    fun leftMenuGone() {
        val item = NavListItem()
        if (binding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            binding.drawerLayout.closeDrawer(Gravity.LEFT)
            item.isClicked = false
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

        headerBinding.btnMessage.setOnClickListener {

            val intent = Intent(it.context, MessageListActivity::class.java)
            intent.putExtra("customer_count", customerMessageCount)
            intent.putExtra("admin_count", adminMessageCount)
            startActivity(intent)
            binding.drawerLayout.closeDrawers()
        }


        if (Preferences.userNation == "SG") {
            headerBinding.layoutMessage.visibility = View.VISIBLE
        } else {
            headerBinding.layoutMessage.visibility = View.GONE
        }

        // sub divider 칼라 없앰
//        binding.navList.setChildDivider(resources.getDrawable(R.color.transparent))

        val adapter2 = NavListViewAdapter2()
        binding.navList.adapter = adapter2
        val arrayList: ArrayList<String>
        val arrayList1: ArrayList<String>
        if (Preferences.outletDriver == "Y") {
            arrayList = ArrayList(
                listOf(
                    getString(R.string.text_start_delivery_for_outlet),
                    getString(R.string.navi_sub_delivery_done),
                    getString(R.string.navi_sub_pickup),
                    getString(R.string.navi_sub_self)
                )
            )
            arrayList1 = ArrayList(
                listOf(
                    getString(R.string.navi_sub_in_progress),
                    getString(R.string.navi_sub_upload_fail),
                    getString(R.string.navi_sub_today_done),
                    getString(R.string.navi_sub_not_in_housed),
                    getString(R.string.text_outlet_order_status)
                )
            )
        } else {
            arrayList = ArrayList(
                listOf(
                    getString(R.string.navi_sub_confirm_delivery),
                    getString(R.string.navi_sub_delivery_done),
                    getString(R.string.navi_sub_pickup),
                    getString(R.string.navi_sub_self)
                )
            )
            arrayList1 = ArrayList(
                listOf(
                    getString(R.string.navi_sub_in_progress),
                    getString(R.string.navi_sub_upload_fail),
                    getString(R.string.navi_sub_today_done),
                    getString(R.string.navi_sub_not_in_housed)
                )
            )
        }
        val leftList = ArrayList(resources.getStringArray(R.array.left_menu).toMutableList())

        if (Preferences.userNation == "SG" && Preferences.pickupDriver == "Y") {
            leftList.add(4,resources.getString(R.string.text_create_pickup_order))
        }


        adapter2.addItem(
            getDrawable(R.drawable.side_icon_home_selector),
            getString(R.string.navi_home),
            null,
            -1
        )
        adapter2.addItem(
            getDrawable(R.drawable.side_icon_home_selector),
            getString(R.string.navi_home),
            null,
            -1
        )
        adapter2.addItem(
            ContextCompat.getDrawable(this, R.drawable.side_icon_scan_selector),
            getString(R.string.navi_scan),
            arrayList,
            -1
        )
        adapter2.addItem(
            ContextCompat.getDrawable(this, R.drawable.side_icon_list_selector),
            getString(R.string.navi_list),
            arrayList1,
            -1
        )
        adapter2.addItem(
            ContextCompat.getDrawable(this, R.drawable.side_icon_statistics_selector),
            getString(R.string.navi_statistics),
            null,
            -1
        )
        adapter2.addItem(
            ContextCompat.getDrawable(this, R.drawable.side_icon_settings_selector),
            getString(R.string.navi_setting),
            null,
            -1
        )

        if (Preferences.userNation == "SG" && Preferences.pickupDriver == "Y") {
            adapter2.addItem(
                ContextCompat.getDrawable(this, R.drawable.icon_pickup_order),
                getString(R.string.text_create_pickup_order),
                null,
                4
            )
        }

        // NOTIFICATION. 추후 반영
//        else if (!Preferences.INSTANCE.getUserNation().equalsIgnoreCase("SG")) {   // MY / ID
//             adapter.addItem(ContextCompat.getDrawable(this, R.drawable.side_icon_route_selector), getString(R.string.text_today_my_route), null, 4);
//        }

/*        binding.navList.setOnGroupClickListener { _, _, position: Int, _ ->

            val title = adapter.getItem(position).title

            if (title == getString(R.string.navi_home)) {

                binding.drawerLayout.closeDrawers()
                if (!titleString.contains(getString(R.string.navi_home))) {
                    val intent = Intent(this@AppBaseActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else if (title == getString(R.string.navi_statistics)) {

                binding.drawerLayout.closeDrawers()
                val intent = Intent(this@AppBaseActivity, StatisticsActivity::class.java)
                startActivity(intent)
                if (!titleString.contains(getString(R.string.navi_home))) {
                    finish()
                }
            } else if (title == getString(R.string.navi_setting)) {

                binding.drawerLayout.closeDrawers()
                // TEST_
                //Intent intent = new Intent(AppBaseActivity.this, SMSVerificationActivity.class);
                val intent = Intent(this@AppBaseActivity, SettingActivity::class.java)
                startActivity(intent)
                if (!titleString.contains(getString(R.string.navi_home))) {
                    finish()
                }
            } else if (title == getString(R.string.text_create_pickup_order)) {

                binding.drawerLayout.closeDrawers()
                val intent = Intent(this@AppBaseActivity, CreatePickupOrderActivity::class.java)
                startActivity(intent)
                if (!titleString.contains(getString(R.string.navi_home))) {
                    finish()
                }
            } else if (title == getString(R.string.text_today_my_route)) {

                binding.drawerLayout.closeDrawers()
                val intent = Intent(this@AppBaseActivity, TodayMyRouteActivity::class.java)
                startActivity(intent)
                if (!titleString.contains(getString(R.string.navi_home))) {
                    finish()
                }
            }
            false
        }


        binding.navList.setOnGroupExpandListener { position: Int ->
            for (i in 0 until adapter.groupCount) {
                if (position != i) {
                    binding.navList.collapseGroup(i)
                }
            }
        }

        binding.navList.setOnChildClickListener { _: ExpandableListView?, _, group_position: Int, child_position: Int, _ ->

            // SCAN, LIST
            if (group_position == 1) {           // SCAN
                when (child_position) {
                    0 -> {
                        // Confirm my delivery order  / Start Delivery for Outlet
                        val intent = Intent(this@AppBaseActivity, CaptureActivity1::class.java)
                        intent.putExtra(
                            "title",
                            resources.getString(R.string.text_title_driver_assign)
                        )
                        intent.putExtra("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER)
                        startActivity(intent)
                    }
                    1 -> {
                        // Delivery done
                        val intent = Intent(this@AppBaseActivity, CaptureActivity1::class.java)
                        intent.putExtra("title", resources.getString(R.string.text_delivered))
                        intent.putExtra("type", BarcodeType.DELIVERY_DONE)
                        startActivity(intent)
                    }
                    2 -> {
                        // Pickup C&R Parcels
                        val intent = Intent(this@AppBaseActivity, CaptureActivity1::class.java)
                        intent.putExtra(
                            "title",
                            resources.getString(R.string.text_title_scan_pickup_cnr)
                        )
                        intent.putExtra("type", BarcodeType.PICKUP_CNR)
                        startActivity(intent)
                    }
                    3 -> {
                        // Self-collection
                        val intent = Intent(this@AppBaseActivity, CaptureActivity1::class.java)
                        intent.putExtra("title", resources.getString(R.string.navi_sub_self))
                        intent.putExtra("type", BarcodeType.SELF_COLLECTION)
                        startActivity(intent)
                    }
                }
            } else if (group_position == 2) {           // LIST
                when (child_position) {
                    0, 1, 2 -> {
                        val intent = Intent(this@AppBaseActivity, ListActivity::class.java)
                        startActivity(intent)
                    }
                    3 -> {
                        // Not In-housed
                        val intent =
                            Intent(this@AppBaseActivity, ListNotInHousedActivity::class.java)
                        startActivity(intent)
                    }
                    4 -> {
                        // Outlet - Outlet Order Status
                        val intent =
                            Intent(this@AppBaseActivity, OutletOrderStatusActivity::class.java)
                        startActivity(intent)
                    }
                }
            }

            for (i in 0 until adapter.groupCount) {
                binding.navList.collapseGroup(i)
            }
            binding.drawerLayout.closeDrawers()

            false
        }*/
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
