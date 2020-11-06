package com.giosis.util.qdrive.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.giosis.util.qdrive.international.MyApplication;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String TAG = "DatabaseHelper";

    private static DatabaseHelper mInstance = null;
    private static SQLiteDatabase sqLiteDatabase;
    private final Context mContext;

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "QdriveDB.db";

    public static final String DB_TABLE_INTEGRATION_LIST = "INTEGRATION_LIST";
    public static final String DB_TABLE_REST_DAYS = "REST_DAYS";
    public static final String DB_TABLE_SCAN_DELIVERY = "SCAN_DELIVERY";

    private static final String CREATE_TABLE_INTEGRATION_LIST = "CREATE TABLE IF NOT EXISTS " +
            DB_TABLE_INTEGRATION_LIST + "(contr_no unique, seq_orderby, partner_ref_no, " +
            "invoice_no, stat, tel_no, hp_no, zip_code, address, self_memo, type, route, " +
            "sender_nm, rcv_nm, rcv_request,  desired_date, req_qty, req_nm, " +
            "failed_count, delivery_dt, delivery_cnt, chg_dt, chg_id, reg_dt, reg_id, " +
            "real_qty, retry_dt, driver_memo, fail_reason, desired_time, " +
            "rcv_type, punchOut_stat, partner_id, cust_no, secret_no_type, secret_no, lat, lng , " +
            "secure_delivery_yn, parcel_amount, currency, order_type_etc)";

    private static final String CREATE_TABLE_REST_DAYS = "CREATE TABLE IF NOT EXISTS " +
            DB_TABLE_REST_DAYS + "(rest_dt, title)";

    private static final String CREATE_TABLE_SCAN_DELIVERY = "CREATE TABLE IF NOT EXISTS " +
            DB_TABLE_SCAN_DELIVERY + "(contr_no, invoice_no, stat, punchOut_stat, chg_id, chg_dt, reg_id, reg_dt, " +
            "partner_ref_no,  rcv_nm, sender_nm, tel_no, hp_no, zip_code, address, fail_reason, del_memo, " +
            "rcv_type, driver_memo, delivery_dt, delivery_cnt)";

    // LazyHolder 사용
    public static DatabaseHelper getInstance() {

        if (mInstance == null) {

            Log.e(TAG, "DB getInstance");
            mInstance = LazyHolder.INSTANCE;
            sqLiteDatabase = mInstance.getWritableDatabase();
        }

        return mInstance;
    }

    private static class LazyHolder {

        private static final DatabaseHelper INSTANCE = new DatabaseHelper(MyApplication.getContext());
    }


    private DatabaseHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        this.mContext = context;
    }

    // 최초 DB를 만들 때 한번만 호출!
    // db.getWritableDatabase()  / db.getReadableDatabase()  호출될 때 호출됨
    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.e(TAG, "onCreate");
        db.execSQL(CREATE_TABLE_INTEGRATION_LIST);
        db.execSQL(CREATE_TABLE_REST_DAYS);
        db.execSQL(CREATE_TABLE_SCAN_DELIVERY);
    }

    // 버전이 업데이트 되었을 때 DB 재생성
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.e(TAG, "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_INTEGRATION_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_REST_DAYS);
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_SCAN_DELIVERY);

        onCreate(db);
    }


    public String getDbPath() {

        return String.valueOf(mContext.getDatabasePath(DB_NAME));
    }


    /***
     * Method to insert record
     * @param table     : table name
     * @param values    : ContentValues instance
     * @return          : long (rowid)
     */
    public long insert(String table, ContentValues values) {
        return sqLiteDatabase.insert(table, null, values);
    }

    /****
     * Method for select statements
     * @param sql       : sql statements
     * @return          : cursor
     */
    public Cursor get(String sql) {
        return sqLiteDatabase.rawQuery(sql, null);
    }

    /***
     * Method to update record
     * @param table         : table name
     * @param values        : ContentValues instance
     * @param whereClause   : Where Clause
     * @return              ; int
     */
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return sqLiteDatabase.update(table, values, whereClause, whereArgs);
    }

    /***
     * Method to delete record
     * @param table         : table name
     * @param whereClause   : Where Clause
     * @return              : int
     */
    public int delete(String table, String whereClause) {


        return sqLiteDatabase.delete(table, whereClause, null);
    }

    /***
     * Method to close database & instance null
     */
    @Override
    public void close() {

        if (mInstance != null) {
            Log.e(TAG, "instance of database (" + DB_NAME + ") close !");
            sqLiteDatabase.close();
            mInstance = null;
        }
    }
}