package com.edisoninteractive.inrideads.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.edisoninteractive.inrideads.Utils.NetworkUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;

/**
 * Created by Alex Angan one fine day
 */

public class DownloadFileService extends IntentService
{
    private static final String DOWNLOAD_FILE = "com.edisoninteractive.inrideads.Services.action.DownloadFile";

    private static final String URL = "com.edisoninteractive.inrideads.Services.extra.Url";
    private static final String FILE_PATH = "com.edisoninteractive.inrideads.Services.extra.FilePath";

    public DownloadFileService()
    {
        super("DownloadFileService");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        setIntentRedelivery(true);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            final String action = intent.getAction();

            if (DOWNLOAD_FILE.equals(action))
            {
                while(!NetworkUtils.isNetworkAvailable(getApplicationContext()))
                {
                    try
                    {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                final String url = intent.getStringExtra(URL);
                final String filePath = intent.getStringExtra(FILE_PATH);
                File fileToDownload = new File(filePath);

                if(fileToDownload.exists())
                {
                    Log.i(APP_LOG_TAG, getClass().getSimpleName() + "Download skipped, file exists " + filePath);
                    return;
                }

                if (!downloadFile(url, filePath))
                {
                    Log.w(APP_LOG_TAG, getClass().getSimpleName() + "Download file failed " + filePath);

                    try
                    {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    Intent downIntent = new Intent(getApplicationContext(), DownloadFileService.class);
                    downIntent.setAction("com.edisoninteractive.inrideads.Services.action.DownloadFile");
                    downIntent.putExtra("com.edisoninteractive.inrideads.Services.extra.Url", url);
                    downIntent.putExtra("com.edisoninteractive.inrideads.Services.extra.FilePath", filePath);
                    getApplicationContext().startService(intent);
                }
            }
        }
    }

    private boolean downloadFile(String url, String filePath)
    {
        final NetworkUtils networkUtils = NetworkUtils.getInstance();
        final File tmpFile = new File(DATA_PATH, filePath + ".part");
        return networkUtils.downloadFile(url, tmpFile);
    }
}
