package com.project.pupitre;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;


import java.util.ArrayList;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class DoorManagementActivity extends AppCompatActivity {
    /**
     * Tag for Log
     */
    private static final String TAG = "DeviceListActivity";
    DoorManagementActivity doorManagementActivity;
    TextView tvState;
    EditText etNbDoor;
    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btnDraw;
    ArrayList<Button> mBtnList = new ArrayList<>();
    ProgressBar pbLoading;
    private BluetoothChatService mChatService = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private android.support.v7.widget.Toolbar secondToolbar;
    private Context mContext;
    // private BluetoothAdapter mBtAdapter;
    private BluetoothDevice mBTDevice;
    private static OrientationEventListener orientationListener;
    int i;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        mContext = this;
        setContentView(R.layout.activity_door_management);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
        btn5 = (Button) findViewById(R.id.btn5);
        btn6 = (Button) findViewById(R.id.btn6);
        btn7 = (Button) findViewById(R.id.btn7);
        btn8 = (Button) findViewById(R.id.btn8);
        btnDraw = (Button) findViewById(R.id.Draw);
        pbLoading = (ProgressBar)findViewById(R.id.progressBar);

        // Setup the toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_second);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Mode");

        // Add buttons in a button list
        mBtnList.add(btn1);
        mBtnList.add(btn2);
        mBtnList.add(btn3);
        mBtnList.add(btn4);
        mBtnList.add(btn5);
        mBtnList.add(btn6);
        mBtnList.add(btn7);
        mBtnList.add(btn8);

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

        for (i=0;i<mBtnList.size();i++) {
            final int current = i+1;
            mBtnList.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "click detected on door "+current, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "click detected on door "+current);
                    sendMessage("OD"+current);
                }
            });
        }

        // Get device default display
        //final Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        // Called when
        orientationListener = new OrientationEventListener(
                this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int angle) {
                if (pbLoading.getVisibility() == View.GONE)CreateButtons();
            }
        };

        if (orientationListener.canDetectOrientation()){
            orientationListener.enable();
        }
        else{
            Toast.makeText(this,
                    "Can not Detect Orientation", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("pbState", pbLoading.getVisibility());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        pbLoading.setVisibility(savedInstanceState.getInt("pbState"));
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
        String modeName = "";
        switch (id) {
            //"Turn Bluetooth On/Off was clicked
            case R.id.mode1:
                break;
            case R.id.mode2:
                break;
            case R.id.mode3:
                break;
            case R.id.mode4:
        }
        modeName = (String) item.getTitle();
        getSupportActionBar().setTitle(modeName);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        orientationListener.disable();
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
            Toast.makeText(mContext, "Not Connected", Toast.LENGTH_SHORT).show();
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
        //Log.d(TAG, "//////////////////////" + mBTDevice.getName() + "\n" + mBTDevice.getAddress());
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
//                  Toast.makeText(activity, "write : " + writeMessage, Toast.LENGTH_SHORT).show();
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

    private void CreateButtons() {
        int value;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int imp = 0;

        String strNbDoor = etNbDoor.getText().toString();
        int nbDoor = Integer.parseInt(strNbDoor);

        int d = 0, i, j, nRow, nCol;

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float btnSize = 100 * (metrics.densityDpi / 160f);

        if (nbDoor % 2 == 1) {
            imp = 1;
        }

        etNbDoor.setVisibility(View.GONE);
        btnDraw.setVisibility(View.GONE);
        value = this.getResources().getConfiguration().orientation;

        if (nbDoor>=1 && nbDoor<=8)pbLoading.setVisibility(View.GONE);

        if (value == ORIENTATION_PORTRAIT) {
            nCol = (nbDoor == 1) ? 1 : 2;
            nRow = (nbDoor / nCol);

            if (nbDoor != 1) {
                for (i = 0; i < nRow; i++) {
                    for (j = 0; j < nCol; j++) {
                        mBtnList.get(d).setVisibility(View.VISIBLE);
                        mBtnList.get(d).setX(width / 3 * (j + 1) - btnSize / 2);
                        mBtnList.get(d).setY(height / (nRow + 1 + imp) * (i + 1) - btnSize / 2);
                        d++;
                    }
                }
                if (nbDoor % 2 == 1) {
                    mBtnList.get(d).setVisibility(View.VISIBLE);
                    mBtnList.get(d).setX((width / 2) - btnSize / 2);
                    mBtnList.get(d).setY(height / (nRow + 1) * i + 1);
                }
            }
        }

        if (value == ORIENTATION_LANDSCAPE) {
            nRow = (nbDoor <= 3) ? 1 : 2;
            if (nbDoor == 3) {
                nCol = 3;
            } else {
                nCol = (nRow == 1) ? nbDoor : nbDoor / 2;
            }
            if (nbDoor != 1) {
                for (i = 0; i < nRow; i++) {
                    for (j = 0; j < nCol + ((nbDoor == 3) ? 0 : imp) - ((imp == 1 && i == 1) ? 1 : 0); j++) {
                        mBtnList.get(d).setVisibility(View.VISIBLE);
                        mBtnList.get(d).setX(width / (nCol + 1 + ((nbDoor == 3) ? 0 : imp)) * (j + 1) - btnSize / 2 + ((imp == 1) ? (i * (width / (nCol + 1 + ((nbDoor == 3) ? 0 : imp)))) / 2 : 0));
                        mBtnList.get(d).setY(height / (nRow + 1) * (i + 1) - btnSize / 2);
                        d++;
                    }
                }
            }
        }
        if (nbDoor == 1) {
            mBtnList.get(0).setVisibility(View.VISIBLE);
            mBtnList.get(0).setX(width / 2 - btnSize / 2);
            mBtnList.get(0).setY(height / 2 - btnSize / 2);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        Log.d("tag", "config changed");
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            Log.d("tag", "Portrait");
        else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            Log.d("tag", "Landscape");
        else
            Log.w("tag", "other: " + orientation);
    }
}



