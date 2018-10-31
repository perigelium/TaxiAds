package com.edisoninteractive.inrideads.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


/**
 * Created by mdumik on 17.01.2018.
 */

public class BluetoothServerConnection {

    private static final String TAG = "BLUETOOTH";

    private static final String NAME = "com.edisoninteractive.inrideads.Bluetooth";
    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private BluetoothAdapter mBluetoothAdapter;
    private DeviceConnectionListener connectionListener;
    private BluetoothManager btManager;
    private Context context;
    private ReadingThread readingThread;
    private WritingThread writingThread;
    private AcceptThread acceptThread;

    public BluetoothServerConnection(Context context, DeviceConnectionListener connectionListener) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.connectionListener = connectionListener;
        this.context = context;
        this.btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public void start() {
        if (acceptThread != null) {
            acceptThread.cancel();
        }
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public void stop() {
        if (acceptThread != null) {
            acceptThread.cancel();
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        private AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: AcceptThread constructor: " +
                        "trying to listen to bluetooth connections");
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: AcceptThread: constructor " +
                        "Socket's lister() method failed " + e.getMessage());
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: AcceptThread run() " +
                            "trying accept socket");
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: AcceptThread run() " +
                            "IOException while accepting socket connection " + e.getMessage());
                    e.printStackTrace();
                    break;
                }

                if (socket != null) {
                    manageMyConnectedSocket(socket);
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: AcceptThread cancel() " +
                        "trying to close socket");
                mmServerSocket.close();
            } catch (IOException e) {
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: AcceptThread cancel() " +
                        "Could not close the connect socket " + e.getMessage());
            }
        }
    }

    private void manageMyConnectedSocket(final BluetoothSocket socket) {

        if (socket.isConnected()) {
            Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: manageMyConnectedSocket() " +
                    "Socket is connected!");

            if (null != readingThread) {
                readingThread.cancel();
                readingThread = null;
            }
            readingThread = new ReadingThread(socket);
            readingThread.start();

            if (null != writingThread) {
                writingThread.cancel();
                writingThread = null;
            }
            writingThread = new WritingThread(socket);
            connectionListener.onDeviceReadyToWrite(writingThread);
        }
    }

    /**
     * CLASS FOR GETTING DATA VIA SOCKET
     */

    public class ReadingThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ReadingThread(BluetoothSocket socket) {
            mmSocket = socket;

            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: ReadingThread: constructor: " +
                        "Error occurred when creating input stream " + e.getMessage());
                e.printStackTrace();
            }

            mmInStream = tmpIn;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: ReadingThread: run() " +
                    "InputStream reading incoming messages");
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    String incomingMessage = new String(mmBuffer, 0, numBytes);
                    Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: ReadingThread: run() " +
                            "Message received = " + incomingMessage);
                    connectionListener.onMessageReceived(incomingMessage);
                } catch (IOException e) {
                    Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: ReadingThread: run() " +
                            " IOException: input stream was disconnected " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: ReadingThread: cancel() " +
                        "trying to close Socket");
                mmSocket.close();
            } catch (IOException e) {
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: ReadingThread: cancel() " +
                        "IOException: could not close the connect socket " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public class WritingThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;
        private String msg;

        public WritingThread(BluetoothSocket socket) {
            mmSocket = socket;
            OutputStream tmpOut = null;

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: WritingThread: constructor " +
                        "Error occurred when creating output stream " + e.getMessage());
                e.printStackTrace();
            }

            mmOutStream = tmpOut;
        }

        public void sendMessage(String msg) {
            this.msg = msg;
            run();
        }

        public void run() {
            try {
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: WritingThread run() " +
                        "trying to write message to OutputStream");
                mmOutStream.write(msg.getBytes());
            } catch (IOException e) {
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: WritingThread run() " +
                        "Error occurred when sending message");
                e.printStackTrace();
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: WritingThread cancel() " +
                        "trying to close socket");
                mmSocket.close();
            } catch (IOException e) {
                Log.d(GlobalConstants.APP_LOG_TAG, "Bluetooth/BluetoothServerConnection: WritingThread cancel() " +
                        "Could not close the connect socket " + e.getMessage());
            }
        }
    }
}
