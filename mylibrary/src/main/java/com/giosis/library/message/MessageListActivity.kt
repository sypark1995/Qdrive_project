package com.giosis.library.message

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.giosis.library.R
import com.giosis.library.util.CommonActivity
import com.giosis.library.util.DataUtil
import kotlinx.android.synthetic.main.activity_message_list.*
import kotlinx.android.synthetic.main.top_title.*

/**
 * @author krm0219
 */
class MessageListActivity : CommonActivity() {
    var TAG = "MessageListActivity"


    //
    lateinit var customerMessageListFragment: CustomerMessageListFragment
    lateinit var adminMessageListFragment: AdminMessageListFragment

    private var pagerAdapter: MessageListActivity.FragmentPagerAdapter? = null
    var viewPagerPosition = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)


        pagerAdapter = FragmentPagerAdapter(supportFragmentManager)
        viewpager_message_list.adapter = pagerAdapter


        viewPagerPosition = intent.getIntExtra("position", FRAGMENT_PAGE1)
        val customerMessageCount = intent.getIntExtra("customer_count", 0)
        val adminMessageCount = intent.getIntExtra("admin_count", 0)


        text_top_title.setText(R.string.text_title_message)
        setCustomerNewImage(customerMessageCount)
        setAdminNewImage(adminMessageCount)


        viewpager_message_list.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageSelected(position: Int) {

                layout_message_list_customer.isSelected = false
                layout_message_list_admin.isSelected = false

                when (position) {
                    0 -> {
                        viewPagerPosition = 0
                        layout_message_list_customer.isSelected = true
                    }
                    1 -> {
                        viewPagerPosition = 1
                        layout_message_list_admin.isSelected = true
                    }
                }
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
            override fun onPageScrollStateChanged(arg0: Int) {}
        })

        if (viewPagerPosition == 0) {

            layout_message_list_customer.isSelected = true
        } else {

            layout_message_list_admin.isSelected = true
        }


        //
        layout_top_back.setOnClickListener {
            finish()
        }

        layout_message_list_customer.setOnClickListener {

            viewpager_message_list!!.currentItem = FRAGMENT_PAGE1
        }

        layout_message_list_admin.setOnClickListener {

            viewpager_message_list!!.currentItem = FRAGMENT_PAGE2
        }
    }


    override fun onResume() {
        super.onResume()

        DataUtil.setMessageListActivity(this)
        viewpager_message_list!!.currentItem = viewPagerPosition
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    fun setCustomerNewImage(customer_count: Int) {
        Log.e("Message", "setCustomerNewImage : $customer_count")
        if (customer_count != 0) {
            img_message_list_customer_new!!.visibility = View.VISIBLE
        } else {
            img_message_list_customer_new!!.visibility = View.GONE
        }
    }

    fun setAdminNewImage(admin_count: Int) {
        Log.e("Message", " setAdminNewImage : $admin_count")
        if (admin_count != 0) {
            img_message_list_admin_new!!.visibility = View.VISIBLE
        } else {
            img_message_list_admin_new!!.visibility = View.GONE
        }
    }


    inner class FragmentPagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    customerMessageListFragment = CustomerMessageListFragment()
                    customerMessageListFragment
                }
                1 -> {
                    adminMessageListFragment = AdminMessageListFragment()
                    adminMessageListFragment
                }
                else -> {

                    customerMessageListFragment = CustomerMessageListFragment()
                    customerMessageListFragment
                }
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getItemPosition(`object`: Any): Int {
            return super.getItemPosition(`object`)
        }
    }


    companion object {
        const val FRAGMENT_PAGE1 = 0
        const val FRAGMENT_PAGE2 = 1
    }
}