package com.giosis.util.qdrive.singapore.util

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.giosis.util.qdrive.singapore.bluetooth.BluetoothClass
import com.giosis.util.qdrive.singapore.list.ListInProgressFragment
import com.giosis.util.qdrive.singapore.list.ListTodayDoneFragment

//  could not find Fragment constructor
// -> 모든 프래그먼트는 빈 생성자를 가져야 한다.  (InstantiationException)
class ListFragmentFactoryImpl(val activity: Activity) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {

        return when (className) {

            ListInProgressFragment::class.java.name -> ListInProgressFragment(BluetoothClass(activity))
            ListTodayDoneFragment::class.java.name -> ListTodayDoneFragment(BluetoothClass(activity))
            else -> super.instantiate(classLoader, className)
        }
    }
}