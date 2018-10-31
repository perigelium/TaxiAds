package com.edisoninteractive.inrideads.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.edisoninteractive.inrideads.Database.DatabaseSchema.StatsTable.Cols;

/**
 * Created by mdumik on 18.12.2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "ti_stats.sqlite";
    private static final int DB_VERSION = 2;

    private static final String CREATE_STATISTIC_TABLE = " CREATE TABLE IF NOT EXISTS "
            + DatabaseSchema.StatsTable.NAME
            + "("
            + "_id integer primary key autoincrement, "
            + Cols.STATS_TYPE + ", "
            + Cols.STATS_ID + ", "
            + Cols.COUNT + ", "
            + Cols.DATE + ", "
            + Cols.SENT + ", "
            + Cols.UNIT_ID + ", "
            + Cols.CAMPAIGN_ID + ", "
            + Cols.DETAILS + ", "
            + Cols.TIME + ", "
            + Cols.LAT + ", "
            + Cols.LON + ", "
            + Cols.TIMESTAMP + ", "
            + Cols.TIME_FULL
            + ")";

    public DatabaseHelper(final Context context) {
        super(new DatabaseContext(context), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        checkIfTimeFullColumnExists(db);
        checkIfTimestampColumnExists(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STATISTIC_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseSchema.StatsTable.NAME);
            onCreate(db);
        }
    }

    private void checkIfTimeFullColumnExists(SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseSchema.StatsTable.NAME, null, null, null, null, null, null);
        if (cursor.getColumnIndex(Cols.TIME_FULL) == -1) {
            addTimeFullColumnToDB(db);
            cursor.close();
        }
    }

    private void checkIfTimestampColumnExists(SQLiteDatabase db){
        Cursor cursor = db.query(DatabaseSchema.StatsTable.NAME, null, null, null, null, null, null);
        if(cursor.getColumnIndex(Cols.TIMESTAMP) == -1){
            db.execSQL("ALTER TABLE "+ DatabaseSchema.StatsTable.NAME + " ADD COLUMN " + Cols.TIMESTAMP);
        }
    }

    private void addTimeFullColumnToDB(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + DatabaseSchema.StatsTable.NAME + " ADD COLUMN "
                + Cols.TIME_FULL);
    }
}
