package com.edisoninteractive.inrideads.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.edisoninteractive.inrideads.Database.DatabaseSchema.StatsTable.Cols;

import java.util.ArrayList;
import java.util.List;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;

/**
 * Created by mdumik on 18.12.2017.
 */

public class DatabaseManager
{

    private Context context;
    private static DatabaseManager instance;
    private SQLiteDatabase database;

    private DatabaseManager(Context context)
    {
        this.context = context;
        this.database = new DatabaseHelper(context).getWritableDatabase();
    }

    public static synchronized DatabaseManager get(Context context)
    {
        if (instance == null)
        {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    public void writeStats(DataStats stats)
    {
        if (null == stats)
        {
            return;
        }
        ContentValues values = getStatsContentValues(stats);
        //Log.d(GlobalConstants.APP_LOG_TAG, "Database/DatabaseManager: writeStats() trying to insert stats to DB");
        try
        {
            long result = database.insert(DatabaseSchema.StatsTable.NAME, null, values);

            if (result != -1)
            {
                Log.d(APP_LOG_TAG, "Database/DatabaseManager: writeStats() stats inserted successfully!");
            } else
            {
                Log.d(APP_LOG_TAG, "Database/DatabaseManager: writeStats() stats was not inserted to DB for some reason");
            }

        } catch (Exception e)
        {
            Log.d(APP_LOG_TAG, "Database/DatabaseManager: writeStats() EXCEPTION " + "reason = " + e.getMessage());

            try
            {
                this.database = new DatabaseHelper(context).getWritableDatabase();
            } catch (Exception ex)
            {
                Log.d(APP_LOG_TAG, "Database/DatabaseManager: writeStats() unable to get " + "writable database. Database journal may be missing in the db directory " + ex.getMessage());
            }
        }
    }

    public void updateStats(DataStats stats)
    {
        ContentValues values = getStatsContentValues(stats);
        database.update(DatabaseSchema.StatsTable.NAME, values, Cols.TIMESTAMP + " = ?", new String[]{stats.getTimeStamp()});
    }

    public void deleteStat(DataStats stat)
    {
        database.delete(DatabaseSchema.StatsTable.NAME, Cols.TIMESTAMP + " = ?", new String[]{stat.getTimeStamp()});
    }

    public void deleteSentStats()
    {
        try
        {
            database.delete(DatabaseSchema.StatsTable.NAME, Cols.SENT + " = ?", new String[]{"2"});
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public DataStats getStatsByTimestamp(String timestamp)
    {

        DataStatsWrapper cursor = queryStats(Cols.TIMESTAMP + " = ?", new String[]{timestamp});

        try
        {
            if (cursor.getCount() == 0)
            {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getStats();

        } finally
        {
            cursor.close();
        }
    }

    public List<DataStats> getUnsentStats()
    {
        List<DataStats> statsList = new ArrayList<>();
        DataStatsWrapper cursor = null;

        try
        {
            cursor = queryStats(Cols.SENT + " = ?", new String[]{"0"});

            if (cursor.getCount() == 0)
            {
                return null;
            }

            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                if (null != cursor.getStats())
                {
                    statsList.add(cursor.getStats());
                }
                cursor.moveToNext();
            }
        } catch (Exception e)
        {
            Log.w(APP_LOG_TAG, getClass().getSimpleName() + ": getUnsentStats error, " + e.getMessage());
        }

        if(cursor != null)
        {
            cursor.close();
        }

        return statsList;
    }

    public List<DataStats> getAllStats()
    {

        List<DataStats> statsList = new ArrayList<>();

        DataStatsWrapper cursor = queryStats(null, null);

        if (cursor.getCount() == 0)
        {
            return null;
        }

        try
        {
            cursor.moveToFirst();

            while (!cursor.isAfterLast())
            {
                if (null != cursor.getStats())
                {
                    statsList.add(cursor.getStats());
                }
                cursor.moveToNext();
            }

        } finally
        {
            cursor.close();
        }
        return statsList;
    }

    private DataStatsWrapper queryStats(String whereClause, String[] whereArgs)
    {

        Cursor cursor = database.query(DatabaseSchema.StatsTable.NAME, null, whereClause, whereArgs, null, null, null);
        return new DataStatsWrapper(cursor);
    }

    private ContentValues getStatsContentValues(DataStats stats)
    {

        ContentValues cv = new ContentValues();

        cv.put(Cols.STATS_TYPE, stats.getStatsType());
        cv.put(Cols.STATS_ID, stats.getStatsId());
        cv.put(Cols.COUNT, stats.getCount());
        cv.put(Cols.DATE, stats.getDate());
        cv.put(Cols.SENT, String.valueOf(stats.getSent()));
        cv.put(Cols.UNIT_ID, stats.getUnitId());
        cv.put(Cols.CAMPAIGN_ID, stats.getCampaignId());
        cv.put(Cols.DETAILS, stats.getDetails());
        cv.put(Cols.TIME, stats.getTime());
        cv.put(Cols.LAT, stats.getLat());
        cv.put(Cols.LON, stats.getLon());
        cv.put(Cols.TIMESTAMP, stats.getTimeStamp());
        cv.put(Cols.TIME_FULL, stats.getTimeFull());

        return cv;
    }
}
