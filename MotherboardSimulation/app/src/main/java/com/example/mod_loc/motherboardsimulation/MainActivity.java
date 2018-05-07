package com.example.mod_loc.motherboardsimulation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    MainActivity mainActivity;
    BluetoothAdapter mBtAdapter;
    Button mkdiscov, btonoff, btnInit, btnCall;
    TextView tvState;
    EditText etNbDoor, DoorCall;
    private BluetoothDevice mBTDevice;
    private BluetoothChatService mChatService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mkdiscov = (Button) findViewById(R.id.mkdiscov);
        btonoff = (Button) findViewById(R.id.btonoff);
        tvState = (TextView) findViewById(R.id.tvState);
        btnInit = (Button) findViewById(R.id.btnInit);
        etNbDoor = (EditText) findViewById(R.id.etNbDoor);
        btnCall = (Button) findViewById(R.id.btnCall);
        DoorCall = (EditText) findViewById(R.id.DoorCall);

        mChatService = new BluetoothChatService(mainActivity, mHandler);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        btonoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDisableBT();
            }
        });
        mkdiscov.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDiscoverable();
            }
        });
        btnInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBtAdapter.isEnabled()){
                    mChatService.start();
                    Log.d(TAG, "Initialisation started");
                    Toast.makeText(getApplicationContext(), "Initialisation started", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Bluetooth must be turned on.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strDoorCall = DoorCall.getText().toString();
                sendMessage("CD-"+strDoorCall);
            }
        });
    }

    //Turn on/off the Bluetooth depending on the actual state
    public void enableDisableBT(){
        if(mBtAdapter == null){
        }
        //If Bluetooth is enabled, disable it.
        if(!mBtAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiverBluetoothOnOff, BTIntent);
        }
        //If Bluetooth is disabled, enable it.
        if(mBtAdapter.isEnabled()){
            mBtAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiverBluetoothOnOff, BTIntent);
        }
    }

    private final BroadcastReceiver mBroadcastReceiverBluetoothOnOff = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action.equals(mBtAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBtAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(getApplicationContext(), "Bluetooth has been disabled.", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(getApplicationContext(), "Bluetooth has been enabled.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    public void makeDiscoverable(){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }


    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            Activity activity = mainActivity;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            tvState.setText("Not connected");
                            break;
                        case BluetoothChatService.STATE_CONNECTED:
                            tvState.setText("Connected");
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            tvState.setText("Connecting...");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_SHORT).show();

                    switch (readMessage){
                        case "Incoming":
                            String strNbDoor = etNbDoor.getText().toString();
                            sendMessage("NBD-"+strNbDoor);
                            Toast.makeText(getApplicationContext(), "NBD-"+strNbDoor, Toast.LENGTH_SHORT).show();
                            break;
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name

                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mBTDevice.getName(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

        private void sendMessage(String message) {
            // Check that we're actually connected before trying anything
            if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                Toast.makeText(mainActivity.getApplicationContext(), "Not Connected", Toast.LENGTH_SHORT).show();
                return;
            }
            // Check that there's actually something to send
            if (message.length() > 0) {
                // Get the message bytes and tell the BluetoothChatService to write
                byte[] send = message.getBytes();
                mChatService.write(send);
            }
        }
    };

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(mainActivity.getApplicationContext(), "Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

}


