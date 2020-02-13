package com.giosis.util.qdrive.settings;

import java.util.List;

/**
 * @author krm0219
 */
public class NoticeResult {

    private String resultCode = "-1";
    private String resultMsg = "";
    private List<NoticeListItem> resultObject;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public List<NoticeListItem> getResultObject() {
        return resultObject;
    }

    public void setResultObject(List<NoticeListItem> resultObject) {
        this.resultObject = resultObject;
    }


    public static class NoticeListItem {

        String noticeNo;
        String noticeTitle;
        String noticeDate;
        String noticeContent;
        String prevNo;
        String nextNo;


        public String getNoticeNo() {
            return noticeNo;
        }

        public void setNoticeNo(String noticeNo) {
            this.noticeNo = noticeNo;
        }

        public String getNoticeTitle() {
            return noticeTitle;
        }

        public void setNoticeTitle(String noticeTitle) {
            this.noticeTitle = noticeTitle;
        }

        public String getNoticeDate() {
            return noticeDate;
        }

        public void setNoticeDate(String noticeDate) {
            this.noticeDate = noticeDate;
        }

        public String getNoticeContent() {
            return noticeContent;
        }

        public void setNoticeContent(String noticeContent) {
            this.noticeContent = noticeContent;
        }

        public String getPrevNo() {
            return prevNo;
        }

        public void setPrevNo(String prevNo) {
            this.prevNo = prevNo;
        }

        public String getNextNo() {
            return nextNo;
        }

        public void setNextNo(String nextNo) {
            this.nextNo = nextNo;
        }
    }
}
