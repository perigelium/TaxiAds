package com.edisoninteractive.inrideads.Database;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

/**
 * Created by mdumik on 18.12.2017.
 */

public class DatabaseContext extends ContextWrapper
{

    private static final String DB_PATH = Environment.getExternalStorageDirectory() + "/TaxiInteractive" + "/db";

    public DatabaseContext(Context base)
    {
        super(base);
    }

    @Override
    public File getDatabasePath(String name)
    {

//        String dbfile = DB_PATH + File.separator + name;
        String dbfile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "TaxiInteractive" + File.separator + "db" + File.separator + name;

//        if (!dbfile.endsWith(".db")) {
//            dbfile += ".db" ;
//        }

        File result = new File(dbfile);

        if (!result.getParentFile().exists())
        {
            // todo: send error msg to sender
            return null;
        }

/*        if (Log.isLoggable(APP_LOG_TAG, Log.WARN)) {
            Log.w(APP_LOG_TAG, "getDatabasePath(" + name + ") = " + result.getAbsolutePath());
        }

        Log.d("dbTest", "getDatabasePath(" + name + ") = " + result.getAbsolutePath());*/

        return result;
    }

    /* this version is called for android devices >= api-11. */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler)
    {
        return openOrCreateDatabase(name, mode, factory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory)
    {
        File file = getDatabasePath(name);

        if(file == null)
        {
            return null;
        }

        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(file, null);
        // SQLiteDatabase result = super.openOrCreateDatabase(name, mode, factory);
/*        if (Log.isLoggable(APP_LOG_TAG, Log.WARN)) {
            Log.w(APP_LOG_TAG, "openOrCreateDatabase(" + name + ") = " + result.getPath());
        }*/
        return result;
    }
}
