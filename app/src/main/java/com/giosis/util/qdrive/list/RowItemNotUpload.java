package com.giosis.util.qdrive.list;

import com.giosis.util.qdrive.list.ChildItemNotUpload;

import java.util.ArrayList;

public class RowItemNotUpload {
	//private String title;
    //private int icon;
	
	private String stat;
	private String shipping ;
	private String name;
	private String address;
	private String request;
	private String type;
	private String route;
	private String sender;
	
    private ArrayList<ChildItemNotUpload> childItems;
	

    public RowItemNotUpload(String stat, String shipping, String name, String address, String request, String type, String route, String sender) {
    	this.stat = stat;
        this.shipping = shipping;
        this.name = name;
        this.address = address;
        this.request = request;
        this.type = type;
        this.route= route;
        this.sender = sender;
    }
    public ArrayList<ChildItemNotUpload> getItems() {
        return childItems;
    }
    public void setItems(ArrayList<ChildItemNotUpload> Items) {
        this.childItems = Items;
    }
    public String getStat() {
        return stat;
    }
    public void setStat(String stat) {
        this.stat = stat;
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
}
