package com.giosis.util.qdrive.singapore

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.giosis.util.qdrive.singapore.util.CommonActivity
import com.giosis.util.qdrive.singapore.util.dialog.ProgressDialog


abstract class ViewModelActivity<T : ViewDataBinding, V : BaseViewModel> : CommonActivity() {

    private lateinit var mViewDataBinding: T
    lateinit var mViewModel: V

    private val progressBar by lazy {
        ProgressDialog(this@ViewModelActivity)
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
        mViewDataBinding.lifecycleOwner = this

        mViewModel.progressVisible.observe(this) {
            progressBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        mViewModel.activityStart.observe(this) {
            runOnUiThread {
                val intent = Intent(this, it.cls)
                if (it.params != null) {
                    intent.putExtras(it.params!!)
                }
                if (it.flag != 0) {
                    intent.addFlags(it.flag)
                }
                if (it.requestCode != 0) {
                    startActivityForResult(intent, it.requestCode)
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

        mViewModel.toastString.observe(this) {
            runOnUiThread {
                var text = ""

                if (it is String) {
                    text = it
                } else if (it is Int) {
                    text = resources.getText(it).toString()
                }

                Toast.makeText(this@ViewModelActivity, text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getStringResID(resId: Int): String {
        return resources.getString(resId)
    }

}