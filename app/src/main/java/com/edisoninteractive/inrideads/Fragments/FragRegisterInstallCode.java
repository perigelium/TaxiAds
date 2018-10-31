package com.edisoninteractive.inrideads.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.edisoninteractive.inrideads.Interfaces.Communicator;
import com.edisoninteractive.inrideads.R;
import com.edisoninteractive.inrideads.Utils.SystemUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.API_UPDATER_CHECK_URL;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_PREFERENCES;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.CONFIGS_FOLDER_NAME;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.DATA_PATH;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UNIT_ID;

/**
 * Created by Alex Angan on 18.04.2018.
 */

public class FragRegisterInstallCode extends Fragment implements View.OnClickListener
{

    Activity activity;
    private Communicator mCommunicator;
    private View rootView;

    private EditText codeField;
    private Button btnCancel, btnRegister;

    private ProgressDialog requestServerDialog;
    private String className = getClass().getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mCommunicator = (Communicator) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.register_install_code, container, false);

        initializeVariables();
        setupProgressbar();
        setupCodeField();
        return rootView;
    }

    private void initializeVariables()
    {
        btnCancel = (Button) rootView.findViewById(R.id.btn_cancel);
        btnRegister = (Button) rootView.findViewById(R.id.btn_register);
        codeField = (EditText) rootView.findViewById(R.id.et_install_code);

        btnCancel.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
    }

    private void setupCodeField()
    {
        codeField.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    hideKeyboard(v);
                }
            }
        });

        codeField.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    hideKeyboard(v);
                    return true;
                }
                return false;
            }
        });
    }

    private void hideKeyboard(View v)
    {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (inputMethodManager == null)
        {
            return;
        }

        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_cancel:
                mCommunicator.replaceFragment(FragServiceMenu.class.getSimpleName(), new FragServiceMenu());
                break;

            case R.id.btn_register:

                if (TextUtils.isEmpty(codeField.getText().toString()))
                {
                    codeField.requestFocus();
                    codeField.setError(getString(R.string.required_field));
                } else
                {
                    //codeField.clearFocus();
                    disableRegBtn();
                    showProgressbar();
                    final String code = codeField.getText().toString();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            executeRegistration(code);
                        }
                    }, 1000);
                }
                break;
        }
    }

    private void executeRegistration(String installCode)
    {
        String unitId = UNIT_ID;
        String uuid = SystemUtils.getDeviceUUID(activity);
        String wifiMac = SystemUtils.getWifiMAC(activity);
        String url = API_UPDATER_CHECK_URL;

        if (!TextUtils.isEmpty(unitId) && uuid != null && !TextUtils.isEmpty(uuid) && wifiMac != null && !TextUtils.isEmpty(wifiMac) && installCode != null && !TextUtils.isEmpty(installCode))
        {
            String urlWithParams = HttpUrl.parse(url).newBuilder()
                    .addQueryParameter("unit_id", unitId)
                    .addQueryParameter("installCode", installCode)
                    .addQueryParameter("uuid", uuid)
                    //.addQueryParameter("wifi_mac", wifiMac)
                    .build().toString();

            Request request = new Request.Builder().url(urlWithParams).build();

            OkHttpClient client = new OkHttpClient();

            OkHttpClient eagerClient = client.newBuilder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build();

            try
            {
                eagerClient.newCall(request).enqueue(new Callback()
                {
                    @Override
                    public void onFailure(Call call, final IOException e)
                    {
                        Log.w(APP_LOG_TAG, className + getString(R.string.unable_to_perform_registration) + ", OkHttp request failure : " + e.getMessage());

                        dismissProgressbar();
                        enableRegBtn();
                        showResultDialog(false, "Request timeout");
                    }

                    @Override
                    public void onResponse(final Call call, final Response response) throws IOException
                    {

                        if (response.isSuccessful())
                        {
                            activity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        String responseBody = response.body().string();
                                        response.body().close();
                                        parseResponse(responseBody);
                                    } catch (Exception ex)
                                    {
                                        showResultDialog(false, "Error in received response.");
                                    }
                                }
                            });
                        }
                        dismissProgressbar();
                        enableRegBtn();
                    }
                });
            } catch (Exception e)
            {
                e.printStackTrace();
                Log.d(APP_LOG_TAG, className + getString(R.string.unable_to_perform_registration));
                dismissProgressbar();
                enableRegBtn();
            }
        } else
        {
            Log.d(APP_LOG_TAG, className + getString(R.string.unable_to_perform_registration));
            Log.d(APP_LOG_TAG, className + ": some of request parameters are missing");
            dismissProgressbar();
            enableRegBtn();
        }
    }

    private void disableRegBtn()
    {
        if (!activity.isDestroyed() && !activity.isFinishing() && this.isVisible())
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    btnRegister.setEnabled(false);
                    btnRegister.setAlpha(0.4f);
                }
            });
        }
    }

    private void enableRegBtn()
    {
        if (!activity.isDestroyed() && !activity.isFinishing() && this.isVisible())
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    btnRegister.setEnabled(true);
                    btnRegister.setAlpha(1.0f);
                }
            });
        }
    }

    private void showResultDialog(final boolean success, final String msg)
    {
        if (activity.isDestroyed() || activity.isFinishing() || !this.isVisible())
        {
            Log.d(APP_LOG_TAG, className + ": Unable to show result dialog, activity is not active");
            return;
        }

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                String title, btnText;
                String message;

                if (success)
                {
                    title = getString(R.string.registration_succeeded);
                    message = getString(R.string.thank_you_for_patience);
                    btnText = getString(R.string.ok);
                } else
                {
                    title = getString(R.string.registration_failed);
                    message = "No group for this group code";
                    btnText = getString(R.string.back);
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                builder.setTitle(title);
                builder.setMessage(message);
                builder.setPositiveButton(btnText, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (success)
                        {
                            File fileLastSyncTimestamp = new File(DATA_PATH + "/" + CONFIGS_FOLDER_NAME + "/lastSyncTimestamp.evt");

                            if (fileLastSyncTimestamp.exists())
                            {
                                SystemUtils.restartActivity(activity, false);
                            } else
                            {
                                mCommunicator.replaceFragment(FragShowDiagAndUpdateContent.class.getSimpleName(), new FragShowDiagAndUpdateContent());
                            }

                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        };

        activity.runOnUiThread(runnable);
    }

    private void setupProgressbar()
    {
        if (requestServerDialog == null)
        {
            requestServerDialog = new ProgressDialog(activity);
        }
        requestServerDialog.setTitle(getString(R.string.registering_this_screen));
        String msg = getString(R.string.this_can_take_up_to_a_minute) + "\n" + getString(R.string.thanks_for_your_patience);
        requestServerDialog.setMessage(msg);
        requestServerDialog.setIndeterminate(true);
        requestServerDialog.setCancelable(false);
    }

    private void showProgressbar()
    {
        if (requestServerDialog != null && !requestServerDialog.isShowing())
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    requestServerDialog.show();
                }
            });
        }
    }

    private void dismissProgressbar()
    {
        if (requestServerDialog != null && requestServerDialog.isShowing())
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    requestServerDialog.dismiss();
                }
            });
        }
    }

    private void parseResponse(String response)
    {
        String s = response;

        try
        {
            JSONObject json = new JSONObject(response);

            if (isResponseOk(json))
            {
                SharedPreferences mSettings = activity.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                mSettings.edit().putBoolean("unit_id_was_registered", true).apply();
                showResultDialog(true, "Thanks for your patience");
            } else
            {
                showResultDialog(false, "Error: No group for the supplied group code.");
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
            showResultDialog(false, "Error in received data");
        }
    }

    private boolean isResponseOk(JSONObject jsonObject)
    {
        boolean result = true;

        try
        {
            String strResult = jsonObject.getString("success");

            if(strResult.equals("false"))
            {
                result = false;
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
