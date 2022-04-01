package com.giosis.library.setting

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.BR
import com.giosis.library.BaseActivity
import com.giosis.library.R
import com.giosis.library.databinding.ActivityNoticeDetailBinding
import kotlinx.android.synthetic.main.activity_notice_detail.*
import kotlinx.android.synthetic.main.top_title.*

class NoticeDetailActivity : BaseActivity<ActivityNoticeDetailBinding, NoticeDetailViewModel>() {
    val tag = "NoticeDetailActivity"

    override fun getLayoutId(): Int {
        return R.layout.activity_notice_detail
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): NoticeDetailViewModel {
        return ViewModelProvider(this).get(NoticeDetailViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text_top_title.text = resources.getString(R.string.text_title_notice)

        layout_top_back.setOnClickListener {
            finish()
        }

        val noticeNo = intent.getStringExtra("notice_no").toString()
        getViewModel().setSeqNo(noticeNo)

        getViewModel().seqNo.observe(this, Observer {
            getViewModel().callServer()
        })


        getViewModel().content.observe(this, Observer {
            text_notice_detail_content.text = it
        })

        getViewModel().date.observe(this, Observer {
            text_notice_detail_date.text = it
        })

        getViewModel().prevNo.observe(this, Observer {
            if (it.isEmpty()) {
                layout_notice_detail_prev.visibility = View.GONE
            } else {
                layout_notice_detail_prev.visibility = View.VISIBLE
            }
        })

        getViewModel().nextNo.observe(this, Observer {
            if (it.isEmpty()) {
                layout_notice_detail_next.visibility = View.GONE
            } else {
                layout_notice_detail_next.visibility = View.VISIBLE
            }
        })

        getViewModel().errorAlert.observe(this, Observer {
            layout_notice_detail_reload.visibility = View.VISIBLE
            layout_notice_detail.visibility = View.GONE
        })

        getViewModel().resultAlert.observe(this, Observer {

            if (it is String || it is Int) {
                layout_notice_detail_reload.visibility = View.VISIBLE
                layout_notice_detail.visibility = View.GONE

            } else {
                layout_notice_detail_reload.visibility = View.GONE
                layout_notice_detail.visibility = View.VISIBLE
            }
        })
    }
}