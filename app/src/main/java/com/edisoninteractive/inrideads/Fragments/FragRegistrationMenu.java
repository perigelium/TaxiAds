package com.edisoninteractive.inrideads.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.edisoninteractive.inrideads.Interfaces.Communicator;
import com.edisoninteractive.inrideads.R;
import com.edisoninteractive.inrideads.Utils.ImageUtils;
import com.edisoninteractive.inrideads.Utils.NetworkUtils;
import com.edisoninteractive.inrideads.Utils.SystemUtils;
import com.edisoninteractive.inrideads.Utils.ViewUtils;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_PREFERENCES;
import static com.edisoninteractive.inrideads.Entities.GlobalConstants.UNIT_ID;

/**
 * Created by Alex Angan one fine day
 */

public class FragRegistrationMenu extends Fragment implements View.OnClickListener
{
    Activity activity;
    private Communicator mCommunicator;
    private String unitId;
    private Button btnRegisterByCompCode;
    private Button btnNext;
    private Button btnRegisterByInstallCode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mCommunicator = (Communicator) activity;

        unitId = UNIT_ID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.frag_registration_menu, container, false);

        btnRegisterByCompCode = (Button) rootView.findViewById(R.id.btnRegisterByCompanyCode);
        btnRegisterByInstallCode = (Button) rootView.findViewById(R.id.btnRegisterByInstallCode);
        btnNext = (Button) rootView.findViewById(R.id.btnNext);
        btnRegisterByCompCode.setOnClickListener(this);
        btnRegisterByInstallCode.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        TextView tvUnitId = (TextView) rootView.findViewById(R.id.tvUnitId);
        tvUnitId.setText(unitId);

        ImageView ivUnitIdQRcode = (ImageView) rootView.findViewById(R.id.ivUnitIdQRcode);

        Bitmap bm = ImageUtils.generateQRCode(unitId);

        if(bm != null)
        {
            ivUnitIdQRcode.setImageBitmap(bm);
        }

        TextView tvBuildVersion = (TextView) rootView.findViewById(R.id.tvBuildVersion);

        String appVersionString = SystemUtils.getAppVersionString(activity);
        tvBuildVersion.setText(appVersionString);

        return rootView;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnRegisterByCompanyCode:
                if(!NetworkUtils.isNetworkAvailable(activity))
                {
                    ViewUtils.showToastMessage(activity, "Internet connection not available");
                    return;
                }
                btnRegisterByCompCode.setAlpha(0.4f);
                mCommunicator.replaceFragment(FragRegisterCompanyCode.class.getSimpleName(), new FragRegisterCompanyCode());
                break;

            case R.id.btnRegisterByInstallCode:
                if(!NetworkUtils.isNetworkAvailable(activity))
                {
                    ViewUtils.showToastMessage(activity, "Internet connection not available");
                    return;
                }
                btnRegisterByInstallCode.setAlpha(0.4f);
                mCommunicator.replaceFragment(FragRegisterInstallCode.class.getSimpleName(), new FragRegisterInstallCode());
                break;

            case R.id.btnNext:

                btnNext.setAlpha(0.4f);
                SharedPreferences mSettings = activity.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                mSettings.edit().putBoolean("unit_id_was_registered", true).apply();

                if(! mSettings.getBoolean("SyncDataCompleted", false))
                {
                    mCommunicator.replaceFragment(FragShowDiagAndUpdateContent.class.getSimpleName(), new FragShowDiagAndUpdateContent());
                }
                else
                {
                    SystemUtils.restartActivity(activity, false);
                }
                break;
        }
    }
}
