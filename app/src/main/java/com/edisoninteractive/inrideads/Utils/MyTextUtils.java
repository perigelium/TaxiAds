package com.edisoninteractive.inrideads.Utils;

import android.util.Log;
import android.util.Pair;

import com.edisoninteractive.inrideads.Entities.CSSstyleAttrs;
import com.edisoninteractive.inrideads.Entities.Command;
import com.edisoninteractive.inrideads.Entities.CommandWithParams;
import com.edisoninteractive.inrideads.Entities.Config_JS;
import com.edisoninteractive.inrideads.Entities.Params_;
import com.edisoninteractive.inrideads.Entities.Tab;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.CONFIGS_FOLDER_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;

/**
 * Created by Alex Angan one fine day
 */

public class MyTextUtils
{
    private static volatile MyTextUtils instance;
    private static final String className = getInstance().getClass().getSimpleName();

    private MyTextUtils()
    {
        // The singleton instance
    }

    public static MyTextUtils getInstance()
    {
        if (instance == null)
        {
            synchronized (MyTextUtils.class)
            {
                if (instance == null)
                {
                    instance = new MyTextUtils();
                }
            }
        }
        return instance;
    }

    private final char[] HEX_VALUES = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    public long dateStringToUnixTime(String datePost)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date;

        try
        {
            date = sdf.parse(datePost);

            return date.getTime();

        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public long timeStringToUnixTime(String datePost)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        Date timeNow;

        try
        {
            timeNow = sdf.parse(datePost);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeNow);

            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);

            Calendar calendarNow = Calendar.getInstance();

            calendarNow.set(Calendar.HOUR, hour);
            calendarNow.set(Calendar.MINUTE, minute);
            calendarNow.set(Calendar.SECOND, second);
            calendarNow.set(Calendar.MILLISECOND, 0);

            return calendarNow.getTimeInMillis();

        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public String toDisplayCase(String s)
    {
        final String ACTIONABLE_DELIMITERS = " '-/."; // these cause the character following
        // to be capitalized

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c : s.toCharArray())
        {
            c = (capNext) ? Character.toUpperCase(c) : Character.toLowerCase(c);
            sb.append(c);
            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
        }
        return sb.toString();
    }

    public String getMD5hash(String s)
    {
        MessageDigest m = null;

        try
        {
            m = MessageDigest.getInstance("MD5");

            m.update(s.getBytes(), 0, s.length());
            String hash = new BigInteger(1, m.digest()).toString(16);
            return hash;

        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    private String getMD5hash(InputStream source)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            while (true)
            {
                int length = source.read(buffer);
                if (length == -1)
                {
                    return hexify(digest.digest());
                }
                digest.update(buffer, 0, length);
            }
        } catch (Exception e)
        {
            return "";
        }
    }


    private String hexify(byte[] bytes)
    {
        char[] hexChars = new char[(bytes.length * 2)];
        for (int i = 0; i < bytes.length; i++)
        {
            int v = bytes[i] & 255;
            hexChars[i * 2] = HEX_VALUES[v >>> 4];
            hexChars[(i * 2) + 1] = HEX_VALUES[v & 15];
        }
        return new String(hexChars);
    }

    private byte[] createChecksum(String filename) throws Exception
    {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        int numRead;

        do
        {
            numRead = fis.read(buffer);
            if (numRead > 0)
            {
                messageDigest.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return messageDigest.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public String getMD5Checksum(String filename) throws Exception
    {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i = 0; i < b.length; i++)
        {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    //returns distance in meters
    public double getFlatEarthDistance(double lat1, double lng1, double lat2, double lng2)
    {
        double a = (lat1 - lat2) * distPerLat(lat1);
        double b = (lng1 - lng2) * distPerLng(lat1);
        return Math.sqrt(a * a + b * b);
    }

    private double distPerLng(double lat)
    {
        return 0.0003121092 * Math.pow(lat, 4) + 0.0101182384 * Math.pow(lat, 3) - 17.2385140059 * lat * lat + 5.5485277537 * lat + 111301.967182595;
    }

    private double distPerLat(double lat)
    {
        return -0.000000487305676 * Math.pow(lat, 4) - 0.0033668574 * Math.pow(lat, 3) + 0.4601181791 * lat * lat - 1.4558127346 * lat + 110579.25662316;
    }

    public static List<CommandWithParams> getCommandString(String strId, List<Command> commands)
    {
        List<CommandWithParams> lstCommandWithParams = new ArrayList<>();

        if (commands != null)
        {
            for (Command this_command : commands)
            {
                String strCommand = this_command.command;
                Params_ params_ = this_command.params;
                long delay = this_command.delay;

                List<String> blocks = (params_ != null) ? params_.blocks : null;

                if (blocks == null) // Broadcast command
                {
                    lstCommandWithParams.add(new CommandWithParams(strCommand, delay, params_));
                } else
                {
                    for (String certain_block : blocks)
                    {
                        if (certain_block.equals(strId))
                        {
                            lstCommandWithParams.add(new CommandWithParams(strCommand, delay, params_));
                        }
                    }
                }
            }
        }

        return lstCommandWithParams;
    }

    public static CSSstyleAttrs retrieveCSSstyle(String styleTitle)
    {
        String certainStyle = null;

        try
        {
            String pathToCssFile = DATA_PATH + "/" + CONFIGS_FOLDER_NAME + "/" + "styles.min.css";
            File cssFile = new File(pathToCssFile);

            String sccString = FileUtils.readFileToString(cssFile);

            String certainStyleStart = sccString.substring(sccString.indexOf(styleTitle));
            certainStyle = certainStyleStart.substring(0, certainStyleStart.indexOf("}"));

        } catch (Exception e)
        {
            Log.d(APP_LOG_TAG, "retrieveCSSstyle exception: " + e.getMessage());
            e.printStackTrace();
        }

        if (certainStyle == null)
        {
            return null;
        }

        String strColor = "";
        String fontWeight = "";
        Float fontSize = 0.0f;
        String textAlign = "";

        try
        {
            String colorStart = certainStyle.substring(certainStyle.indexOf("color:"));
            strColor = colorStart.substring(colorStart.indexOf(":") + 1, colorStart.indexOf(";"));
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            String fontWeightStart = certainStyle.substring(certainStyle.indexOf("font-weight"));

            if (fontWeightStart.contains(";"))
            {
                fontWeight = fontWeightStart.substring(fontWeightStart.indexOf(":") + 1, fontWeightStart.indexOf(";"));
            } else
            {
                fontWeight = fontWeightStart.substring(fontWeightStart.indexOf(":") + 1);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            String fontSizeStart = certainStyle.substring(certainStyle.indexOf("font-size"));
            String strFontSize = fontSizeStart.substring(fontSizeStart.indexOf(":") + 1, fontSizeStart.indexOf("px;"));
            fontSize = Float.valueOf(strFontSize) - 2;
        } catch (NumberFormatException e)
        {
            e.printStackTrace();
        }

        try
        {
            String textAlignStart = certainStyle.substring(certainStyle.indexOf("text-align"));

            if (textAlignStart.contains(";"))
            {
                textAlign = textAlignStart.substring(textAlignStart.indexOf(":") + 1, textAlignStart.indexOf(";"));
            } else
            {
                textAlign = textAlignStart.substring(textAlignStart.indexOf(":") + 1);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return new CSSstyleAttrs(strColor, fontWeight, fontSize, textAlign);
    }

    public static List<Pair<String, String>> findAllPlaylistsInConfig_JS()
    {
        Gson gson = new Gson();
        Config_JS config_js = FileUtils.readMediaInterfaceConfigObject();
        String str_config_json = gson.toJson(config_js);

        List<Pair<String, String>> idsPairs = new ArrayList<>();
        List<List<Tab>> tabs = new ArrayList<>();

        try
        {
            tabs = JsonPath.parse(str_config_json).read("$..tabs");

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        Type typeTab = new TypeToken<List<Tab>>()
        {
        }.getType();

        for (int i = 0; i < tabs.size(); i++)
        {
            List<Tab> tabList = gson.fromJson(String.valueOf(tabs.get(i)), typeTab);

            for (int j = 0; j < tabList.size(); j++)
            {
                Tab tab = tabList.get(j);

                if (tab.playList != null)
                {
                    idsPairs.add(new Pair<String, String>(tab.statsId, tab.playList));
                }
            }
        }

        return idsPairs;
    }
}
