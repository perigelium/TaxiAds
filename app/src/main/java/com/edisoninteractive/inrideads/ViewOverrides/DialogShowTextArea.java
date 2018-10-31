package com.edisoninteractive.inrideads.ViewOverrides;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.edisoninteractive.inrideads.R;


public class DialogShowTextArea extends DialogFragment
{
    String strMessage;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.FragmentDialogStyle);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialog = inflater.inflate(R.layout.show_textarea_dialog, null);

        TextView tvData = (TextView) dialog.findViewById(R.id.tvData);

        if (getArguments() != null)
        {
            strMessage = getArguments().getString("message");
            tvData.setText(strMessage);
        }

        builder.setView(dialog)
                // Add action buttons
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        DialogShowTextArea.this.getDialog().cancel();
                    }
                });


        return builder.create();
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }
}
