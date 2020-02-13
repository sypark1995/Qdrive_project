package com.giosis.util.qdrive.util;

import android.util.Log;

import com.giosis.util.qdrive.settings.LockerUserInfoResult;
import com.giosis.util.qdrive.message.MessageCountResult;
import com.giosis.util.qdrive.message.MessageDetailResult;
import com.giosis.util.qdrive.message.MessageListResult;
import com.giosis.util.qdrive.message.MessageQuestionNumberResult;
import com.giosis.util.qdrive.message.MessageSendResult;
import com.giosis.util.qdrive.main.NotInHousedResult;
import com.giosis.util.qdrive.list.pickup.OutletPickupDoneResult;
import com.giosis.util.qdrive.list.delivery.QRCodeResult;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * @author krm0219
 */
public class Custom_XmlPullParser {

    public static MessageCountResult getMessageCount(String object) {

        MessageCountResult item = null;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(object));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("StdCustomResultOfInt32")) {

                        item = new MessageCountResult();
                    } else if (sTag.equals("ResultCode")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResult_code(parser.getText());
                        }
                    } else if (sTag.equals("ResultMsg")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResult_msg(parser.getText());
                        }
                    } else if (sTag.equals("ResultObject")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setMessage_count(Integer.parseInt(parser.getText()));
                        }
                    }
                }

                eventType = parser.next();
            }

            return item;

        } catch (Exception e) {
            Log.e("krm0219", "Custom_XmlPullParser.getMessageCount  Exception : " + e.toString());
        }

        return item;
    }

    // 2018.08
    public static MessageListResult getCustomerMessageList(String object) {

        MessageListResult item = null;

        ArrayList<MessageListResult.MessageList> messageList = new ArrayList<>();
        MessageListResult.MessageList messageListItem = null;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(object));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("StdCustomResultOfListOfGetListQPostsInQdrive")) {

                        item = new MessageListResult();
                    } else if (sTag.equals("ResultCode")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultCode(Integer.parseInt(parser.getText()));
                        }
                    } else if (sTag.equals("ResultMsg")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultMsg(parser.getText());
                        }
                    } else if (sTag.equals("GetListQPostsInQdrive")) {          // List Name

                        messageListItem = new MessageListResult.MessageList();
                    } else if (sTag.equals("tracking_No")) {

                        messageListItem.setTracking_no(parser.nextText());
                    } else if (sTag.equals("contents")) {

                        messageListItem.setMessage(parser.nextText());
                    } else if (sTag.equals("send_dt")) {

                        messageListItem.setTime(parser.nextText());
                    } else if (sTag.equals("read_yn")) {

                        messageListItem.setRead_yn(parser.nextText());
                    } else if (sTag.equals("total_page")) {

                        messageListItem.setTotal_page_size(Integer.parseInt(parser.nextText()));
                    } else if (sTag.equals("question_seq_no")) {

                        messageListItem.setQuestion_seq_no(Integer.parseInt(parser.nextText()));
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("GetListQPostsInQdrive") && messageListItem != null) {

                        messageList.add(messageListItem);
                        messageListItem = null;
                    }
                }

                eventType = parser.next();
            }

            item.setResultObject(messageList);
            return item;

        } catch (Exception e) {
            Log.e("krm0219", "Custom_XmlPullParser.getCustomerMessageList  Exception : " + e.toString());
        }

        return item;
    }

    public static MessageListResult getAdminMessageList(String object) {

        MessageListResult item = null;

        ArrayList<MessageListResult.MessageList> messageList = new ArrayList<>();
        MessageListResult.MessageList messageListItem = null;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(object));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("StdCustomResultOfListOfGetListQPostsInQdrive")) {

                        item = new MessageListResult();
                    } else if (sTag.equals("ResultCode")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultCode(Integer.parseInt(parser.getText()));
                        }
                    } else if (sTag.equals("ResultMsg")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultMsg(parser.getText());
                        }
                    } else if (sTag.equals("GetListQPostsInQdrive")) {          // List Name

                        messageListItem = new MessageListResult.MessageList();
                    } else if (sTag.equals("contents")) {

                        messageListItem.setMessage(parser.nextText());
                    } else if (sTag.equals("send_dt")) {

                        messageListItem.setTime(parser.nextText());
                    } else if (sTag.equals("read_yn")) {

                        messageListItem.setRead_yn(parser.nextText());
                    } else if (sTag.equals("question_seq_no")) {

                        messageListItem.setQuestion_seq_no(Integer.parseInt(parser.nextText()));
                    } else if (sTag.equals("sender_id")) {

                        messageListItem.setSender_id(parser.nextText());
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("GetListQPostsInQdrive") && messageListItem != null) {

                        messageList.add(messageListItem);
                        messageListItem = null;
                    }
                }

                eventType = parser.next();
            }

            item.setResultObject(messageList);
            return item;

        } catch (Exception e) {
            Log.e("krm0219", "Custom_XmlPullParser.getAdminMessageList  Exception : " + e.toString());
        }

        return item;
    }

    public static MessageDetailResult getMessageDetailList(String object) {

        MessageDetailResult item = null;

        ArrayList<MessageDetailResult.MessageDetailList> messageDetailList = new ArrayList<>();
        MessageDetailResult.MessageDetailList messageDetailListItem = null;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(object));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("StdCustomResultOfListOfGetDetailsQPostsInQdrive")) {

                        item = new MessageDetailResult();
                    } else if (sTag.equals("ResultCode")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultCode(Integer.parseInt(parser.getText()));
                        }
                    } else if (sTag.equals("ResultMsg")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultMsg(parser.getText());
                        }
                    } else if (sTag.equals("GetDetailsQPostsInQdrive")) {          // List Name

                        messageDetailListItem = new MessageDetailResult.MessageDetailList();
                    } else if (sTag.equals("tracking_No")) {

                        messageDetailListItem.setTracking_no(parser.nextText());
                    } else if (sTag.equals("question_seq_no")) {

                        messageDetailListItem.setQuestion_seq_no(parser.nextText());
                    } else if (sTag.equals("contents")) {

                        messageDetailListItem.setMessage(parser.nextText());
                    } else if (sTag.equals("sender_id")) {

                        messageDetailListItem.setSender_id(parser.nextText());
                    } else if (sTag.equals("rcv_id")) {

                        messageDetailListItem.setReceive_id(parser.nextText());
                    } else if (sTag.equals("send_dt")) {

                        messageDetailListItem.setSend_date(parser.nextText());
                    } else if (sTag.equals("align")) {

                        messageDetailListItem.setAlign(parser.nextText());
                    } else if (sTag.equals("title")) {

                        messageDetailListItem.setTitle(parser.nextText());
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("GetDetailsQPostsInQdrive") && messageDetailListItem != null) {

                        messageDetailList.add(messageDetailListItem);
                        messageDetailListItem = null;
                    }
                }

                eventType = parser.next();
            }

            item.setResultObject(messageDetailList);
            return item;

        } catch (Exception e) {
            Log.e("krm0219", "Custom_XmlPullParser.getMessageDetailList  Exception : " + e.toString());
        }

        return item;
    }


    public static MessageSendResult sendMessageResult(String object) {

        MessageSendResult item = null;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(object));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("StdCustomResultOfSentResultOfQPostsInQdrive")) {

                        item = new MessageSendResult();
                    } else if (sTag.equals("ResultCode")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setObject_resultCode(parser.getText());
                        }
                    } else if (sTag.equals("ResultMsg")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setObject_resultMsg(parser.getText());
                        }
                    }
                }

                eventType = parser.next();
            }

        } catch (Exception e) {
            Log.e("krm0219", "Custom_XmlPullParser.getCustomerMessageList  Exception : " + e.toString());
        }

        return item;
    }

    public static MessageQuestionNumberResult getQuestionNumber(String object) {

        MessageQuestionNumberResult item = null;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(object));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("StdCustomResultOfListOfGetDetailsQPostsInQdrive")) {

                        item = new MessageQuestionNumberResult();
                    } else if (sTag.equals("ResultCode")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultCode(parser.getText());
                        }
                    } else if (sTag.equals("ResultMsg")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultMsg(parser.getText());
                        }
                    } else if (sTag.equals("question_seq_no")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setQuestionNo(Integer.parseInt(parser.getText()));
                        }
                    }
                }

                eventType = parser.next();
            }

            return item;

        } catch (Exception e) {
            Log.e("krm0219", "Custom_XmlPullParser.getQuestionNumber  Exception : " + e.toString());
        }

        return item;
    }

    public static ServerResult getCommonServerResult(String object) {

        ServerResult item = null;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(object));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("StdResult")) {

                        item = new ServerResult();
                    } else if (sTag.equals("ResultCode")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultCode(parser.getText());
                        }
                    } else if (sTag.equals("ResultMsg")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultMsg(parser.getText());
                        }
                    }
                }

                eventType = parser.next();
            }

            return item;

        } catch (Exception e) {
            Log.e("krm0219", "Custom_XmlPullParser.getCommonServerResult  Exception : " + e.toString());
        }

        return item;
    }


    // TODO 2018.07.26
    /*
     if (parser.next() == XmlPullParser.TEXT)
     이걸로 먼저 크게 묶고 (START_TAG 안쪽으로)
     sTag 체크하는 걸로 바꿔보기!
     => 코드가 더 깔끔해 질 수 있음
     */

    public static NotInHousedResult getNotInHousedList(String object) {

        NotInHousedResult item = null;

        ArrayList<NotInHousedResult.NotInhousedList.NotInhousedSubList> notInhousedSubLists = new ArrayList<NotInHousedResult.NotInhousedList.NotInhousedSubList>();
        ArrayList<NotInHousedResult.NotInhousedList> notInhousedLists = new ArrayList<NotInHousedResult.NotInhousedList>();

        NotInHousedResult.NotInhousedList.NotInhousedSubList notInhousedSubListitem = null;
        NotInHousedResult.NotInhousedList notInhousedListitem = null;


        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(object));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("StdCustomResultOfListOfQdriveCheckNotInhousedPickupList")) {

                        item = new NotInHousedResult();
                    } else if (sTag.equals("ResultCode")) {

                        item.setResultCode(Integer.parseInt(parser.nextText()));
                    } else if (sTag.equals("ResultMsg")) {

                        item.setResultMsg(parser.nextText());
                    } else if (sTag.equals("QdriveCheckNotInhousedPickupList")) {

                        notInhousedListitem = new NotInHousedResult.NotInhousedList();
                    } else if (sTag.equals("invoice_no")) {

                        if (notInhousedListitem != null)
                            notInhousedListitem.setInvoiceNo(parser.nextText());
                    } else if (sTag.equals("req_nm")) {

                        if (notInhousedListitem != null)
                            notInhousedListitem.setReqName(parser.nextText());
                    } else if (sTag.equals("partner_id")) {

                        if (notInhousedListitem != null)
                            notInhousedListitem.setPartner_id(parser.nextText());
                    } else if (sTag.equals("zip_code")) {

                        if (notInhousedListitem != null)
                            notInhousedListitem.setZipCode(parser.nextText());
                    } else if (sTag.equals("address")) {

                        if (notInhousedListitem != null)
                            notInhousedListitem.setAddress(parser.nextText());
                    } else if (sTag.equals("pickup_cmpl_dt")) {

                        if (notInhousedListitem != null)
                            notInhousedListitem.setPickup_date(parser.nextText());
                    } else if (sTag.equals("real_qty")) {

                        if (notInhousedListitem != null)
                            notInhousedListitem.setReal_qty(parser.nextText());
                    } else if (sTag.equals("not_processed_qty")) {

                        if (notInhousedListitem != null)
                            notInhousedListitem.setNot_processed_qty(parser.nextText());
                    } else if (sTag.equals("qdriveOutstandingInhousedPickupLists")) {

                        notInhousedSubLists = new ArrayList<NotInHousedResult.NotInhousedList.NotInhousedSubList>();
                    } else if (sTag.equals("QdriveOutstandingInhousedPickupList")) {

                        notInhousedSubListitem = new NotInHousedResult.NotInhousedList.NotInhousedSubList();
                    } else if (sTag.equals("packing_no")) {

                        if (parser.next() == XmlPullParser.TEXT && notInhousedSubListitem != null) {
                            notInhousedSubListitem.setPackingNo(parser.getText());
                        }
                    } else if (sTag.equals("purchased_amt")) {

                        if (parser.next() == XmlPullParser.TEXT && notInhousedSubListitem != null)
                            notInhousedSubListitem.setPurchasedAmount(parser.getText());
                    } else if (sTag.equals("purchased_currency")) {

                        if (parser.next() == XmlPullParser.TEXT && notInhousedSubListitem != null)
                            notInhousedSubListitem.setPurchaseCurrency(parser.getText());
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("QdriveCheckNotInhousedPickupList") && notInhousedListitem != null) {

                        notInhousedLists.add(notInhousedListitem);
                        notInhousedListitem = null;
                    } else if (sTag.equals("qdriveOutstandingInhousedPickupLists")) {

                        if (notInhousedSubLists.size() == 0) {
                            notInhousedListitem.setNot_processed_qty("0");
                        } else {

                            notInhousedListitem.setSubLists(notInhousedSubLists);
                            notInhousedSubLists = null;
                        }
                    } else if (sTag.equals("QdriveOutstandingInhousedPickupList") && notInhousedSubListitem != null) {

                        notInhousedSubLists.add(notInhousedSubListitem);
                        notInhousedSubListitem = null;
                    }
                }

                eventType = parser.next();
            }

            item.setResultObject(notInhousedLists);
            return item;

        } catch (Exception e) {
            Log.e("krm0219", "Custom_XmlPullParser.getNotInHousedList  Exception : " + e.toString());
        }

        return item;
    }

    //NOTI  :  QR CODE URL DATA
    public static QRCodeResult getQRCodeData(String object) {

        QRCodeResult item = null;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(object));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("StdCustomResultOfString")) {

                        item = new QRCodeResult();
                    } else if (sTag.equals("ResultCode")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResult_code(parser.getText());
                        }
                    } else if (sTag.equals("ResultMsg")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResult_msg(parser.getText());
                        }
                    } else if (sTag.equals("ResultObject")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setQrcode_data(parser.getText());
                        }
                    }
                }

                eventType = parser.next();
            }

            return item;

        } catch (Exception e) {

            Log.e("krm0219", "Custom_XmlPullParser.getQRCodeData  Exception : " + e.toString());
        }

        return item;
    }

    //NOTI  :  OUTLET PICKUP DONE
    public static OutletPickupDoneResult getOutletPickupDoneData(String object) {

        OutletPickupDoneResult item = null;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(object));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("StdCustomResultOfPickupFromSevenEleven")) {

                        item = new OutletPickupDoneResult();
                    } else if (sTag.equals("ResultCode")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultCode(parser.getText());
                        }
                    } else if (sTag.equals("ResultMsg")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResultMsg(parser.getText());
                        }
                    } else if (sTag.equals("PickupNo")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setPickupNo(parser.getText());
                        }
                    } else if (sTag.equals("JobNumber")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setJobNumber(parser.getText());
                        }
                    } else if (sTag.equals("QRCode")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setQRCode(parser.getText());
                        }
                    } else if (sTag.equals("ListTrackingNo")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setTrackingNumbers(parser.getText());
                        }
                    }
                }

                eventType = parser.next();
            }

            return item;

        } catch (Exception e) {

            Log.e("krm0219", "Custom_XmlPullParser.getOutletPickupDoneData  Exception : " + e.toString());
        }

        return item;
    }

    //
    public static LockerUserInfoResult getLockerUserInfo(String object) {

        LockerUserInfoResult item = null;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(object));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String sTag = parser.getName();

                    if (sTag.equals("StdCustomResultOfDataTable")) {

                        item = new LockerUserInfoResult();
                    } else if (sTag.equals("ResultCode")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResult_code(parser.getText());
                        }
                    } else if (sTag.equals("ResultMsg")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setResult_msg(parser.getText());
                        }
                    } else if (sTag.equals("hp_no")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setUser_mobile(parser.getText());
                        }
                    } else if (sTag.equals("lsp_user_status")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setUser_status(parser.getText());
                        }
                    } else if (sTag.equals("lsp_user_key")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setUser_key(parser.getText());
                        }
                    } else if (sTag.equals("lsp_user_expired_date")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setUser_expiry_date(parser.getText());
                        }
                    } else if (sTag.equals("lst_user_id")) {
                        if (parser.next() == XmlPullParser.TEXT) {

                            item.setUser_id(parser.getText());
                        }
                    }
                }

                eventType = parser.next();
            }

        } catch (Exception e) {
            Log.e("krm0219", "Custom_XmlPullParser.getCustomerMessageList  Exception : " + e.toString());
        }

        return item;
    }
}
