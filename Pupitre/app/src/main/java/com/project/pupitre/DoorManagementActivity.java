package com.project.pupitre;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Set;

public class DoorManagementActivity extends Activity {
    /**
     * Tag for Log
     */
    private static final String TAG = "DeviceListActivity";

    DoorManagementActivity doorManagementActivity;
    TextView tvState;
    EditText etNbDoor;
    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btnDraw;
    private BluetoothChatService mChatService = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private android.support.v7.widget.Toolbar secondToolbar;
    private BluetoothAdapter mBtAdapter;
    private BluetoothDevice mBTDevice;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        setContentView(R.layout.activity_door_management);

        int value;
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
        btn5 = (Button) findViewById(R.id.btn5);
        btn6 = (Button) findViewById(R.id.btn6);
        btn7 = (Button) findViewById(R.id.btn7);
        btn8 = (Button) findViewById(R.id.btn8);
        btnDraw = (Button) findViewById(R.id.Draw);

        etNbDoor = (EditText) findViewById(R.id.etNbDoor);
        tvState = (TextView) findViewById(R.id.tvState);
        secondToolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(secondToolbar);

        // Get the info from the previous activity
        mBTDevice = getIntent().getExtras().getParcelable("device");

        // Get local bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mChatService = new BluetoothChatService(doorManagementActivity, mHandler);
        connectDevice(mBTDevice, false);

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(doorManagementActivity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            doorManagementActivity.finish();
        }
        btnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateButtons();
            }
        });
    }

    //Displays menu created in menu_main.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_second, menu);
        return true;
    }

    //Detects clicks on menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            //"Turn Bluetooth On/Off was clicked
            case R.id.mode1:
                Toast.makeText(doorManagementActivity, "Mode 1", Toast.LENGTH_LONG).show();
                break;
            case R.id.mode2:
                Toast.makeText(doorManagementActivity, "Mode 2", Toast.LENGTH_LONG).show();
                break;
            case R.id.mode3:
                Toast.makeText(doorManagementActivity, "Mode 3", Toast.LENGTH_LONG).show();
                break;
            case R.id.mode4:
                Toast.makeText(doorManagementActivity, "Mode 4", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(doorManagementActivity, "Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    private void connectDevice(BluetoothDevice mBTDevice, boolean secure) {
        Log.d(TAG, "///////////////////////////////////////////////////////////////" + mBTDevice.getName() + "\n" + mBTDevice.getAddress());
        // Attempt to connect to the device
        mChatService.connect(mBTDevice, false);
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity = doorManagementActivity;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            tvState.setText("Not connected");
                            break;
                        case BluetoothChatService.STATE_CONNECTED:
                            tvState.setText("Connected to " + mBTDevice.getName());
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            tvState.setText("Connecting...");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Toast.makeText(activity, "write : " + writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(activity, "Read : " + readMessage, Toast.LENGTH_SHORT).show();
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
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
        CreateButtons();
    }

    private void CreateButtons() {

        String orientation = "";
        int value;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float btnSize = 100 * (metrics.densityDpi / 160f);

        String strNbDoor = etNbDoor.getText().toString();
        int nbDoor = Integer.parseInt(strNbDoor);

        value = this.getResources().getConfiguration().orientation;

        if (value == Configuration.ORIENTATION_PORTRAIT) {

            orientation = "Portrait";
        }

        if (value == Configuration.ORIENTATION_LANDSCAPE) {

            orientation = "Landscape";
        }

        tvState.setText(" Current Screen Orientation = " + orientation);

        if (orientation == "Landscape") {
            switch (nbDoor) {
                case 1:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 2 - btnSize / 2);
                    btn1.setY(height / 2 - btnSize / 2);
                    break;
                case 2:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 3 - btnSize / 2);
                    btn1.setY(height / 2 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 3 * 2 - btnSize / 2);
                    btn2.setY(height / 2 - btnSize / 2);
                    break;
                case 3:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 4 - btnSize / 2);
                    btn1.setY(height / 2 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 4 * 2 - btnSize / 2);
                    btn2.setY(height / 2 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 4 * 3 - btnSize / 2);
                    btn3.setY(height / 2 - btnSize / 2);
                    break;
                case 4:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 3 - btnSize / 2);
                    btn1.setY(height / 3 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 3 * 2 - btnSize / 2);
                    btn2.setY(height / 3 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 3 - btnSize / 2);
                    btn3.setY(height / 3 * 2 - btnSize / 2);
                    ///
                    btn4.setVisibility(View.VISIBLE);
                    btn4.setX(width / 3 * 2 - btnSize / 2);
                    btn4.setY(height / 3 * 2 - btnSize / 2);
                    break;
                case 5:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 4 - btnSize / 2);
                    btn1.setY(height / 3 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 4 * 2 - btnSize / 2);
                    btn2.setY(height / 3 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 4 * 3 - btnSize / 2);
                    btn3.setY(height / 3 - btnSize / 2);
                    ///
                    btn4.setVisibility(View.VISIBLE);
                    btn4.setX(width / 8 * 3 - btnSize / 2);
                    btn4.setY(height / 3 * 2 - btnSize / 2);
                    ///
                    btn5.setVisibility(View.VISIBLE);
                    btn5.setX(width / 8 * 5 - btnSize / 2);
                    btn5.setY(height / 3 * 2 - btnSize / 2);
                    break;
                case 6:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 4 - btnSize / 2);
                    btn1.setY(height / 3 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 4 * 2 - btnSize / 2);
                    btn2.setY(height / 3 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 4 * 3 - btnSize / 2);
                    btn3.setY(height / 3 - btnSize / 2);
                    ///
                    btn4.setVisibility(View.VISIBLE);
                    btn4.setX(width / 4 - btnSize / 2);
                    btn4.setY(height / 3 * 2 - btnSize / 2);
                    ///
                    btn5.setVisibility(View.VISIBLE);
                    btn5.setX(width / 4 * 2 - btnSize / 2);
                    btn5.setY(height / 3 * 2 - btnSize / 2);
                    ///
                    btn6.setVisibility(View.VISIBLE);
                    btn6.setX(width / 4 * 3 - btnSize / 2);
                    btn6.setY(height / 3 * 2 - btnSize / 2);
                    break;
                case 7:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 5 - btnSize / 2);
                    btn1.setY(height / 3 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 5 * 2 - btnSize / 2);
                    btn2.setY(height / 3 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 5 * 3 - btnSize / 2);
                    btn3.setY(height / 3 - btnSize / 2);
                    ///
                    btn4.setVisibility(View.VISIBLE);
                    btn4.setX(width / 5 * 4 - btnSize / 2);
                    btn4.setY(height / 3 - btnSize / 2);
                    ///
                    btn5.setVisibility(View.VISIBLE);
                    btn5.setX(width / 10 * 3 - btnSize / 2);
                    btn5.setY(height / 5 * 3 - btnSize / 2);
                    ///
                    btn6.setVisibility(View.VISIBLE);
                    btn6.setX(width / 10 * 5 - btnSize / 2);
                    btn6.setY(height / 5 * 3 - btnSize / 2);
                    ///
                    btn7.setVisibility(View.VISIBLE);
                    btn7.setX(width / 10 * 7 - btnSize / 2);
                    btn7.setY(height / 5 * 4 - btnSize / 2);
                    break;
                case 8:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 5 - btnSize / 2);
                    btn1.setY(height / 3 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 5 * 2 - btnSize / 2);
                    btn2.setY(height / 3 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 5 * 3 - btnSize / 2);
                    btn3.setY(height / 3 - btnSize / 2);
                    ///
                    btn4.setVisibility(View.VISIBLE);
                    btn4.setX(width / 5 * 4 - btnSize / 2);
                    btn4.setY(height / 3 - btnSize / 2);
                    ///
                    btn5.setVisibility(View.VISIBLE);
                    btn5.setX(width / 5 - btnSize / 2);
                    btn5.setY(height / 3 * 2 - btnSize / 2);
                    ///
                    btn6.setVisibility(View.VISIBLE);
                    btn6.setX(width / 5 * 2 - btnSize / 2);
                    btn6.setY(height / 3 * 2 - btnSize / 2);
                    ///
                    btn7.setVisibility(View.VISIBLE);
                    btn7.setX(width / 5 * 3 - btnSize / 2);
                    btn7.setY(height / 3 * 2 - btnSize / 2);
                    ///
                    btn8.setVisibility(View.VISIBLE);
                    btn8.setX(width / 5 * 4 - btnSize / 2);
                    btn8.setY(height / 3 * 2 - btnSize / 2);
                    break;
            }
        } else {
            switch (nbDoor) {
                case 1:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 2 - btnSize / 2);
                    btn1.setY(height / 2 - btnSize / 2);
                    break;
                case 2:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 2 - btnSize / 2);
                    btn1.setY(height / 3 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 2 - btnSize / 2);
                    btn2.setY(height / 3 * 2 - btnSize / 2);
                    break;
                case 3:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 2 - btnSize / 2);
                    btn1.setY(height / 4 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 2 - btnSize / 2);
                    btn2.setY(height / 4 * 2 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 2 - btnSize / 2);
                    btn3.setY(height / 4 * 3 - btnSize / 2);
                    break;
                case 4:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 3 - btnSize / 2);
                    btn1.setY(height / 3 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 3 * 2 - btnSize / 2);
                    btn2.setY(height / 3 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 3 - btnSize / 2);
                    btn3.setY(height / 3 * 2 - btnSize / 2);
                    ///
                    btn4.setVisibility(View.VISIBLE);
                    btn4.setX(width / 3 * 2 - btnSize / 2);
                    btn4.setY(height / 3 * 2 - btnSize / 2);
                    break;
                case 5:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 3 - btnSize / 2);
                    btn1.setY(height / 4 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 3 * 2 - btnSize / 2);
                    btn2.setY(height / 4 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 3 - btnSize / 2);
                    btn3.setY(height / 4 * 2 - btnSize / 2);
                    ///
                    btn4.setVisibility(View.VISIBLE);
                    btn4.setX(width / 3 * 2 - btnSize / 2);
                    btn4.setY(height / 4 * 2 - btnSize / 2);
                    ///
                    btn5.setVisibility(View.VISIBLE);
                    btn5.setX(width / 2 - btnSize / 2);
                    btn5.setY(height / 4 * 3 - btnSize / 2);
                    break;
                case 6:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 3 - btnSize / 2);
                    btn1.setY(height / 4 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 3 * 2 - btnSize / 2);
                    btn2.setY(height / 4 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 3 - btnSize / 2);
                    btn3.setY(height / 4 * 2 - btnSize / 2);
                    ///
                    btn4.setVisibility(View.VISIBLE);
                    btn4.setX(width / 3 * 2 - btnSize / 2);
                    btn4.setY(height / 4 * 2 - btnSize / 2);
                    ///
                    btn5.setVisibility(View.VISIBLE);
                    btn5.setX(width / 3 - btnSize / 2);
                    btn5.setY(height / 4 * 3 - btnSize / 2);
                    ///
                    btn6.setVisibility(View.VISIBLE);
                    btn6.setX(width / 3 * 2 - btnSize / 2);
                    btn6.setY(height / 4 * 3 - btnSize / 2);
                    break;
                case 7:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 3 - btnSize / 2);
                    btn1.setY(height / 5 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 3 * 2 - btnSize / 2);
                    btn2.setY(height / 5 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 3 - btnSize / 2);
                    btn3.setY(height / 5 * 2 - btnSize / 2);
                    ///
                    btn4.setVisibility(View.VISIBLE);
                    btn4.setX(width / 3 * 2 - btnSize / 2);
                    btn4.setY(height / 5 * 2 - btnSize / 2);
                    ///
                    btn5.setVisibility(View.VISIBLE);
                    btn5.setX(width / 3 - btnSize / 2);
                    btn5.setY(height / 5 * 3 - btnSize / 2);
                    ///
                    btn6.setVisibility(View.VISIBLE);
                    btn6.setX(width / 3 * 2 - btnSize / 2);
                    btn6.setY(height / 5 * 3 - btnSize / 2);
                    ///
                    btn7.setVisibility(View.VISIBLE);
                    btn7.setX(width / 2 - btnSize / 2);
                    btn7.setY(height / 5 * 4 - btnSize / 2);
                    break;
                case 8:
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setX(width / 3 - btnSize / 2);
                    btn1.setY(height / 5 - btnSize / 2);
                    ///
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setX(width / 3 * 2 - btnSize / 2);
                    btn2.setY(height / 5 - btnSize / 2);
                    ///
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setX(width / 3 - btnSize / 2);
                    btn3.setY(height / 5 * 2 - btnSize / 2);
                    ///
                    btn4.setVisibility(View.VISIBLE);
                    btn4.setX(width / 3 * 2 - btnSize / 2);
                    btn4.setY(height / 5 * 2 - btnSize / 2);
                    ///
                    btn5.setVisibility(View.VISIBLE);
                    btn5.setX(width / 3 - btnSize / 2);
                    btn5.setY(height / 5 * 3 - btnSize / 2);
                    ///
                    btn6.setVisibility(View.VISIBLE);
                    btn6.setX(width / 3 * 2 - btnSize / 2);
                    btn6.setY(height / 5 * 3 - btnSize / 2);
                    ///
                    btn7.setVisibility(View.VISIBLE);
                    btn7.setX(width / 3 - btnSize / 2);
                    btn7.setY(height / 5 * 4 - btnSize / 2);
                    ///
                    btn8.setVisibility(View.VISIBLE);
                    btn8.setX(width / 3 * 2 - btnSize / 2);
                    btn8.setY(height / 5 * 4 - btnSize / 2);
                    break;

            }
        }
    }
}


