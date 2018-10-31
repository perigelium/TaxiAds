package com.edisoninteractive.inrideads.Utils;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.Toast;

import com.edisoninteractive.inrideads.R;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;

/**
 * Created by Alex Angan one fine day
 */

public class ViewUtils
{
    private static final String className = ViewUtils.class.getSimpleName();

    public static void showToastMessage(final Activity activity, final String msg)
    {
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private static Pair<Integer,Integer> getScreenDimensions(Activity activity)
    {
        try
        {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            return new Pair<>(width, height);
        }
        catch (Exception ex)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ":getScreenDimensions error," + ex.getMessage());
        }

        return null;
    }

    public static Pair<Double, Double> getScreenRatios(Activity activity, String strWidth, String strHeight)
    {
        int scrWidthPX = (int) Double.parseDouble(strWidth);
        int scrHeightPX = (int) Double.parseDouble(strHeight);

        Pair scrDimensions = getScreenDimensions(activity);

        if (scrDimensions == null)
        {
            return null;
        }

        double scrWidthRealPX = (int) scrDimensions.first;
        double scrHeightRealPX = (int) scrDimensions.second;

        Double scaledWidth = scrWidthRealPX / scrWidthPX;
        Double scaledHeight = scrHeightRealPX / scrHeightPX;

        if (scaledWidth > 0 && scaledHeight > 0)
        {
            return new Pair<>(scaledWidth, scaledHeight);
        }

        return null;
    }

    public static void setImageViewBackground(ImageView imageView, String strIvBackgroundUrl)
    {
        if (strIvBackgroundUrl != null)
        {
            Drawable d = Drawable.createFromPath(DATA_PATH + strIvBackgroundUrl);

            if (d != null)
            {
                imageView.setBackground(d);
            }
            else
            {
                imageView.setBackgroundResource(R.drawable.edison_interactive);
            }
        }
    }

    public static void clearImageViewBackground(ImageView imageView)
    {
        imageView.setBackground(null);
    }
}
