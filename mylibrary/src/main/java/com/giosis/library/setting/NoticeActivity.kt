package com.giosis.library.setting

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.BR
import com.giosis.library.ViewModelActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityNoticeBinding
import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.android.synthetic.main.top_title.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NoticeActivity : ViewModelActivity<ActivityNoticeBinding, NoticeViewModel>() {
    val tag = "NoticeDetailActivity"

    override fun getLayoutId(): Int {
        return R.layout.activity_notice
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): NoticeViewModel {
        return ViewModelProvider(this).get(NoticeViewModel::class.java)
    }

    val adapter: NoticeAdapter by lazy {
        NoticeAdapter(mViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text_top_title.text = resources.getString(R.string.text_title_notice)

        layout_top_back.setOnClickListener {
            finish()
        }

        recycler_notice.adapter = adapter

        getViewModel().errorMsg.observe(this, Observer {

            if (it is String) {

                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            } else if (it is Int) {

                Toast.makeText(this, resources.getString(it), Toast.LENGTH_SHORT).show()
            }
        })

    }


    override fun onResume() {
        super.onResume()
        getViewModel().callServer()
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun noti(event: String?) {
        adapter.notifyDataSetChanged()
    }


    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

}