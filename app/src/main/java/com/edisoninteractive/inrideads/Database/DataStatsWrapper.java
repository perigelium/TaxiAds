package com.edisoninteractive.inrideads.Database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import com.edisoninteractive.inrideads.Database.DatabaseSchema.StatsTable.Cols;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;

/**
 * Created by mdumik on 18.12.2017.
 */

public class DataStatsWrapper extends CursorWrapper
{

    Cursor cursor;

    public DataStatsWrapper(Cursor cursor)
    {
        super(cursor);
        this.cursor = cursor;
    }

    public DataStats getStats()
    {
        try
        {
            int id;
            try
            {
                id = getInt(getColumnIndex(Cols.ID));
            } catch (Exception ex)
            {
                try
                {
                    id = getInt(getColumnIndex("_id"));
                } catch (Exception e)
                {
                    id = 0;
                }
            }
            String statsType = getString(getColumnIndex(Cols.STATS_TYPE));
            String statsId = getString(getColumnIndex(Cols.STATS_ID));
            int count = getInt(getColumnIndex(Cols.COUNT));
            String date = getString(getColumnIndex(Cols.DATE));
            String sent = getString(getColumnIndex(Cols.SENT));
            String unitId = getString(getColumnIndex(Cols.UNIT_ID));
            String campaignId = getString(getColumnIndex(Cols.CAMPAIGN_ID));
            String details = getString(getColumnIndex(Cols.DETAILS));
            String time = getString(getColumnIndex(Cols.TIME));
            String lat = getString(getColumnIndex(Cols.LAT));
            String lon = getString(getColumnIndex(Cols.LON));
            String timeStamp = getString(getColumnIndex(Cols.TIMESTAMP));
            String timeFull = getString(getColumnIndex(Cols.TIME_FULL));

            if (null == statsId || statsId.isEmpty())
            {
                statsId = "null";
            }

            if (null == campaignId || campaignId.isEmpty())
            {
                campaignId = "null";
            }

            if (null == details || details.isEmpty())
            {
                details = "null";
            }

            return new DataStats(id, statsType, statsId, count, date, Integer.parseInt(sent), unitId, campaignId, details, time, lat, lon, timeStamp, timeFull);

        } catch (Exception ex)
        {
            Log.d(GlobalConstants.APP_LOG_TAG, "Database/DataStatsWrapper: getStats() EXCEPTION " + ex.getMessage());
            return null;
        }/* finally
        {
            if(cursor != null)
                cursor.close();
        }*/
    }
}
