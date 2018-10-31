package com.edisoninteractive.inrideads.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.StatFs;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.edisoninteractive.inrideads.BuildConfig;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.BITMAP_QUALITY_PERCENT;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;

/**
 * Created by Alex Angan one fine day
 */

public class SystemUtils
{
    private Activity activity;
    private static final String className = SystemUtils.class.getSimpleName();

    public SystemUtils(Activity activity) throws PackageManager.NameNotFoundException
    {
        this.activity = activity;
    }

    public static String getWifiMAC(Context context)
    {
        String result = null;

        try
        {
            if (Build.VERSION.SDK_INT >= 23)
            {
                List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : all)
                {
                    if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null)
                    {
                        return null;
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes)
                    {
                        res1.append(Integer.toHexString(b & 0xFF) + ":");
                    }

                    if (res1.length() > 0)
                    {
                        res1.deleteCharAt(res1.length() - 1);
                    }

                    return res1.toString();
                }
            } else
            {
                WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInf = wifiMan.getConnectionInfo();
                result = wifiInf.getMacAddress();
            }

        } catch (Exception exception)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 2, ": : Failed to extract wifi mac address");
        }

        return result != null ? result : "";
    }

    public static String getAppVersionString(Context context)
    {
        try
        {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e)

        {
            e.printStackTrace();
        }
        return "999";
    }

    public static String getDeviceUUID(Context context)
    {
        String deviceUUID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        deviceUUID = deviceUUID != null ? deviceUUID : "";

        return deviceUUID;
    }

    public static float getMBytesInExternalStorageAvailable()
    {
        StatFs stat = new StatFs(DATA_PATH);

        long bytesAvailable = stat.getAvailableBytes();

        float mMBytesAvailable = bytesAvailable / (1024.f * 1024.f);

        return mMBytesAvailable;
    }

    public static boolean isGooglePlayServicesUpToDateAndAvailable(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            return false;
        }

        int playServicesAvailableResCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        if (playServicesAvailableResCode != ConnectionResult.SUCCESS)
        {
            try
            {
                String versionName = context.getPackageManager().getPackageInfo("com.google.android.gms", 0).versionName;
                Log.d(APP_LOG_TAG, className + ": : Google Play Services is outdated or unavailable, current version is: " + versionName);

            } catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        return playServicesAvailableResCode == ConnectionResult.SUCCESS;
    }

    public static boolean isDeviceRooted()
    {
        return checkRootMethod2() || checkRootMethod3();
    }

    // the following method can return true even on unrooted devices.
    private static boolean checkRootMethod1()
    {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2()
    {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths)
        {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkRootMethod3()
    {
        Process process = null;
        try
        {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t)
        {
            return false;
        } finally
        {
            if (process != null) process.destroy();
        }
    }

    public static void rebootDevice(Context context)
    {
        try
        {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
            proc.waitFor();
        } catch (Exception ex)
        {
            ex.printStackTrace();
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 2, ": : Failed to reboot the screen using su shell");
        }

        Intent rebootIntent = new Intent(GlobalConstants.REBOOT_DEVICE_INTENT_ACTION);
        context.sendBroadcast(rebootIntent);
    }

/*    public static void restartActivity(Context context, Class<?> activityClass, long delay)
    {
        NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": : Scheduling restart Activity after " + delay + " seconds");

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if (am == null)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": : AlarmManager not available");
            return;
        }

        Intent intent = new Intent(context, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

        if (pIntent == null)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": : PendingIntent.getActivity - PendingIntent is null");
            return;
        }

        am.set(AlarmManager.RTC, System.currentTimeMillis() + 1000 * delay + 100, pIntent);

        NetworkUtils.getInstance().showAndUploadLogEvent(className , 2, ": : Exiting application");

        if (context instanceof Activity)
        {
            ((Activity) context).finish();
        }

        System.exit(0);
    }*/

/*    public static void restartActivity(Context context, Class<?> activityClass, long delay)
    {
        Intent intent = new Intent(context, context.getClass());
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK); // In case we are called with non-Activity context.

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);

        if (context instanceof Activity)
        {
            ((Activity) context).finish();
        }
        Runtime.getRuntime().exit(0); // Kill kill kill!
    }*/

    public static void restartActivity(Context context, Boolean realRestart)
    {
        NetworkUtils.getInstance().showAndUploadLogEvent(className, 1, ": restartActivity");

        Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());

        if (i != null)
        {
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Boolean isActivity = context instanceof Activity;

            if (!isActivity)
            {
                i.addFlags(FLAG_ACTIVITY_NEW_TASK);
            }

            context.startActivity(i);

            if (isActivity)
            {
                ((Activity) context).finish();
            }

            if (realRestart)
            {
                System.exit(0);
            }
        }
    }

    public static void scheduleStartService(Service service, long delay)
    {
        Intent restartIntent = new Intent(service, service.getClass());
        PendingIntent pi = PendingIntent.getService(service, 1, restartIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager am = (AlarmManager) service.getSystemService(ALARM_SERVICE);

        if (am == null)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 2, ": : Unable to connect to Alarm Manager service");
            return;
        }

        long time = System.currentTimeMillis() + 1000 * delay;
        am.setExact(AlarmManager.RTC_WAKEUP, time, pi);
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass)
    {
        try
        {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            if (manager != null)
            {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
                {
                    if (serviceClass.getName().equals(service.service.getClassName()))
                    {
                        return true;
                    }
                }
            }
        } catch (Exception e)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 2, " isServiceRunning exception, " + e.getMessage());
        }
        return false;
    }

    public static boolean isActivityRunning(Context context)
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null)
        {
            return false;
        }

        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        if (tasks == null)
        {
            return false;
        }

        for (ActivityManager.RunningTaskInfo task : tasks)
        {
            if (context.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
            {
                return true;
            }
        }

        return false;
    }

    public static void shutdownDevice(Context context)
    {
        NetworkUtils.getInstance().showAndUploadLogEvent(className, 1, ": Shutdown device invoked");

        if (SystemUtils.deviceHasKnoxAndNotRooted(context))
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 1, ": sendBroadcast SHUT_DOWN_DEVICE_EDISON");
            //if its knox device - send intent to launcher or monitor
            context.sendBroadcast(new Intent(GlobalConstants.SHUTDOWN_INTENT_ACTION));

        } else if (SystemUtils.isDeviceRooted())
        {
            // rooted device NOT IEI -> shut down via shell cmd
            try
            {
                Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"});
                proc.waitFor();
            } catch (Exception ex)
            {
                ex.printStackTrace();
                NetworkUtils.getInstance().showAndUploadLogEvent(className, 2, ": Shutdown failed:" + ex.getMessage());
                Crashlytics.logException(ex);
            }
        } else
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 1, ": This device isnt IEI model and has no root access so it cant be turned off programmatically");
        }
    }


    private static boolean deviceHasKnoxSDK(Context context)
    {
        boolean result = false;

        try
        {
            PackageManager packageManager = context.getPackageManager();
            result = packageManager.hasSystemFeature("com.sec.android.mdm");
            Log.d(APP_LOG_TAG, className + ": Is this knox device: " + result);
        } catch (Throwable throwable)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 2, ": Failed to detect if its a knox device");
        }

        Log.d(APP_LOG_TAG, className + ": KNX AVAILABLE:" + result);

        return result;

    }

    public static boolean deviceHasKnoxAndNotRooted(Context context)
    {
        return deviceHasKnoxSDK(context) && !isDeviceRooted();
    }

    public static void uninstallAirVersionIfPresent()
    {
        try
        {
            if (isDeviceRooted())
            {
                new Thread(new Runnable()
                {

                    @Override
                    public void run()
                    {

                        try
                        {
                            ////////////////////////////////////////////////////////////////////////////////////////////
                            //                          FOR PRODUCTION PURPOSES
                            Process uninstallProcess = Runtime.getRuntime().exec("su -c pm uninstall " + GlobalConstants.AIR_VERSION_ID);
                            ////////////////////////////////////////////////////////////////////////////////////////////


                        } catch (Exception e)
                        {
                            NetworkUtils.getInstance().showAndUploadLogEvent(className, 2, ": Failed to uninstall AIR app");

                            //broadcastApkInstallFailed(currentInstallingItem.getDownloadOrder());
                        }
                    }
                }).start();
            }
        } catch (Exception exc)
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className, 1, ": uninstallAirVersionIfPresent threw an exception: " + exc.getMessage());
            exc.printStackTrace();

            if (!BuildConfig.DEBUG)
            {
                Crashlytics.logException(exc);
            }
        }
    }

    public static JSONArray getCellInfo(Context context)
    {
        if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }

        TelephonyManager telMngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (telMngr == null)
        {
            return null;
        }

        String mcc = "";
        String mnc = "";
        String networkOperator = telMngr.getNetworkOperator();

        try
        {
            mcc = networkOperator.substring(0, 3);
            mnc = networkOperator.substring(3);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        JSONArray cellList = new JSONArray();

        //from Android M up must use getAllCellInfo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)  // For KitKat and Lollipop
        {
            List<NeighboringCellInfo> neighCells = telMngr.getNeighboringCellInfo();
            for (int i = 0; i < neighCells.size(); i++)
            {
                try
                {
                    JSONObject cellObj = new JSONObject();
                    NeighboringCellInfo thisCell = neighCells.get(i);
                    cellObj.put("cellId", thisCell.getCid());
                    cellObj.put("locationAreaCode", thisCell.getLac());
                    cellObj.put("mobileCountryCode", mcc);
                    cellObj.put("mobileNetworkCode", mnc);
                    cellObj.put("signalStrength", thisCell.getRssi());
                    cellList.put(cellObj);
                } catch (Exception e)
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className, 1, ": getCellInfo threw an exception: " + e.getMessage());
                }
            }

        } else
        {
            List<CellInfo> infos = telMngr.getAllCellInfo();
            for (int i = 0; i < infos.size(); ++i)
            {
                try
                {
                    JSONObject cellObj = new JSONObject();
                    CellInfo info = infos.get(i);

                    if (info instanceof CellInfoWcdma)
                    {
                        CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info).getCellSignalStrength();
                        CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info).getCellIdentity();
                        cellObj.put("cellId", identityWcdma.getCid());
                        cellObj.put("locationAreaCode", identityWcdma.getLac());
                        cellObj.put("mobileCountryCode", identityWcdma.getMcc());
                        cellObj.put("mobileNetworkCode", identityWcdma.getMnc());
                        cellObj.put("signalStrength", wcdma.getDbm());
                        cellList.put(cellObj);
                    } else if (info instanceof CellInfoGsm)
                    {
                        CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                        CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                        cellObj.put("cellId", identityGsm.getCid());
                        cellObj.put("locationAreaCode", identityGsm.getLac());
                        cellObj.put("mobileCountryCode", identityGsm.getMcc());
                        cellObj.put("mobileNetworkCode", identityGsm.getMnc());
                        cellObj.put("signalStrength", gsm.getDbm());
                        cellList.put(cellObj);
                    } else if (info instanceof CellInfoLte)
                    {
                        CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                        CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                        cellObj.put("cellId", identityLte.getCi());
                        cellObj.put("locationAreaCode", identityLte.getTac());
                        cellObj.put("mobileCountryCode", identityLte.getMcc());
                        cellObj.put("mobileNetworkCode", identityLte.getMnc());
                        cellObj.put("signalStrength", lte.getDbm());
                        cellList.put(cellObj);
                    }
                    break;

                } catch (Exception ex)
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className, 1, ": getCellInfo threw an exception: " + ex.getMessage());
                }
            }
        }

        return cellList;
    }

    private int frequency_to_channel(int freq)
    {
        if (freq == 2484) return 14;

        if (freq < 2484) return (freq - 2407) / 5;

        return freq / 5 - 1000;
    }

    public static void setVmPolicyInDebugMode()
    {
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
    }

    public static byte[] getScreenshotBytesOnRootedDevice()
    {
        Log.w(APP_LOG_TAG, "getScreenshotBytesOnRootedDevice - start");
        Bitmap screenshotBitmap = null;

        try
        {
            Process process = Runtime.getRuntime().exec("su");

            OutputStreamWriter outputStream = new OutputStreamWriter(process.getOutputStream());
            outputStream.write("/system/bin/screencap -p\n");
            outputStream.flush();

            screenshotBitmap = BitmapFactory.decodeStream(process.getInputStream());

            if (screenshotBitmap == null)
            {
                return null;
            }

            Bitmap scaledBitmap = ImageUtils.getScaledBitmap(screenshotBitmap, 3);

            if (scaledBitmap == null)
            {
                return null;
            }

            int bmpHeight = scaledBitmap.getHeight();
            int bmpWidth = scaledBitmap.getWidth();

            if (bmpHeight > bmpWidth)
            {
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
                screenshotBitmap = rotatedBitmap;
            }

            outputStream.write("exit\n");
            outputStream.flush();
            outputStream.close();
            process.waitFor();

            Log.i(APP_LOG_TAG, SystemUtils.class.getSimpleName() + " getScreenshotBytesOnRootedDevice - success");
        } catch (Exception e)
        {
            Log.e(APP_LOG_TAG, SystemUtils.class.getSimpleName() + " getScreenshotBytesOnRootedDevice exception: " + e.getMessage());
        }

        return screenshotBitmap != null ? ImageUtils.getBytesFromBitmap(screenshotBitmap, BITMAP_QUALITY_PERCENT) : null;
    }
}
