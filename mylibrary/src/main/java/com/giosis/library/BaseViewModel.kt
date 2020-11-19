package com.giosis.library

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.giosis.library.util.SingleLiveEvent
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlin.reflect.KClass

open class BaseViewModel() : ViewModel() {

    private val _progressVisible = MutableLiveData<Boolean>()
    val progressVisible: MutableLiveData<Boolean>
        get() = _progressVisible

    private val _activityStart = SingleLiveEvent<Triple<KClass<*>, Bundle?, Int>>()
    val activityStart: SingleLiveEvent<Triple<KClass<*>, Bundle?, Int>>
        get() = _activityStart

    private val _finishActivity = SingleLiveEvent<Bundle?>()
    val finishActivity: SingleLiveEvent<Bundle?>
        get() = _finishActivity

    fun startActivity(cls: KClass<*>) {
        activityStart.value = Triple(cls, null, 0)
    }

    fun startActivity(cls: KClass<*>, bundle: Bundle) {
        activityStart.value = Triple(cls, bundle, 0)
    }

    fun startActivity(cls: KClass<*>, bundle: Bundle, request: Int) {
        activityStart.value = Triple(cls, bundle, request)
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