package com.edisoninteractive.inrideads.Utils;

import android.util.Log;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;

import java.io.File;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;

/**
 * Created by mdumik on 1/26/2018.
 */

public class DirectoryUtils
{
    private static final String className = DirectoryUtils.class.getSimpleName();

    private static final String configsDirectory = DATA_PATH + File.separator + "configs";
    private static final String configJs = configsDirectory + File.separator + "config.js";
    private static final String adscheduleJs = configsDirectory + File.separator + "adschedule.js";

    private static final String mediaDirectory = DATA_PATH + File.separator + "media";
    private static final String rssDirectory = DATA_PATH + File.separator + "rss";
    private static final String dbDirectory = DATA_PATH + File.separator + "db";
    private static final String tempDirectory = DATA_PATH + File.separator + "temp";

    private static final String[] additionalDirectories = new String[]{mediaDirectory, rssDirectory, dbDirectory, tempDirectory};

    public boolean doesNecessaryDirectoriesExists()
    {
        return doesFileExist(configsDirectory) && doesFileExist(configJs) && doesFileExist(adscheduleJs);
    }

    private boolean doesFileExist(String directoryName)
    {
        File file = new File(directoryName);

        if (file.exists())
        {
            return true;
        }
        return false;
    }

    public void checkAndAutoCreateAdditionalDirectories()
    {
        for (String directory : additionalDirectories)
        {
            if (!doesFileExist(directory))
            {
                createDirectory(directory);
            }
        }
    }

    private void createDirectory(String directoryName)
    {
        File dir = new File(directoryName);
        try
        {
            if (dir.mkdir())
            {
                Log.d(GlobalConstants.APP_LOG_TAG, className + ": DirectoryUtils: created " + directoryName + " directory");
            } else
            {
                Log.d(GlobalConstants.APP_LOG_TAG, className + ": DirectoryUtils: can't create " + directoryName + " directory");
            }
        } catch (Exception e)
        {
            Log.d(GlobalConstants.APP_LOG_TAG, className + ": DirectoryUtils: Exception creating directory " + directoryName + ", " + e.getMessage());
            e.printStackTrace();
        }
    }
}
