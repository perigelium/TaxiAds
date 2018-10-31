package com.edisoninteractive.inrideads.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.AdSchedule;
import com.edisoninteractive.inrideads.Entities.Config_JS;
import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.Response;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.ADSCHEDULE_JS_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_PREFERENCES;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.CONFIGS_FOLDER_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.CONFIG_JS_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_ROOT_FOLDER_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.PARAMS_JS_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.STORAGE_DIR;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UNIT_ID;

/**
 * Created by Alex Angan one fine day
 */

public class FileUtils
{
    private static volatile FileUtils instance;
    private static final String className = FileUtils.class.getSimpleName();

    private FileUtils()
    {
    }

    public static FileUtils getInstance()
    {
        if (instance == null)
        {
            synchronized (FileUtils.class)
            {
                if (instance == null)
                {
                    instance = new FileUtils();
                }
            }
        }
        return instance;
    }

    public static void deleteFolderRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory()) for (File file : fileOrDirectory.listFiles())
        {
            deleteFolderRecursive(file);
        }

        fileOrDirectory.delete();
    }

    public static void deleteFilesOfTypeInFolder(String folderPath, String fileType)
    {
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory())
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": deleteFilesOfType: target folder does not exist");
            return;
        }

        for (File file : folder.listFiles())
        {
            if (FileUtils.isFileOfType(file.getPath(), fileType))
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": deleteFilesOfType: " + file.getName() + " deleted");
                file.delete();
            }
        }
    }

    public static void delFilesMissedInCurrentConfig(String folderPath, List<String> paths)
    {
        File folder = new File(folderPath);

        if (!folder.exists())
        {
            NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": delFilesMissedInCurrentConfig: target folder does not exist");
            return;
        }

        for (File file : folder.listFiles())
        {
            if (file.isDirectory())
            {
                delFilesMissedInCurrentConfig(file.getAbsolutePath(), paths);
                continue;
            }

            Boolean pathNotFound = true;

            for (String path : paths)
            {
                if (file.getPath().equals(path))
                {
                    pathNotFound = false;
                    break;
                }
            }

            if (pathNotFound)
            {
                if (file.delete())
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": delFilesMissedInCurrentConfig: " + file.getName() + " deleted");
                } else
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": delFilesMissedInCurrentConfig: " + file.getName() + " failed to delete");
                }
            }
        }
    }


    public static boolean moveDirectory(File sourceLocation, File targetLocation) throws IOException
    {
        boolean restStatus = true;

        if (sourceLocation.isDirectory())
        {
            if (!targetLocation.exists() && !targetLocation.mkdirs())
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ":moveDirectory: Cannot create dir");
            }

            String[] children = sourceLocation.list();
            if (children != null)
            {
                for (int i = 0; i < children.length; i++)
                {
                    moveDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
                }
            }
        } else
        {
            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();

            if (directory != null && !directory.exists() && !directory.mkdirs())
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": moveDirectory: Cannot create dir");
                restStatus = false;
            }

            if (!sourceLocation.renameTo(targetLocation))
            {
                restStatus = false;
            }
        }

        return restStatus;
    }

    public static boolean writeStringToFile(String filePath, String strContent)
    {
        boolean retStatus = false;

        FileWriter fw;
        try
        {
            fw = new FileWriter(filePath);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(strContent);
            bw.flush();
            bw.close();

            retStatus = true;
        } catch (IOException e)
        {
            retStatus = false;
            e.printStackTrace();
        }

        return retStatus;
    }

    public static String readFileToString(File configFile)
    {
        int length = (int) configFile.length();

        byte[] bytes = new byte[length];

        FileInputStream in = null;
        try
        {
            in = new FileInputStream(configFile);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        try
        {
            in.read(bytes);
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                in.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        String contents = new String(bytes);

        return contents;
    }

    public static boolean isImageFile(Context context, String strPathToFile)
    {
        Uri uriSrcImage = Uri.parse(strPathToFile);

        String mediaType = ImageUtils.getMimeTypeOfUri(context, uriSrcImage);

        return mediaType != null && mediaType.startsWith("image");
    }

    public static boolean isVideoFile(Context context, String strPathToFile)
    {
        Uri uriSrcImage = Uri.parse(strPathToFile);

        String mediaType = ImageUtils.getMimeTypeOfUri(context, uriSrcImage);

        return mediaType != null && mediaType.startsWith("video");
    }

    public static boolean hasImageExtension(String strPathToFile)
    {
        Uri uriSrcImage = Uri.parse(strPathToFile);

        String srcImageFileName = uriSrcImage.getLastPathSegment();

        String fileExtension = "";
        int extensionPtr = -1;

        if (srcImageFileName != null)
        {
            extensionPtr = srcImageFileName.lastIndexOf(".");
        }

        if (extensionPtr != -1)
        {
            fileExtension = srcImageFileName.substring(extensionPtr);
            fileExtension = fileExtension.toLowerCase();
        }

        return fileExtension.equals(".png") || fileExtension.equals(".jpg");
    }

    public static boolean hasVideoExtension(String strPathToFile)
    {
        Uri uriSrcImage = Uri.parse(strPathToFile);
        String srcImageFileName = uriSrcImage.getLastPathSegment();

        String fileExtension = "";
        int extensionPtr = -1;

        if (srcImageFileName != null)
        {
            extensionPtr = srcImageFileName.lastIndexOf(".");
        }

        if (extensionPtr != -1)
        {
            fileExtension = srcImageFileName.substring(extensionPtr);
            fileExtension = fileExtension.trim();
            fileExtension = fileExtension.toLowerCase();
        }

        boolean result = fileExtension.equals(".mp4") || fileExtension.equals(".flv");

        return result;
    }

    public static boolean isFileOfType(String strPathToFile, String fileType)
    {
        if (strPathToFile == null || strPathToFile.isEmpty() || fileType == null || fileType.isEmpty())
        {
            return false;
        }

        Uri uriSrcImage = Uri.parse(strPathToFile);

        if (uriSrcImage == null)
        {
            return false;
        }

        String srcImageFileName = uriSrcImage.getLastPathSegment();

        if (srcImageFileName == null)
        {
            return false;
        }

        String fileExtension = "";
        int extensionPtr = srcImageFileName.lastIndexOf(".");

        if (extensionPtr != -1)
        {
            fileExtension = srcImageFileName.substring(extensionPtr);
            fileExtension = fileExtension.toLowerCase();
        }

        return fileExtension.equals(fileType);
    }

    public static Boolean bufferedSourceToFileByChunks(@NonNull BufferedSource source, @NonNull File destFile) throws IOException
    {
        Boolean result = false;

        try
        {
            BufferedSink sink = Okio.buffer(Okio.sink(destFile));
            Buffer sinkBuffer = sink.buffer();

            int bufferSize = 4 * 1024;

            while (!source.exhausted())
            {
                long bytesRead = source.read(sinkBuffer, bufferSize);
                sink.emit();
            }

/*            for (long bytesRead; (bytesRead = source.read(sinkBuffer, bufferSize)) != -1; )
            {
                sink.emit();
            }*/

            sink.flush();
            sink.close();
            source.close();
            sinkBuffer.close();
            result = true;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static Boolean bufferedSourceTofile(@NonNull BufferedSource source, @NonNull File destFile) throws IOException
    {
        Boolean result = false;

        try
        {
            BufferedSink sink = Okio.buffer(Okio.sink(destFile));
            Buffer sinkBuffer = sink.buffer();

            sink.writeAll(source);

            sink.flush();
            sink.close();
            source.close();
            sinkBuffer.close();
            result = true;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean writeItemContentToFile(@NonNull String pathSuffix, @NonNull String remoteObjectName, @NonNull String strContent)
    {
        boolean retStatus = false;
        String filePath = "/" + DATA_ROOT_FOLDER_NAME + pathSuffix;

        File dir2 = new File(STORAGE_DIR, filePath);

        if (!dir2.exists())
        {
            dir2.mkdirs();
        }

        File file = new File(dir2, remoteObjectName);

        FileOutputStream stream = null;
        try
        {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        try
        {
            if (stream != null)
            {
                stream.write(strContent.getBytes());
            }
            retStatus = true;
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (stream != null)
            {
                try
                {
                    stream.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return retStatus;
    }

    public static String getUnitID(String pathToFile)
    {
        File fileParams_JS = new File(pathToFile);

        if (fileParams_JS.exists())
        {
            String fileParams_JS_Content = readFileToString(fileParams_JS);

            try
            {
                JSONObject jsonObject = new JSONObject(fileParams_JS_Content);

                return jsonObject.getString("unitId");
            } catch (JSONException e)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": Error while parsing params.js: " + e.getMessage());
                e.printStackTrace();
            }
        } else
        {
            Log.d(APP_LOG_TAG, className + ": " + fileParams_JS.getAbsolutePath() + " doesn't exist");
        }
        return null;
    }

    @Nullable
    public static Config_JS readMediaInterfaceConfigObject()
    {
        String pathToConfigs = STORAGE_DIR.getAbsolutePath() + "/" + DATA_ROOT_FOLDER_NAME + "/" + CONFIGS_FOLDER_NAME;
        File configsFolder = new File(pathToConfigs);
        File configFile = new File(configsFolder, CONFIG_JS_NAME);

        if (configFile.exists())
        {
            String configContent = readFileToString(configFile);
            Gson gson = new Gson();

            try
            {
                return gson.fromJson(configContent, Config_JS.class);
            } catch (Exception e)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ": Error in readMediaInterfaceConfigObject, " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void unzipMyZipToFolderStructure(InputStream inputStream, String directoryToExtractTo)
    {
        byte[] buffer = new byte[8192];

        try
        {
            ZipInputStream zin = new ZipInputStream(inputStream);
            ZipEntry ze;

            while ((ze = zin.getNextEntry()) != null)
            {
                if (!ze.isDirectory())
                {
                    FileOutputStream fout = new FileOutputStream(directoryToExtractTo + "/" + ze.getName());
                    int count;

                    while ((count = zin.read(buffer)) != -1)
                    {
                        fout.write(buffer, 0, count);
                    }
                    zin.closeEntry();
                    fout.close();
                } else
                {
                    File f = new File(directoryToExtractTo + "/" + ze.getName());
                    f.mkdirs();
                }
            }
            zin.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void writeStreamToFile(InputStream in, OutputStream out) throws IOException
    {
        try
        {
            byte[] buffer = new byte[8192];
            int len;

            while ((len = in.read(buffer)) >= 0) out.write(buffer, 0, len);

            in.close();
            out.close();
        } catch (IOException e)
        {
            Log.d(APP_LOG_TAG, className + ": writeStreamToFile IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String streamToString(InputStream inputStream)
    {
        StringBuilder total = new StringBuilder();
        try
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = r.readLine()) != null)
            {
                total.append(line);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return total.toString();
    }

    public Boolean updateUnitId(Context context)
    {
        Boolean result = false;
        final SharedPreferences mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        String unitId;
        String pathToFileInConfig = DATA_PATH + "/" + CONFIGS_FOLDER_NAME + "/" + PARAMS_JS_NAME;
        unitId = FileUtils.getUnitID(pathToFileInConfig);

        Log.w(APP_LOG_TAG, className + ": FileUtils.getUnitID() from " + pathToFileInConfig + " unitId = " + unitId);

        if (unitId == null || unitId.equals("0000"))
        {
            String pathToFileInRoot = STORAGE_DIR + "/" + PARAMS_JS_NAME;
            unitId = FileUtils.getUnitID(pathToFileInRoot);

            Log.w(APP_LOG_TAG, className + ": FileUtils.getUnitID() from " + pathToFileInRoot + " unitId = " + unitId);
        }

        if (unitId == null || unitId.equals("0000"))
        {
            unitId = UNIT_ID;

            Log.w(APP_LOG_TAG, className + ": FileUtils.updateUnitId() from GlobalConstants, unitId = " + unitId);
        }

        if (unitId == null || unitId.equals("0000"))
        {
            unitId = mSettings.getString("unit_id", "0000");

            Log.w(APP_LOG_TAG, className + ": FileUtils.updateUnitId() from SharedPreferences, unitId = " + unitId);
        }

        if (unitId.equals("0000") && NetworkUtils.isNetworkAvailable(context))
        {
            NetworkUtils networkUtils = NetworkUtils.getInstance();
            unitId = networkUtils.requestUnitId(SystemUtils.getDeviceUUID(context));

            Log.w(APP_LOG_TAG, className + ": FileUtils.updateUnitId() using back-end query, unitId = " + unitId);
        }

        if (!unitId.equals("0000"))
        {
            mSettings.edit().putString("unit_id", unitId).apply();

            Log.w(APP_LOG_TAG, className + ": FileUtils.updateUnitId() - updated unitId in SharedPreferences, unitId =  " + mSettings.getString("unit_id", "0000"));

            String jsonStrUnitId = "{\"unitId\":\"" + unitId + "\"}";
            writeStringToFile(pathToFileInConfig, jsonStrUnitId);

            Log.w(APP_LOG_TAG, className + ": FileUtils.updateUnitId() - updated unitId in params.js, file content =  " + readFileToString(new File(pathToFileInConfig)));

            GlobalConstants.UNIT_ID = unitId;

            Log.w(APP_LOG_TAG, className + ": FileUtils.updateUnitId() - updated unitId in GlobalConstants, unitId =  " + UNIT_ID);

            result = true;
        }
        else
        {
            Log.e(APP_LOG_TAG, className + ": FileUtils.updateUnitId() - Unable to obtain unitId !");
        }

        return result;
    }

    public static List<Response> reloadAdsList(List<Response> adsList)
    {
        String pathToConfigs = STORAGE_DIR.getAbsolutePath() + "/" + DATA_ROOT_FOLDER_NAME + "/" + CONFIGS_FOLDER_NAME;
        File configsFolder = new File(pathToConfigs);
        File adSchedule = new File(configsFolder, ADSCHEDULE_JS_NAME);
        Gson gson = new Gson();

        if (adSchedule.exists())
        {
            String adScheduleContent = FileUtils.readFileToString(adSchedule);

            try
            {
                AdSchedule adScheduleJSONobj = gson.fromJson(String.valueOf(adScheduleContent), AdSchedule.class);

                if (adScheduleJSONobj != null && adScheduleJSONobj.response != null)
                {
                    return adScheduleJSONobj.response;
                } else
                {
                    NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ":reloadAdsList: unable to reload ads list");
                }
            } catch (JsonSyntaxException e)
            {
                NetworkUtils.getInstance().showAndUploadLogEvent(className , 1, ":reloadAdsList: unable to reload ads list, " + e.getMessage());
                e.printStackTrace();
            }
        }

        return adsList;
    }

    public static long getFolderSize(File f)
    {
        long size = 0;
        if (f.isDirectory())
        {
            for (File file : f.listFiles())
            {
                size += getFolderSize(file);
            }
        } else
        {
            size = f.length();
        }
        return size / (1024 * 1024);
    }
}

