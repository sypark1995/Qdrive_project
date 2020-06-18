package com.giosis.util.qdrive.list;

import java.util.ArrayList;

public class RowItem {

    private String contr_no;
    private String delay;
    private String shipping;
    private String name;
    private String address;
    private String request;
    private String type;
    private String route;
    private String sender;
    private String desired_date;
    private String qty;
    private String self_memo;
    private String partner_id;

    private double lat;
    private double lng;
    private String stat;
    private String cust_no;

    private ArrayList<ChildItem> childItems;

    private String secure_delivery_yn;
    private String parcel_amount;
    private String currency;

    // krm0219
    private String order_type_etc;
    private int outlet_qty;
    private String outlet_company;
    private String outlet_store_code;
    private String outlet_store_name;
    private String outlet_operation_hour;

    private String desired_time;
    private String zip_code;
    private String ref_pickup_no;


    // 2020.06  Trip 단위 묶음
    private int tripNo;
    private boolean primaryKey;
    private ArrayList<RowItem> tripDataArrayList;

    float distance;


    public RowItem(String contr_no, String delay, String shipping, String name, String address, String request, String type, String route, String sender, String desired_date, String qty, String self_memo, double lat, double lng, String stat, String cust_no, String partner_id
            , String secure_delivery_yn, String parcel_amount, String currency) {
        this.contr_no = contr_no;
        this.delay = delay;
        this.shipping = shipping;
        this.name = name;
        this.address = address;
        this.request = request;
        this.type = type;
        this.route = route;
        this.sender = sender;
        this.desired_date = desired_date;
        this.qty = qty;
        this.self_memo = self_memo;
        this.lat = lat;
        this.lng = lng;
        this.stat = stat;
        this.cust_no = cust_no;
        this.partner_id = partner_id;
        this.secure_delivery_yn = secure_delivery_yn;
        this.parcel_amount = parcel_amount;
        this.currency = currency;
    }

    public ArrayList<ChildItem> getItems() {
        return childItems;
    }

    public void setItems(ArrayList<ChildItem> Items) {
        this.childItems = Items;
    }

    public String getContrNo() {
        return contr_no;
    }

    public void setContrNo(String contr_no) {
        this.contr_no = contr_no;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getRequest() {
        return request;
    }

    public String getShipping() {
        return shipping;
    }

    public void setShipping(String shipping) {
        this.shipping = shipping;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getDesiredDate() {
        return desired_date;
    }

    public void setDesiredDate(String desired_date) {
        this.desired_date = desired_date;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getSelfMemo() {
        return self_memo;
    }

    public void setSelfMemo(String self_memo) {
        this.self_memo = self_memo;
    }
    /*
    public String getZipcode() {
        return zipcode;
    }
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }*/

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getCustNo() {
        return cust_no;
    }

    public void setCustNo(String cust_no) {
        this.cust_no = cust_no;
    }

    public String getPartnerID() {
        return partner_id;
    }

    public void setPartnerID(String partner_id) {
        this.partner_id = partner_id;
    }

    public String getSecure_delivery_yn() {
        return secure_delivery_yn;
    }

    public String getParcel_amount() {
        return parcel_amount;
    }

    public String getCurrency() {
        return currency;
    }


    public String getOrder_type_etc() {
        return order_type_etc;
    }

    public void setOrder_type_etc(String order_type_etc) {
        this.order_type_etc = order_type_etc;
    }

    public int getOutlet_qty() {
        return outlet_qty;
    }

    public void setOutlet_qty(int outlet_qty) {
        this.outlet_qty = outlet_qty;
    }

    public String getOutlet_company() {
        return outlet_company;
    }

    public void setOutlet_company(String outlet_company) {
        this.outlet_company = outlet_company;
    }

    public String getOutlet_store_code() {
        return outlet_store_code;
    }

    public void setOutlet_store_code(String outlet_store_code) {
        this.outlet_store_code = outlet_store_code;
    }

    public String getOutlet_store_name() {
        return outlet_store_name;
    }

    public void setOutlet_store_name(String outlet_store_name) {
        this.outlet_store_name = outlet_store_name;
    }

    public String getDesired_time() {
        return desired_time;
    }

    public void setDesired_time(String desired_time) {
        this.desired_time = desired_time;
    }

    public String getOutlet_operation_hour() {
        return outlet_operation_hour;
    }

    public void setOutlet_operation_hour(String outlet_operation_hour) {
        this.outlet_operation_hour = outlet_operation_hour;
    }

    public String getZip_code() {
        return zip_code;
    }

    public void setZip_code(String zip_code) {
        this.zip_code = zip_code;
    }

    public String getRef_pickup_no() {
        return ref_pickup_no;
    }

    public void setRef_pickup_no(String ref_pickup_no) {
        this.ref_pickup_no = ref_pickup_no;
    }



    public int getTripNo() {
        return tripNo;
    }

    public void setTripNo(int tripNo) {
        this.tripNo = tripNo;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    // primary key 'Y' 일 경우 해당 Trip에 묶인 데이터 리스트
    public ArrayList<RowItem> getTripDataArrayList() {
        return tripDataArrayList;
    }

    public void setTripDataArrayList(ArrayList<RowItem> tripDataArrayList) {
        this.tripDataArrayList = tripDataArrayList;
    }

    // 드라이버 위치와의 거리
    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }


}