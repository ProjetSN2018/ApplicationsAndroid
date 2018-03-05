package com.project.xyz;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by eleve on 12/02/2018.
 */

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "XYZ";
    private static final UUID SECURE_UUID = UUID.fromString("2bf5a2a6-1657-49da-8371-01c2cba68832");
    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;
    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context){
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private class AcceptThread extends Thread {

        // Local Server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            Log.d(TAG, "---- AcceptThread() ----");
            BluetoothServerSocket tmp = null;

            //Creating a new listening server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, SECURE_UUID);

                Log.d(TAG, "AcceptThread: Setting up server using:" + SECURE_UUID);
            }catch(IOException e){
                Log.e(TAG,"IOException : "+ e.getMessage());
            }
            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "---- AcceptThread run() ----");
            BluetoothSocket socket = null;
            try{
                socket = mmServerSocket.accept();
            }catch(IOException e){
                Log.e(TAG,"IOException : "+ e.getMessage());
            }
            if (socket != null){
                connected(socket,mmDevice);
            }
        }

        // Canceling AcceptThread
        public void cancel(){
            Log.d(TAG, "---- AcceptThread cancel() ----");
            try {
                mmServerSocket.close();
            }catch(IOException e){
                Log.e(TAG,"Close of AcceptThead ServerSocket failed. "+ e.getMessage());
            }
        }
    }

    /**
     * This Thread runs while attempting to make an outgoing connection with a device.
     * It runs straight through; the connection either success or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            mmDevice = device;
            deviceUUID = uuid;
        }
        public void run(){
            Log.d(TAG, "---- ConnectThread run() ----");
            BluetoothSocket tmp = null;

            //Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            }catch(IOException e){
                Log.e(TAG,"Could not create Socket : "+ e.getMessage());
            }
            mmSocket = tmp;
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
                Log.d(TAG, "---- mmSocket.connect(); ----");
            } catch (IOException e) {
                //Closing Socket
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    Log.e(TAG,"Unable to close connection in socket : "+ e.getMessage());
                }
                Log.d(TAG,"Couldn't connect to the UUID: "+ SECURE_UUID);
            }
            connected(mmSocket, mmDevice);
        }
        public void cancel() throws IOException {
            Log.d(TAG, "---- ConnectThread cancel() ----");
            try{
                mmSocket.close();
                Log.d(TAG, "---- mmSocket.close(); ----");
            }catch(IOException e){
                Log.e(TAG,"Closing of mmSocket in ConnectThread : "+ e.getMessage());
            }
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "---- ConnectThread start() ----");
        if (mConnectThread != null){
            try {
                mConnectThread.cancel();
            } catch (IOException e) {
                Log.e(TAG,"Couldn't cancel the ThreadConnect");
            }
            mConnectThread = null;
        }
        if(mSecureAcceptThread == null){
            mSecureAcceptThread = new AcceptThread();
            mSecureAcceptThread.start();
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection.
     * Then ConnectThread starts and attempts to make a
     * connection with the other devices AcceptThread.
     */
    public void startClient(BluetoothDevice device , UUID uuid){
        // Init progress dialog
        Log.d(TAG, "---- ConnectThread startClient() ----");
        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth","Please Wait...", true);
        mConnectThread = new ConnectThread(device,uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "---- ConnectedThread() ----");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Dismiss the progress dialog when connection is established
            try{
                mProgressDialog.dismiss();
            }catch(NullPointerException e){
                Log.e(TAG,"No progressDialog to dismiss");
            }

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Couldn't get input and/or output stream");
            }


            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            Log.d(TAG, "---- ConnectedThread run() ----");
            byte[] buffer = new byte[1024]; // Buffer to store the stream

            int bytes; // Bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while(true){
                Toast.makeText(mContext, "Bluetooth has been disabled.", Toast.LENGTH_SHORT).show();
                try {

                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG,"IncomingMessage: " + incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "Couldn't read the Inputstream");
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device
        public void write(byte[] bytes){
            Log.d(TAG, "---- ConnectedThread write() ----");
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "Write:" + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG,"Couldn't write in the Outputstream"+ e.getMessage());
            }
        }
        // Call this from the main activity to shutdown the connection
        public void cancel(){
            Log.d(TAG, "---- ConnectedThread cancel() ----");
            try{
                mmSocket.close();
            }catch(IOException e){ }
        }
    }

    private void connected(BluetoothSocket mmSocket,BluetoothDevice mmDevice){
        Log.d(TAG, "---- connected() ----");
        mConnectedThread = new ConnectedThread(mmSocket);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     */
    public void write(byte[] out){
        Log.d(TAG, "---- write out ----");
        // Synchronize a copy of the ConnectedThread and Perform the write unsyncronized
        mConnectedThread.write(out);
    }
}
