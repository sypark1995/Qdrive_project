package com.giosis.library

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.giosis.library.util.SingleLiveEvent
import org.greenrobot.eventbus.EventBus

abstract class ListViewModel<T> : BaseViewModel() {

    private val _items = MutableLiveData<ArrayList<T>>()
    val items: LiveData<ArrayList<T>>
        get() = _items

    private val _scrollPosition = SingleLiveEvent<Int>()
    val scrollPosition: LiveData<Int>
        get() = _scrollPosition

    private val _notifyChange = MutableLiveData<Int>()
    val notifyChange: LiveData<Int>
        get() = _notifyChange

    init {
//        _items.value = ArrayList()
    }

    open fun getItemType(position: Int): Int {
        return 0
    }

    fun setItemList(itemList: ArrayList<T>) {
        this._items.value = itemList
    }

    fun getItemList(): ArrayList<T>? {
        return if (_items.value != null) {
            this._items.value
        } else {
            ArrayList()
        }
    }

    fun getItem(position: Int): T {
        return _items.value!![position]
    }

    fun getItemCount(): Int {
        return if (_items.value != null) {
            _items.value!!.size
        } else {
            0
        }
    }

    fun addItem(item: T): ArrayList<T> {
        _items.value?.add(item)
        return _items.value!!
    }

    fun addItem(itemList: ArrayList<T>?): ArrayList<T> {
        if (itemList != null) {
            _items.value?.addAll(itemList)
        }
        return _items.value!!
    }

    fun addItem(index: Int, item: T): ArrayList<T> {
        if (item != null) {
            _items.value?.add(index, item)
        }

        return _items.value!!
    }

    fun checkItemExist(): Boolean {
        return if (_items.value != null) {
            _items.value!!.size > 0
        } else {
            false
        }
    }

    fun setItem(index: Int, item: T) {
        _items.value!![index] = item
    }

    fun removeItem(item: T) {
        _items.value!!.remove(item)
    }

    fun removeItem(index: Int) {
        _items.value!!.removeAt(index)
    }

    fun removeItems(items: ArrayList<T>) {
        _items.value!!.removeAll(items)
    }


    fun clearList() {

        try {

            if (0 < _items.value!!.size)
                _items.value!!.clear()
        } catch (e: Exception) {

            Log.e("Exception", " ListViewModel  ${e.message}")
        }
    }


    fun getLastItem(): T {
        return if (_items.value!!.size > 0) {
            getItem(_items.value!!.size - 1)
        } else {
            getItem(0)
        }
    }

    fun scrollPosition(position: Int) {
        _scrollPosition.postValue(position)
    }

    fun notifyChange() {
        Log.e("TAG", "notifyChange value = 1")
        EventBus.getDefault().post("noti")
    }
}
