package com.edisoninteractive.inrideads.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.edisoninteractive.inrideads.Entities.Config_JS;
import com.edisoninteractive.inrideads.Interfaces.Communicator;
import com.edisoninteractive.inrideads.R;
import com.edisoninteractive.inrideads.Utils.FileUtils;
import com.edisoninteractive.inrideads.Utils.ImageUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UNIT_ID;


/**
 * Created by mdumik on 13.12.2017.
 */

public class FragLogin extends Fragment implements View.OnClickListener
{

    Activity activity;
    Context context;
    View rootView;

    private Communicator mCommunicator;
    private Button backBtn, loginBtn;
    private EditText passwordField;
    private TextView textId, timer;
    private ImageView ivQRCode;
    private TextClock clock;
    private TextView tvBuildVer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mCommunicator = (Communicator) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.login_frag, container, false);

        initializeVariables();
        setupPasswordField();
        generateQRCode();
        return rootView;
    }

    private void initializeVariables()
    {
        context = rootView.getContext();
        backBtn = (Button) rootView.findViewById(R.id.btn_back);
        backBtn.setOnClickListener(this);
        loginBtn = (Button) rootView.findViewById(R.id.btn_login);
        loginBtn.setOnClickListener(this);
        passwordField = (EditText) rootView.findViewById(R.id.et_password);

        tvBuildVer = (TextView) rootView.findViewById(R.id.tv_build_ver);
        String strAppVersion = "ver. " + SystemUtils.getAppVersionString(context);
        tvBuildVer.setText(strAppVersion);
        textId = (TextView) rootView.findViewById(R.id.tv_id);

        String id = UNIT_ID;
        textId.setText(id);

        clock = (TextClock) rootView.findViewById(R.id.clock);
        clock.setFormat12Hour("hh:mm:ss a");
        clock.setFormat24Hour(null);

        ivQRCode = (ImageView) rootView.findViewById(R.id.iv_qr_code);
    }

    private void setupPasswordField()
    {
        passwordField.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    try
                    {
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    } catch (NullPointerException ex)
                    {
                    }
                }
            }
        });
        passwordField.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    loginBtn.callOnClick();
                    return true;
                }
                return false;
            }
        });
    }

    private void generateQRCode()
    {
        Bitmap myBitmap = ImageUtils.generateQRCode(UNIT_ID);
        if (myBitmap != null)
        {
            ivQRCode.setImageBitmap(myBitmap);
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_back:
                backBtn.setAlpha(0.2f);
                backBtn.setEnabled(false);
                SystemUtils.restartActivity(activity, false);
                break;

            case R.id.btn_login:
                if (!TextUtils.isEmpty(passwordField.getText().toString()) && doesPasswordMatch())
                {
                    mCommunicator.replaceFragment(FragServiceMenu.class.getSimpleName(), new FragServiceMenu());
                } else
                {
                    showErrorPopup();
                }
                break;
        }
    }

    private boolean doesPasswordMatch()
    {
        int adminPassword = -9999;
        int typedPassword = -1;

        try
        {
            Config_JS config_js = FileUtils.readMediaInterfaceConfigObject();
            adminPassword = (int) Math.round(config_js.params.adminPassword);
            typedPassword = Integer.valueOf(passwordField.getText().toString());
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return adminPassword == typedPassword;
    }

    private void showErrorPopup()
    {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        View dialogView = LayoutInflater.from(context).inflate(R.layout.error_password_popup, null);
        Button ok = (Button) dialogView.findViewById(R.id.btn_ok);
        ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        dialog.setView(dialogView);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        passwordField.setText("");
    }
}
