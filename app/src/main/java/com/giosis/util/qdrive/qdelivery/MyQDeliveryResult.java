package com.giosis.util.qdrive.qdelivery;

import java.util.ArrayList;

/**
 * @author krm0219
 */
public class MyQDeliveryResult {

    private int resultCode = -1;
    private String resultMsg = "";
    private ArrayList<MYQDeliveryItem> myqDeliveryItemArrayList;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public ArrayList<MYQDeliveryItem> getMyqDeliveryItemArrayList() {
        return myqDeliveryItemArrayList;
    }

    public void setMyqDeliveryItemArrayList(ArrayList<MYQDeliveryItem> myqDeliveryItemArrayList) {
        this.myqDeliveryItemArrayList = myqDeliveryItemArrayList;
    }

    public static class MYQDeliveryItem {

        String pickupNo;
        String orderNo;
        String registeDate;
        String status;
        String itemName;
        String itemPrice;
        String shippingCost;

        public MYQDeliveryItem() {
        }

        public MYQDeliveryItem(String pickupNo, String orderNo, String registeDate, String status, String itemName, String itemPrice, String shippingCost) {
            this.pickupNo = pickupNo;
            this.orderNo = orderNo;
            this.registeDate = registeDate;
            this.status = status;
            this.itemName = itemName;
            this.itemPrice = itemPrice;
            this.shippingCost = shippingCost;
        }

        public String getPickupNo() {
            return pickupNo;
        }

        public void setPickupNo(String pickupNo) {
            this.pickupNo = pickupNo;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public String getRegisteDate() {
            return registeDate;
        }

        public void setRegisteDate(String registeDate) {
            this.registeDate = registeDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getItemPrice() {
            return itemPrice;
        }

        public void setItemPrice(String itemPrice) {
            this.itemPrice = itemPrice;
        }

        public String getShippingCost() {
            return shippingCost;
        }

        public void setShippingCost(String shippingCost) {
            this.shippingCost = shippingCost;
        }
    }
}