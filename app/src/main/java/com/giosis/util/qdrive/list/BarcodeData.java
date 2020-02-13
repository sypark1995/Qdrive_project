package com.giosis.util.qdrive.list;

public class BarcodeData {
    private String barcode;
    private String state;               // type : 'D', 'P'... 등등~

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
