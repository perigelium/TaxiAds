package com.edisoninteractive.inrideads.Database;

/**
 * Created by mdumik on 18.12.2017.
 */

public class DatabaseSchema {

    public static final class StatsTable {

        public static final String NAME = "stats";

        public static final class Cols {

            public static final String ID = "id";
            public static final String STATS_TYPE = "stats_type";
            public static final String STATS_ID = "stats_id";
            public static final String COUNT = "count";
            public static final String DATE = "date";
            public static final String SENT = "sent";
            public static final String UNIT_ID = "unit_id";
            public static final String CAMPAIGN_ID = "campaign_id";
            public static final String DETAILS = "details";
            public static final String TIME = "time";
            public static final String LAT = "lat";
            public static final String LON = "lon";
            public static final String TIMESTAMP = "timestamp";
            public static final String TIME_FULL = "time_full";
        }
    }
}
