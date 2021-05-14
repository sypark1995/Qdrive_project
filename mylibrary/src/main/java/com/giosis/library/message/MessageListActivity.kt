package com.giosis.library.message

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.giosis.library.R
import com.giosis.library.util.CommonActivity
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_message_list.*
import kotlinx.android.synthetic.main.top_title.*

/**
 * @author krm0219
 */
class MessageListActivity : CommonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        text_top_title.setText(R.string.text_title_message)
        layout_top_back.setOnClickListener {

            finish()
        }


        val viewPagerPosition = intent.getIntExtra("position", 0)
        val customerMessageCount = intent.getIntExtra("customer_count", 0)
        val adminMessageCount = intent.getIntExtra("admin_count", 0)
        Log.e(TAG, "Position  $viewPagerPosition    // Count $customerMessageCount, $adminMessageCount")
        setCustomerNewImage(customerMessageCount)
        setAdminNewImage(adminMessageCount)


        pager.offscreenPageLimit = 1
        pager.adapter = MessageAdapter(this)
        pager.currentItem = viewPagerPosition

        TabLayoutMediator(tab_layout, pager) { tab, position ->

            when (position) {
                0 -> tab.text = resources.getString(R.string.text_customer)
                1 -> tab.text = resources.getString(R.string.text_administrator)
            }
        }.attach()
    }


    private inner class MessageAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {

            return when (position) {
                0 -> CustomerMessageListFragment()
                else -> AdminMessageListFragment()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
    }

    fun setCustomerNewImage(count: Int) {
        Log.e(TAG, "setCustomerNewImage : $count")
        if (count != 0) {

            tab_layout.getTabAt(0)?.orCreateBadge
        } else {

            tab_layout.getTabAt(0)?.removeBadge()
        }
    }

    fun setAdminNewImage(count: Int) {
        Log.e(TAG, " setAdminNewImage : $count")
        if (count != 0) {

            tab_layout.getTabAt(1)?.orCreateBadge
        } else {

            tab_layout.getTabAt(1)?.removeBadge()
        }
    }

    companion object {

        var TAG = "MessageListActivity"
    }
}