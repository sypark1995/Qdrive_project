package com.giosis.library.main.leftMenu

import com.giosis.library.R
import com.giosis.library.barcodescanner.CaptureActivity1
import com.giosis.library.list.ListActivity
import com.giosis.library.main.submenu.ListNotInHousedActivity
import com.giosis.library.main.submenu.OutletOrderStatusActivity
import com.giosis.library.main.submenu.StatisticsActivity
import com.giosis.library.pickup.CreatePickupOrderActivity
import com.giosis.library.setting.SettingActivity
import com.giosis.library.util.BarcodeType


object LeftMenu {

    val DELIVERY_OUTLET = SubMenuItem(
        title = R.string.text_start_delivery_for_outlet,
        className = CaptureActivity1::class.java,
        extras = {
            putInt("title_resource", R.string.text_title_driver_assign)
            putString("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER)
        }
    )

    val CONFIRM_DELIVERY = SubMenuItem(
        title = R.string.navi_sub_confirm_delivery,
        className = CaptureActivity1::class.java,
        extras = {
            putInt("title_resource", R.string.text_title_driver_assign)
            putString("type", BarcodeType.CONFIRM_MY_DELIVERY_ORDER)
        }
    )

    val DELIVERY_DONE = SubMenuItem(
        title = R.string.text_delivered,
        className = CaptureActivity1::class.java,
        extras = {
            putInt("title_resource", R.string.text_delivered)
            putString("type", BarcodeType.DELIVERY_DONE)
        }
    )

    val PICKUP_CNR = SubMenuItem(
        title = R.string.text_title_scan_pickup_cnr,
        className = CaptureActivity1::class.java,
        extras = {
            putInt("title_resource", R.string.text_delivered)
            putString("type", BarcodeType.PICKUP_CNR)
        }
    )

    val SELF_COLLECT = SubMenuItem(
        title = R.string.navi_sub_self,
        className = CaptureActivity1::class.java,
        extras = {
            putInt("title_resource", R.string.navi_sub_self)
            putString("type", BarcodeType.SELF_COLLECTION)
        }
    )

    val IN_PROGRESS = SubMenuItem(
        title = R.string.navi_sub_in_progress,
        className = ListActivity::class.java,
    )

    val UPLOAD_FAIL = SubMenuItem(
        title = R.string.navi_sub_upload_fail,
        className = ListActivity::class.java,
    )

    val DEL_DONE = SubMenuItem(
        title = R.string.navi_sub_today_done,
        className = ListActivity::class.java,
    )

    val NOT_IN_PARCELS = SubMenuItem(
        title = R.string.navi_sub_not_in_housed,
        className = ListNotInHousedActivity::class.java,
    )

    val OUTLET_STATUS = SubMenuItem(
        title = R.string.text_outlet_order_status,
        className = OutletOrderStatusActivity::class.java,
    )

    val EMPTY_MENU = NavListItem(
    )

    val HOME_MENU = NavListItem(
        iconId = R.drawable.side_icon_home_selector,
        titleResource = R.string.navi_home,
    )

    val SCAN_MENU = NavListItem(
        iconId = R.drawable.memu_scan_selector,
        titleResource = R.string.navi_scan,
        subList = ArrayList(
            listOf(
                DELIVERY_DONE,
                PICKUP_CNR,
                SELF_COLLECT,
            )
        )
    )

    val LIST_MENU = NavListItem(
        iconId = R.drawable.menu_list_selector,
        titleResource = R.string.navi_list,
        subList = ArrayList(
            listOf(
                IN_PROGRESS,
                UPLOAD_FAIL,
                DEL_DONE,
                NOT_IN_PARCELS,
                OUTLET_STATUS,
            )
        )
    )

    val STATI_MENU = NavListItem(
        iconId = R.drawable.side_icon_statistics_selector,
        titleResource = R.string.navi_statistics,
        className = StatisticsActivity::class.java
    )

    val CREATE_PICKUP_MENU = NavListItem(
        iconId = R.drawable.icon_pickup_order,
        titleResource = R.string.text_create_pickup_order,
        className = CreatePickupOrderActivity::class.java
    )

    val SETTING_MENU = NavListItem(
        iconId = R.drawable.side_icon_settings_selector,
        titleResource = R.string.navi_setting,
        className = SettingActivity::class.java
    )
}