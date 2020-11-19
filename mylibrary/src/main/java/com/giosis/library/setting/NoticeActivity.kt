package com.giosis.library.setting

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityNoticeBinding
import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.android.synthetic.main.top_title.*

class NoticeActivity : BaseActivity<ActivityNoticeBinding, NoticeViewModel>() {
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


    lateinit var adapter: NoticeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text_top_title.text = resources.getString(R.string.text_title_notice)

        layout_top_back.setOnClickListener {
            finish()
        }


        adapter = NoticeAdapter(this@NoticeActivity)
        recycler_notice.adapter = adapter


        getViewModel().noticeItems.observe(this) {

            adapter.setItems(it)
        }


        getViewModel().errorMsg.observe(this) {

            if (it is String) {

                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            } else if (it is Int) {

                Toast.makeText(this, resources.getString(it), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        getViewModel().callServer()
    }
}