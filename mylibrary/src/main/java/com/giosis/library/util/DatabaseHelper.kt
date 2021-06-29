package com.giosis.library.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper private constructor(private val mContext: Context) : SQLiteOpenHelper(mContext, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val TAG = "DatabaseHelper"

        private var mInstance: DatabaseHelper? = null
        private var sqLiteDatabase: SQLiteDatabase? = null

        private const val DB_VERSION = 3
        private const val DB_NAME = "QdriveDB.db"

        const val DB_TABLE_INTEGRATION_LIST = "INTEGRATION_LIST"
        const val DB_TABLE_REST_DAYS = "REST_DAYS"

        private const val CREATE_TABLE_INTEGRATION_LIST = "CREATE TABLE IF NOT EXISTS " +
                DB_TABLE_INTEGRATION_LIST + "(contr_no unique, partner_ref_no, " +
                "invoice_no, stat, tel_no, hp_no, zip_code, address, self_memo, type, route, " +
                "sender_nm, rcv_nm, rcv_request, desired_date, req_qty, req_nm, " +
                "delivery_dt, chg_dt, reg_dt, reg_id, " +
                "real_qty, retry_dt, driver_memo, fail_reason, desired_time, " +
                "rcv_type, punchOut_stat, partner_id, cust_no, secret_no_type, secret_no, lat, lng, " +
                "secure_delivery_yn, parcel_amount, currency, order_type_etc, " +
                "high_amount_yn, state, city, street)"

        private const val CREATE_TABLE_REST_DAYS = "CREATE TABLE IF NOT EXISTS " +
                DB_TABLE_REST_DAYS + "(rest_dt, title)"


        @Volatile
        private var instance: DatabaseHelper? = null

        @JvmStatic
        fun getInstance(context: Context): DatabaseHelper =
                instance ?: synchronized(this) {
                    instance ?: DatabaseHelper(context).also {
                        instance = it
                        sqLiteDatabase = it.writableDatabase
                    }
                }

        // kjyoo 추후 컨텍스트 없을경우 처리 어떻게 해야 할지
        @JvmStatic
        fun getInstance(): DatabaseHelper = instance!!

    }


    // 최초 DB를 만들 때 한번만 호출!
    // db.getWritableDatabase()  / db.getReadableDatabase()  호출될 때 호출됨
    override fun onCreate(db: SQLiteDatabase) {
        Log.e(TAG, "onCreate")
        db.execSQL(CREATE_TABLE_INTEGRATION_LIST)
        db.execSQL(CREATE_TABLE_REST_DAYS)
    }

    // 버전이 업데이트 되었을 때 DB 재생성
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.e(TAG, "onUpgrade  $oldVersion > $newVersion")

        // 필요없는 db column 정리 후 첫 업그레이드
        db.execSQL("DROP TABLE IF EXISTS $DB_TABLE_INTEGRATION_LIST")
        db.execSQL("DROP TABLE IF EXISTS $DB_TABLE_REST_DAYS")
        onCreate(db)
    }

    val dbPath: String
        get() = mContext.getDatabasePath(DB_NAME).toString()

    /***
     * Method to insert record
     * @param table     : table name
     * @param values    : ContentValues instance
     * @return          : long (rowid)
     */
    fun insert(table: String?, values: ContentValues?): Long {
        return sqLiteDatabase!!.insert(table, null, values)
    }

    /****
     * Method for select statements
     * @param sql       : sql statements
     * @return          : cursor
     */
    operator fun get(sql: String?): Cursor {
        return sqLiteDatabase!!.rawQuery(sql, null)
    }

    /***
     * Method to update record
     * @param table         : table name
     * @param values        : ContentValues instance
     * @param whereClause   : Where Clause
     * @return              ; int
     */
    fun update(table: String?, values: ContentValues?, whereClause: String?, whereArgs: Array<String?>?): Int {
        return sqLiteDatabase!!.update(table, values, whereClause, whereArgs)
    }

    /***
     * Method to delete record
     * @param table         : table name
     * @param whereClause   : Where Clause
     * @return              : int
     */
    fun delete(table: String?, whereClause: String?): Int {
        return sqLiteDatabase!!.delete(table, whereClause, null)
    }

    /***
     * Method to close database & instance null
     */
    override fun close() {
        if (mInstance != null) {
            Log.e(TAG, "instance of database (" + DB_NAME + ") close !")
            sqLiteDatabase!!.close()
            mInstance = null
        }
    }

}