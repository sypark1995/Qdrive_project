package com.giosis.library

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.giosis.library.databinding.ActivityTestBinding


class TestActivity : BaseActivity<ActivityTestBinding, TestViewModel>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_test
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): TestViewModel {
        return ViewModelProvider(this).get(TestViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("TAG", "oncreate()")
    }



}