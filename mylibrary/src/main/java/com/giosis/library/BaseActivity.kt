package com.giosis.library

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.giosis.library.util.dialog.ProgressDialog


abstract class BaseActivity<T : ViewDataBinding, V : BaseViewModel> : AppCompatActivity() {

    private lateinit var mViewDataBinding: T
    lateinit var mViewModel: V

    private val progressBar by lazy {
        ProgressDialog(this@BaseActivity)
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun getBindingVariable(): Int

    abstract fun getViewModel(): V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId())
        mViewModel = getViewModel()
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel)
        mViewDataBinding.executePendingBindings()

        mViewModel.progressVisible.observe(this) {
            progressBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        mViewModel.activityStart.observe(this) {
            runOnUiThread {
                val intent = Intent(this, it.first.java)
                if (it.second != null) {
                    intent.putExtras(it.second!!)
                }

                if (it.third != 0) {
                    startActivityForResult(intent, it.third)
                } else {
                    startActivity(intent)
                }
            }
        }

        mViewModel.finishActivity.observe(this) {
            runOnUiThread {
                val intent = Intent()
                if (it != null) {
                    intent.putExtras(it)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    fun getStringResID(resId: Int): String {
        return resources.getString(resId)
    }

}