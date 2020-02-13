package org.apache.cordova.sqlite;

import java.sql.SQLException;

public class SQLiteConnector implements SQLiteConnectionFactory {

    static boolean isLibLoaded = false;

    public SQLiteConnector() {
        if (!isLibLoaded) {
            System.loadLibrary("sqlc-native-driver");

            if (SQLiteNative.sqlc_api_version_check(SQLiteNative.SQLC_API_VERSION) != SQLCode.OK) {
                throw new RuntimeException("native library version mismatch");
            }

            isLibLoaded = true;
        }
    }

    public SQLiteConnection newSQLiteConnection(String filename, int flags) throws SQLException {
        return new SQLiteGlueConnection(filename, flags);
    }
}
