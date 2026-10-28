
package com.example.trafficfinemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TrafficFines.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "fines";
    public static final String COL_ID = "id";
    public static final String COL_VEHICLE = "vehicle";
    public static final String COL_VIOLATION = "violation";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_DATE = "fine_date";
    public static final String COL_STATUS = "status";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_VEHICLE + " TEXT NOT NULL, " +
                COL_VIOLATION + " TEXT NOT NULL, " +
                COL_AMOUNT + " REAL NOT NULL, " +
                COL_DATE + " TEXT NOT NULL, " +
                COL_STATUS + " TEXT NOT NULL)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        // Add migration code here if the database schema changes.
    }

    public boolean insertFine(String vehicle, String violation,
                              double amount, String date, String status) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_VEHICLE, vehicle);
        values.put(COL_VIOLATION, violation);
        values.put(COL_AMOUNT, amount);
        values.put(COL_DATE, date);
        values.put(COL_STATUS, status);

        return db.insert(TABLE_NAME, null, values) != -1;
    }

    public Cursor getAllFines() {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_NAME, null, null, null, null, null,
                COL_ID + " DESC"
        );
    }

    public Cursor getFineById(int id) {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_NAME,
                null,
                COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );
    }

    public boolean updateFine(int id, String vehicle, String violation,
                              double amount, String date, String status) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_VEHICLE, vehicle);
        values.put(COL_VIOLATION, violation);
        values.put(COL_AMOUNT, amount);
        values.put(COL_DATE, date);
        values.put(COL_STATUS, status);

        int rowsUpdated = db.update(
                TABLE_NAME,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        return rowsUpdated > 0;
    }
}
