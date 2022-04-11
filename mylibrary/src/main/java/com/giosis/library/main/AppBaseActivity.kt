package com.giosis.library.main


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import com.giosis.library.R
import com.giosis.library.databinding.ActivityMainBinding
import com.giosis.library.databinding.ViewNavListHeaderBinding
import com.giosis.library.list.ListActivity
import com.giosis.library.main.leftMenu.LeftMenu
import com.giosis.library.main.leftMenu.LeftViewAdapter
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

        if (Preferences.outletDriver == "Y") {
            LeftMenu.SCAN_MENU.subList!!.add(0, LeftMenu.DELIVERY_OUTLET)
        } else {
            LeftMenu.SCAN_MENU.subList!!.add(0, LeftMenu.CONFIRM_DELIVERY)
        }

        if (Preferences.outletDriver == "Y") {
            LeftMenu.LIST_MENU.subList!!.add(LeftMenu.OUTLET_STATUS)
        }

        val listItemList = ArrayList(
            listOf(
                LeftMenu.EMPTY_MENU,
                LeftMenu.HOME_MENU,
                LeftMenu.SCAN_MENU,
                LeftMenu.LIST_MENU,
                LeftMenu.STATI_MENU,
                LeftMenu.SETTING_MENU
            )
        )

        if (Preferences.userNation == "SG" && Preferences.pickupDriver == "Y") {
            listItemList.add(listItemList.size - 2, LeftMenu.CREATE_PICKUP_MENU)
        }

        leftViewAdapter.item = listItemList
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