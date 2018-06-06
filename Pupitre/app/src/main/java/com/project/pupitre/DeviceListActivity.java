package com.project.pupitre;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DeviceListActivity extends Activity {
    /**
     * Tag for Log
     */
    private static final String TAG = "DeviceListActivity";

    /**
     * Return Intent extra
     */

    // Member fields
    private BluetoothAdapter mBtAdapter;

    // Newly discovered devices
    // Appareils découverts lors de la recherche
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    public ArrayList<String> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mBTDevicesList = new ArrayList<>();
    public ArrayAdapter<String> pairedDevicesArrayAdapter;

    ListView newDevicesListView;
    ListView pairedListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        // Mise en place de la fenètre
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);

        // Set result CANCELED in case the user backs out
        // Met le resultat à CANCELED en cas de retour de l'utilisateur
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        // Initialisation du bouton pour lancer la recherche d'appareil
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // Initialize array adapters. One for already paired devices and one for newly discovered devices
        // Initialise les array adapters. Un pour les appareils déja appairés et un autre pour les nouveuax appareils découverts
        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        // Recherche et initialise la ListView pour les appareils appairé
        pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        // Recherche et initialise la ListView pour les nouveaux appareils
        newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        // Enregistrement de la diffusion quand un appareil est découvert
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        // Enregistrement de la diffusion quand la recherche est terminée
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        // Récupère le Bluetooth adapter local
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        // Récupère la liste des appareils déja appairée
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        // Si il y a des appareils appairés, les ajoutes dans l'ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        // Verifie si on n'est plus en etat de recherche
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    public void checkBTPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        // Indicate scanning in the title
        // Indique que la recherche est en cours dans le titre
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        // Activation des sous-titres pour les nouveaux appareils
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If the device is already discovering, stop the discovery
        // Si l'appareil est déja en recherche, arrete la recherche.
        if (mBtAdapter.isDiscovering()) {
            Toast.makeText(getApplicationContext(), "Is discovering", Toast.LENGTH_SHORT).show();

            mBtAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            // check BT permissions in manifest
            // Vérification des permissions dans le manifest
            mBtAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, discoverDevicesIntent);
        }
        // If the device isn't discovering, start discovery
        // Si l'appareil n'est pas en recherche, démarre une recherche.
        if (!mBtAdapter.isDiscovering()) {
            //check BT permissions in manifest
            // Vérification des permissions dans le manifest
            checkBTPermissions();
            mBtAdapter.startDiscovery();
            Log.d(TAG, "startDiscovery");

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, discoverDevicesIntent);
        }
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            // Arret de la recherche car elle est gourmande en ressource et la connexion va etre établie
            mBtAdapter.cancelDiscovery();
            // Get the device MAC address, which is the last 17 chars in the View
            // Récupère l'adresse MAC de l'appareil, qui correspond au 17 derniers caractères de la vue
            String info = ((TextView) v).getText().toString();
            // Create the result Intent and include the MAC address
            // Création du résultat de l'Intent et inclue l'adresse MAC

            // If clicked item is not a device
            // Si l'objet cliqué n'est pas un appareil
            if      ((info == getResources().getText(R.string.none_found).toString())||
                    (info == getResources().getText(R.string.none_paired).toString()))
            {

                Log.d(TAG, getResources().getText(R.string.none_found).toString()
                        +" or "
                        +getResources().getText(R.string.none_paired).toString());
            }
            else{
                Log.d(TAG, "----------------------------------\n" + info);
                Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
                Intent DoorManagementIntent = new Intent(DeviceListActivity.this, DoorManagementActivity.class);
                DoorManagementIntent.putExtra("device_info",info);
                if (av != pairedListView) {
                    DoorManagementIntent.putExtra("device", mBTDevicesList.get(arg2));
                }
                startActivity(DoorManagementIntent);
            }
        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // A device have been found
            // Un appareil a été découvert
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                Log.d(TAG, "onReceive : ACTION FOUND.");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Check if the device has already been added to the ListView
                // Vérifie si l'appareil a déja été ajouté dans la ListView
                if (!mBTDevices.contains(device.getName()+"\n"+device.getAddress())) {
                    mBTDevices.add(device.getName() + "\n" + device.getAddress());
                    mBTDevicesList.add(device);
                    String strDevice = (device.getName() + "\n" + device.getAddress());
                    mNewDevicesArrayAdapter.add(strDevice);
                }
            }
            // If no devices have been found after the discovery stopped display that no devices have been found
            // Si aucun appareil n'a été découvert lors de la recherche, affiche qu'aucun appareil n'a été trouvé.
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
}









