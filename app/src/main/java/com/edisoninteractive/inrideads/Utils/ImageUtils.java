package com.edisoninteractive.inrideads.Utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Services.UploadService;

import net.glxn.qrgen.android.QRCode;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.BITMAP_QUALITY_PERCENT;

/**
 * Created by mdumik on 15.12.2017, edited by Alex Angan 2018.08.07.
 */

public class ImageUtils
{
    private static String className = ImageUtils.class.getSimpleName();
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private final long FIFTEEN_MINUTES = 1000 * 60 * 15;

    private Context context;
    private ImageReader mImageReader;
    private MediaProjection projection;
    private MediaProjectionManager projectionManager;
    private Intent data;
    private int resultCode;
    private int mDensity;
    private int mWidth;
    private int mHeight;

    public static String getMimeTypeOfUri(Context context, Uri uri)
    {
        BitmapFactory.Options opt = new BitmapFactory.Options();

        opt.inJustDecodeBounds = true;

        InputStream istream = null;
        ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver != null)
        {
            try
            {
                istream = contentResolver.openInputStream(uri);
            } catch (FileNotFoundException e)
            {
                Log.w(GlobalConstants.APP_LOG_TAG, " getMimeTypeOfUri exception, " + e.getMessage());
            }

            BitmapFactory.decodeStream(istream, null, opt);

            try
            {
                if (istream != null)
                {
                    istream.close();
                }
            } catch (IOException e)
            {
                Log.w(GlobalConstants.APP_LOG_TAG, " getMimeTypeOfUri exception, " + e.getMessage());
            }
        }

        return opt.outMimeType;
    }

    public static byte[] makeScreenshotFromView(View view)
    {
        //NetworkUtils.getInstance().showAndUploadLogEvent(className , 0, ": makeScreenshotFromView");

        Bitmap bitmap;
        try
        {
            bitmap = getBitmapFromView(view);
        } catch (Exception ex)
        {
            Log.w(GlobalConstants.APP_LOG_TAG, " makeScreenshotFromView exception, " + ex.getMessage());

            return null;
        }
        if (null == bitmap)
        {
            return null;
        }

        return getBytesFromBitmap(bitmap, BITMAP_QUALITY_PERCENT);
    }

    private static Bitmap getBitmapFromView(View view)
    {
        Bitmap bitmap = null;
        try
        {
            view.setDrawingCacheEnabled(true);
            bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.destroyDrawingCache();
            view.setDrawingCacheEnabled(false);
        } catch (Exception e)
        {
            Log.w(GlobalConstants.APP_LOG_TAG, " getBitmapFromView exception, " + e.getMessage());
        }
        return bitmap;
    }

    /*************************** MEDIA PROJECTION FOR API 21+ ************************************/

    public void makeScreenshotsViaMediaProjection(Context context, MediaProjectionManager projectionManager, int resultCode, Intent data)
    {
        this.projectionManager = projectionManager;
        this.context = context;
        this.data = data;
        this.resultCode = resultCode;

        // display metrics
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mDensity = metrics.densityDpi;
        Display mDisplay = ((Activity) context).getWindowManager().getDefaultDisplay();

        // get width and height
        Point size = new Point();
        mDisplay.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    createVirtualDisplay();
                } catch (Exception ex)
                {
                    Log.w(GlobalConstants.APP_LOG_TAG, "makeScreenshotsViaMediaProjection exception, " + ex.getMessage());
                }
                handler.postDelayed(this, FIFTEEN_MINUTES);
            }
        }, 10000);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createVirtualDisplay()
    {
        projection = projectionManager.getMediaProjection(resultCode, data);

        if (null == projection)
        {
            return;
        }

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 1);
        projection.createVirtualDisplay("screen-mirror", mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, null);

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener()
        {
            @Override
            public void onImageAvailable(ImageReader reader)
            {
                getImageAndSendToServer(reader);

                if (mImageReader != null)
                {
                    mImageReader.setOnImageAvailableListener(null, null);
                }
                projection.stop();
            }
        }, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void getImageAndSendToServer(ImageReader reader)
    {
        Image image = reader.acquireLatestImage();

        if (null == image)
        {
            return;
        }

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * mWidth;

        Bitmap bmp = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buffer);

        image.close();

        byte[] imageBytes = getBytesFromBitmap(bmp, BITMAP_QUALITY_PERCENT);

        if (null != imageBytes && NetworkUtils.isNetworkAvailable(context))
        {
            Intent intent = new Intent(context, UploadService.class);
            intent.putExtra(UploadService.EXTRA_IMAGE_BYTES_KEY, imageBytes);
            intent.putExtra(UploadService.EXTRA_UNIT_ID_KEY, GlobalConstants.UNIT_ID);
            context.startService(intent);
        }
    }

    public static Bitmap generateQRCode(String text)
    {
        if (text == null || TextUtils.isEmpty(text))
        {
            return null;
        }
        return QRCode.from(text).bitmap();
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    public static Bitmap getScaledBitmap(Bitmap originBitmap, Integer ratio)
    {
        if (originBitmap != null)
        {
            return Bitmap.createScaledBitmap(originBitmap, originBitmap.getWidth() / ratio, originBitmap.getHeight() / ratio, false);
        }
        return null;
    }
}
