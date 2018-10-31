package com.edisoninteractive.inrideads.Bluetooth;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.MainActivity;
import com.edisoninteractive.inrideads.R;

/**
 * Created by mdumik on 22.01.2018.
 */

public class BluetoothConnection {

    private MainActivity activity;

    private BluetoothService bluetoothService;

    private boolean isBluetoothServiceBound;

    public BluetoothConnection(Context context) {
        Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothConnection constructor called from MainActivity");
        this.activity = (MainActivity) context;
    }

    public void setBluetoothServiceBound(boolean isBound) {
        isBluetoothServiceBound = isBound;
    }

    public boolean isBluetoothServiceBound() {
        return isBluetoothServiceBound;
    }

    public ServiceConnection getBluetoothServiceConnection() {
        return mBluetoothServiceConnection;
    }

    private ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(GlobalConstants.APP_LOG_TAG,"BluetoothConnection: ServiceConnection: onServiceConnected() called");
//            bluetoothService = (BluetoothService) binder;
            bluetoothService = ((BluetoothService.MyBinder) binder).getService();
            bluetoothService.setHandler(bluetoothHandler);
            isBluetoothServiceBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothConnection: ServiceConnection: onServiceDisconnected() called");
            bluetoothService = null;
            isBluetoothServiceBound = false;
        }
    };

    private Handler bluetoothHandler = new Handler(new android.os.Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            Log.d(GlobalConstants.APP_LOG_TAG, "BluetoothConnection: bluetoothHandler: handleMessage() called");

            if (message.what == BluetoothService.BLUETOOTH_MESSAGE_WHAT) {

                final String price = (String) message.obj;

                if (null != price && !price.isEmpty()) {

                    showPaymentConfirmationDialog(price);
                }
            }
            return false;
        }
    });

    private void showPaymentConfirmationDialog(String price) {

        View view = LayoutInflater.from(activity).inflate(R.layout.confirmation_payment_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(view)
                .setCancelable(false)
                .create();

        TextView tvPrice = (TextView) view.findViewById(R.id.tv_price);
        tvPrice.setText(price);

        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBluetoothServiceBound) {
                    bluetoothService.onPaymentResult(false);
                }
                dialog.dismiss();
            }
        });

        Button btnConfirm = (Button) view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBluetoothServiceBound) {
                    bluetoothService.onPaymentResult(true);
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
