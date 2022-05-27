package com.giosis.util.qdrive.singapore.message


import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.databinding.ActivityMessageListBinding
import com.giosis.util.qdrive.singapore.util.CommonActivity
import com.giosis.util.qdrive.singapore.util.FirebaseEvent
import com.google.android.material.tabs.TabLayoutMediator


class MessageListActivity : CommonActivity() {
    val TAG = "MessageListActivity"

    private val binding by lazy {
        ActivityMessageListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        FirebaseEvent.createEvent(this, TAG)

        binding.layoutTopTitle.textTopTitle.setText(R.string.text_title_message)
        binding.layoutTopTitle.layoutTopBack.setOnClickListener {
            finish()
        }

        val viewPagerPosition = intent.getIntExtra("position", 0)
        val customerMessageCount = intent.getIntExtra("customer_count", 0)
        val adminMessageCount = intent.getIntExtra("admin_count", 0)

        val pagerAdapter = PagerFragmentStateAdapter(this)
        pagerAdapter.addFragment(CustomerMessageListFragment())
        pagerAdapter.addFragment(AdminMessageListFragment())
        binding.pager2.adapter = pagerAdapter
        binding.pager2.currentItem = viewPagerPosition

        val tabElement = listOf(
            resources.getString(R.string.text_customer),
            resources.getString(R.string.text_administrator)
        )
        TabLayoutMediator(binding.tabLayout, binding.pager2) { tab, position ->
            tab.text = tabElement[position]
        }.attach()


        Log.e(TAG, "Position $viewPagerPosition \nCount $customerMessageCount, $adminMessageCount")
        setCustomerNewImage(customerMessageCount)
        setAdminNewImage(adminMessageCount)
    }


    private inner class PagerFragmentStateAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {

        var fragments: ArrayList<Fragment> = ArrayList()

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {

            return fragments[position]
        }

        fun addFragment(fragment: Fragment) {

            fragments.add(fragment)
            notifyItemInserted(fragments.size - 1)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
    }

    fun setCustomerNewImage(count: Int) {
        Log.e(TAG, "setCustomerNewImage : $count")
        if (count != 0) {

            binding.tabLayout.getTabAt(0)?.orCreateBadge
        } else {

            binding.tabLayout.getTabAt(0)?.removeBadge()
        }
    }

    fun setAdminNewImage(count: Int) {
        Log.e(TAG, " setAdminNewImage : $count   ")
        if (count != 0) {

            binding.tabLayout.getTabAt(1)?.orCreateBadge
        } else {

            binding.tabLayout.getTabAt(1)?.removeBadge()
        }
    }

    companion object {

        var TAG = "MessageListActivity"
    }
}