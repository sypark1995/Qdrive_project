package com.giosis.library

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.giosis.library.util.SingleLiveEvent
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

open class BaseViewModel() : ViewModel() {

    private val _progressVisible = MutableLiveData<Boolean>()
    val progressVisible: MutableLiveData<Boolean>
        get() = _progressVisible

    private val _activityStart = SingleLiveEvent<StartActivityData>()
    val activityStart: SingleLiveEvent<StartActivityData>
        get() = _activityStart

    private val _finishActivity = SingleLiveEvent<Bundle?>()
    val finishActivity: SingleLiveEvent<Bundle?>
        get() = _finishActivity

    private val _toastString =  SingleLiveEvent<Int>()
    val toastString: SingleLiveEvent<Int>
        get() = _toastString

    fun startActivity(cls: Class<*>) {
        val activityModel = StartActivityData()
        activityModel.cls = cls
        activityStart.value = activityModel
    }

    fun startActivity(cls: Class<*>, bundle: Bundle) {
        val activityModel = StartActivityData()
        activityModel.cls = cls
        activityModel.params = bundle
        activityStart.value = activityModel
    }

    fun startActivity(cls: Class<*>, bundle: Bundle?, request: Int) {
        val activityModel = StartActivityData()
        activityModel.cls = cls
        activityModel.params = bundle
        activityModel.requestCode = request
        activityStart.value = activityModel
    }

    fun startActivityAddFlag(cls: Class<*>, bundle: Bundle, flag: Int) {
        val activityModel = StartActivityData()
        activityModel.cls = cls
        activityModel.params = bundle
        activityModel.flag = flag
        activityStart.value = activityModel
    }

    fun startActivity(cls: Class<*>, bundle: Bundle, flag: Int, request: Int) {
        val activityModel = StartActivityData()
        activityModel.cls = cls
        activityModel.params = bundle
        activityModel.flag = flag
        activityModel.requestCode = request
        activityStart.value = activityModel
    }

    fun finish() {
        finishActivity.call()
    }

    fun finish(bundle: Bundle) {
        finishActivity.value = bundle
    }

    private val disposables = CompositeDisposable()

    fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

}