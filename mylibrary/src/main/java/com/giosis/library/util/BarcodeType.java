package com.giosis.library.util;

public class BarcodeType {

    public static final String TYPE_DELIVERY = "D";
    public static final String TYPE_PICKUP = "P";
    public static final String TYPE_CNR = "CnR";
    public static final String PICKUP_ZERO_QTY = "PZQ";


    // CaptureActivity
    public static final String CONFIRM_MY_DELIVERY_ORDER = "MDA";       // Main & SCAN > Confirm my delivery order
    public static final String CHANGE_DELIVERY_DRIVER = "CDR";          // Main > Change Delivery Driver

    public static final String DELIVERY_DONE = "D4";                    // SCAN > Delivery Done
    public static final String PICKUP_CNR = "CNR";                      // SCAN > Pickup C&R Parcels
    public static final String SELF_COLLECTION = "SEC";                 // SCAN > Self-Collection

    public static final String PICKUP_SCAN_ALL = "PSA";                 // LIST > Start to Scan
    public static final String PICKUP_ADD_SCAN = "PAS";                 // LIST > Today Done > Add Scan
    public static final String OUTLET_PICKUP_SCAN = "OPS";              // LIST > Outlet Pickup Scan
    public static final String PICKUP_TAKE_BACK = "PTB";                // LIST > Today Done > Take Back
}