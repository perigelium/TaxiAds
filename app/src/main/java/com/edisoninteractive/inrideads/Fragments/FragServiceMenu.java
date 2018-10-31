package com.edisoninteractive.inrideads.Fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.edisoninteractive.inrideads.Interfaces.Communicator;
import com.edisoninteractive.inrideads.MainActivity;
import com.edisoninteractive.inrideads.R;
import com.edisoninteractive.inrideads.Utils.ImageUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;
import com.edisoninteractive.inrideads.ViewOverrides.DialogShowTextArea;

import java.util.Timer;
import java.util.TimerTask;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UNIT_ID;

public class FragServiceMenu extends Fragment implements View.OnClickListener
{
    private static final String TEAM_VIEWER_PACKAGE_ID = "com.teamviewer.host.market";

    Activity activity;
    View rootView;
    private Communicator mCommunicator;

    private Button btnRegisterByCompCode, btnUpdateContent, btnCloseApp, btnRebootScreen, btnClose, btnBluetoothPair, btnSetupRemoteAccess;
    private ImageView ivQRCode;
    private long twoMinutes = 1000 * 60 * 2;
    private volatile long lastTouchTime;
    private Timer timer;
    private Button btnShowKnoxStatus;
    private BroadcastReceiver rec;
    private Button btnRegisterByInstallCode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mCommunicator = (Communicator) activity;

        IntentFilter filter = new IntentFilter("KNOX_STATUS_UPDATE");

        rec = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.i(APP_LOG_TAG, "KNOX_STATUS_UPDATE received");

                String knox_keys_activated = "Knox keys activated : " + intent.getStringExtra("knox_keys_activated");
                String kiosk_mode_activated = "Kiosk mode activated : " + intent.getStringExtra("kiosk_mode_activated");
                String boot_on_power_activated = "Boot on power activated : " + intent.getStringExtra("boot_on_power_activated");
                String mobile_data_activated = "Mobile data activated : " + intent.getStringExtra("mobile_data_activated");
                String hotspot_activated = "Hotspot activated : " + intent.getStringExtra("hotspot_activated");

                String message = knox_keys_activated + "\n\n" + kiosk_mode_activated + "\n\n" + boot_on_power_activated + "\n\n" +
                        mobile_data_activated + "\n\n" +  hotspot_activated + "\n\n";

                DialogShowTextArea dialogShowTextArea = new DialogShowTextArea();
                Bundle bundle = new Bundle();
                bundle.putString("message", message);
                dialogShowTextArea.setArguments(bundle);
                dialogShowTextArea.show(getFragmentManager(), "dialogShowTextArea");
            }
        };

        activity.registerReceiver(rec, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.frag_admin_menu, container, false);

        initializeVariables();
        generateQRCode();
        setOnClickListeners();
        setOnRootViewTouchListeners();
        startUserActivityTimeChecker();

        if (isTeamViewerInstalled())
        {
            btnSetupRemoteAccess.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    private void initializeVariables()
    {
        btnClose = (Button) rootView.findViewById(R.id.btn_close);
        btnRegisterByCompCode = (Button) rootView.findViewById(R.id.btn_register_by_comp_code);
        btnRegisterByInstallCode = (Button) rootView.findViewById(R.id.btn_register_by_install_code);
        btnUpdateContent = (Button) rootView.findViewById(R.id.btn_update_content);
        btnCloseApp = (Button) rootView.findViewById(R.id.btn_close_app);
        btnRebootScreen = (Button) rootView.findViewById(R.id.btn_reboot_screen);
        btnBluetoothPair = (Button) rootView.findViewById(R.id.btn_open_bluetooth);
        btnSetupRemoteAccess = (Button) rootView.findViewById(R.id.btn_setup_remote_access);
        btnShowKnoxStatus = (Button) rootView.findViewById(R.id.btn_show_knox_status);
        ivQRCode = (ImageView) rootView.findViewById(R.id.iv_qr_code);
        lastTouchTime = System.currentTimeMillis();

        String manufacturer = android.os.Build.MANUFACTURER;

        if (!manufacturer.toLowerCase().contains("samsung"))
        {
            btnShowKnoxStatus.setVisibility(View.INVISIBLE);
        }
    }

    private void generateQRCode()
    {
        Bitmap myBitmap = ImageUtils.generateQRCode(UNIT_ID);
        if (myBitmap != null)
        {
            ivQRCode.setImageBitmap(myBitmap);
        }
    }

    private void setOnClickListeners()
    {
        btnClose.setOnClickListener(this);
        btnRegisterByCompCode.setOnClickListener(this);
        btnRegisterByInstallCode.setOnClickListener(this);
        btnUpdateContent.setOnClickListener(this);
        btnCloseApp.setOnClickListener(this);
        btnRebootScreen.setOnClickListener(this);
        btnBluetoothPair.setOnClickListener(this);
        btnSetupRemoteAccess.setOnClickListener(this);
        btnShowKnoxStatus.setOnClickListener(this);
    }

    private void setOnRootViewTouchListeners()
    {
        rootView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                lastTouchTime = System.currentTimeMillis();
                return false;
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        lastTouchTime = System.currentTimeMillis();

        switch (v.getId())
        {
            case R.id.btn_close:
                SystemUtils.restartActivity(activity, false);
                break;

            case R.id.btn_register_by_comp_code:
                registerByCompCode();
                break;

            case R.id.btn_register_by_install_code:
                registerByInstallCode();
                break;

            case R.id.btn_update_content:
                updateContent();
                break;

            case R.id.btn_close_app:
                closeApp();
                break;

            case R.id.btn_reboot_screen:
                rebootScreen();
                break;

            case R.id.btn_open_bluetooth:
                openBluetoothScreen();
                break;

            case R.id.btn_setup_remote_access:
                launchTeamViewer();
                break;
            case R.id.btn_show_knox_status:
                requestAndShowKnoxData();
                break;
        }
    }

    private void registerByInstallCode()
    {
        mCommunicator.replaceFragment(FragRegisterInstallCode.class.getSimpleName(), new FragRegisterInstallCode());
    }

    private void requestAndShowKnoxData()
    {
        Intent in = new Intent("GET_KNOX_STATUS_EDISON");
        activity.sendBroadcast(in);
    }

    private void openBluetoothScreen()
    {
        mCommunicator.replaceFragment(FragBindBluetoothDevice.class.getSimpleName(), new FragBindBluetoothDevice());
    }

    private void registerByCompCode()
    {
        mCommunicator.replaceFragment(FragRegisterCompanyCode.class.getSimpleName(), new FragRegisterCompanyCode());
    }

    private void updateContent()
    {
        mCommunicator.replaceFragment(FragShowDiagAndUpdateContent.class.getSimpleName(), new FragShowDiagAndUpdateContent());
    }

    private void closeApp()
    {
        mCommunicator.closeApp();
    }

    private void rebootScreen()
    {
        SystemUtils.rebootDevice(getActivity());
    }

    private void launchTeamViewer()
    {
        if (isTeamViewerInstalled())
        {
            try
            {
                Intent LaunchIntent = activity.getPackageManager().getLaunchIntentForPackage(TEAM_VIEWER_PACKAGE_ID);
                startActivity(LaunchIntent);

                Intent intent = new Intent(activity, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager mgr = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, SystemClock.elapsedRealtime() + 120000, pendingIntent);
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    private boolean isTeamViewerInstalled()
    {
        PackageManager pm = activity.getPackageManager();
        try
        {
            pm.getPackageInfo(TEAM_VIEWER_PACKAGE_ID, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e)
        {
        }

        return false;
    }

    private void startUserActivityTimeChecker()
    {
        timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastTouchTime) >= twoMinutes)
                {
                    SystemUtils.restartActivity(activity, false);
                }
            }
        }, twoMinutes, twoMinutes);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        if (null != timer)
        {
            timer.cancel();
        }

        if (rec != null)
        {
            activity.unregisterReceiver(rec);
        }
    }
}
