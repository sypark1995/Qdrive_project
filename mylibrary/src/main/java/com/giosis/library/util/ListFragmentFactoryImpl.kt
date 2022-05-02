package com.giosis.library.util

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.giosis.library.bluetooth.BluetoothClass
import com.giosis.library.list.ListInProgressFragment2
import com.giosis.library.list.ListTodayDoneFragment2

//  could not find Fragment constructor
// -> 모든 프래그먼트는 빈 생성자를 가져야 한다.  (InstantiationException)
class ListFragmentFactoryImpl(val activity: Activity) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {

        return when (className) {

            ListInProgressFragment2::class.java.name -> ListInProgressFragment2(BluetoothClass(activity))
            ListTodayDoneFragment2::class.java.name -> ListTodayDoneFragment2(BluetoothClass(activity))
            else -> super.instantiate(classLoader, className)
        }
    }
}