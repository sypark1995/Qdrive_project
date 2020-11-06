package com.giosis.util.qdrive.settings


class NoticeResults {

    lateinit var resultCode: String
    lateinit var resultMsg: String
    lateinit var resultObject: ArrayList<NoticeItem>

    class NoticeItem {

        lateinit var seqNo: String
        lateinit var title: String
        lateinit var date: String
        lateinit var content: String
        lateinit var prevNo: String
        lateinit var nextNo: String
    }
}