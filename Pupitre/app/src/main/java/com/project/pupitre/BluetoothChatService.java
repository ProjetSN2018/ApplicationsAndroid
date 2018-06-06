package com.project.pupitre;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 *
 * Cette classe sert a configurer et gérer les connexions Bluetooth avec d'autres appareils.
 * Elle a un thread qui écoute les connexions entrantes,
 * un thread pour la connexion avec un périphérique et
 * un thread pour effectuer des transmissions de données lorsqu'il est connecté.
 */

public class BluetoothChatService {
    // Debugging
    // Débogage
    private static final String TAG = "BluetoothChatService";

    // Name for the SDP record when creating server socket
    // Nom pour le Service de recherche lorsqu'on crée un socket serveur
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    // UUID unique pour cette application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Member fields
    // Données membres
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;

    // Constants that indicate the current connection state
    // Constante qui indiqueront l'état actuel de la connexion
    public static final int STATE_NONE = 0;         // we're doing nothing
                                                    // Rien ne se passe
    public static final int STATE_LISTEN = 1;       // now listening for incoming connections
                                                    // Ecoute les connexions entrantes
    public static final int STATE_CONNECTING = 2;   // now initiating an outgoing connection
                                                    // Initialise une connexion sortante
    public static final int STATE_CONNECTED = 3;    // now connected to a remote device
                                                    // Est connecté a un appareil distant

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * Constructeur. Prépare une nouvelle session BluetoothChat
     *
     * @param context The UI Activity Context
     *                Le context de l'activité interface
     * @param handler A Handler to send messages back to the UI Activity
     *                Un Handler pour renvoyer les message vers l'activité
     */
    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mHandler = handler;
    }

    /**
     * Update UI title according to the current state of the chat connection
     * Met a jour le titre de l'interface pour mettre l'état actuel de la connexion
     */
    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        // Envoie le nouvel état à l'Handler pour que l'interface de l'activité s'actualise
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     * Retourne l'état actuel de la connexion
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     *
     * Démarre le ChatService. Spécifiquement AcceptThread pour commencer
     * une session en mode écoute (serveur). Appelé par l'activité onResume()
     */
    public synchronized void start() {

        // Cancel any thread attempting to make a connection
        // Annule les threads qui éssaient d'établir une connexion
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        // Annule les threads qui executent une connexion
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        // Démarre le thread pour écouter sur un BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
        // Update UI title
        // Met a jour l'interface
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * Démarre ConnectThread pour initialiser la connecxion a un appareil distant.
     *
     * @param device The BluetoothDevice to connect
     *               L'appareil Bluetooth avec lequel il faut se connecter
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     *               Type de sécurité du socket.
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {

        // Cancel any thread attempting to make a connection
        // Annule tout les threads qui essaye d'établir une connexion
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        // Annule tout les threads qui executent une connexion
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        Log.d(TAG, "Connect to :\n" + device.getName()+ device.getAddress());
        // Start the thread to connect with the given device
        // Démarre le thread pour se connecter avec l'appareil donné
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        // Update UI title
        // Met a jour le titre de l'interface
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * Démarre ConnectThread pour gérer une connexion Bluetooth
     *
     * @param socket The BluetoothSocket on which the connection was made
     *               Le BluetoothSocket sur lequel la connxion est établie
     * @param device The BluetoothDevice that has been connected
     *               Le BluetoothDevice qui a été connecté
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {

        // Cancel the thread that completed the connection
        // Annule le thread qui a terminé une connexion
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        // Annule les threads
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        // Annule le thread d'acceptation puisque nous voulons nous connecter qu'a un seul appareil
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        // Démarre le thread pour gérer la connection et executer les transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        // Renvoie le nom de l'appareil connecté vers l'activité d'interface
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        // Update UI title
        // Met à jour le titre de l'interface
        updateUserInterfaceTitle();
    }

    /**
     * Stop all threads
     * Arret de tout les threads
     */
    public synchronized void stop() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        mState = STATE_NONE;
        // Update UI title
        // Met à jour le titre de l'interface
        updateUserInterfaceTitle();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * Ecrit dans le ConnectedThread de manière non-synchronisée
     *
     * @param out The bytes to write
     *            Les octets à écrires
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        // Création d'un objete temporaire
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        // Synchronise une copie du ConnectThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        // Effectue une écriture non-synchronisée
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     * Indique que la tentative de connexion a échouée et notifie l'activité interface
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        // Renvoie un message d'erreur à l'activité
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect to device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
        // Met à jour le titre de l'interface
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        // Démarre le service afin de redémarrer l'écoute
        BluetoothChatService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     * Indique que la tentative de connexion a été interompu et notifie l'activité interface
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        // Renvoie un message d'erreur à l'activité
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
        // Met à jour le titre de l'interface

        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        // Démarre le service afin de redémarrer l'écoute

        BluetoothChatService.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     *
     * Ce thread est actif lors de l'écoute de connexion entrante.
     * Il se comporte comme un client côté serveur.
     * Il fonctionne jusqu'à ce qu'une connexion soit acceptée (ou annulée).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        // Le socket serveur local
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {

            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run() {
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            // Ecoute le socket serveur si aucune conncxion est en cours
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a successful connection or an exception
                    // Appel bloquant qui retourne uniquement sur une connexion réussie ou une exeption
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // If a connection was accepted
                // Si la connexion est acceptée
                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Normal situation. Start the connected thread.
                                // Situation normal. Démarre le thread connecté.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                // Si le socket est pret ou déja connecté, termine la connexion.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                }
                                break;
                        }
                    }
                }
            }

        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     *
     * Ce thread s'exécute lors d'une tentative de connexion sortante
     * avec un appareil. Il court droit à travers;
     * la connexion réussit ou échoue.
     */

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            // Récupère un BluetoothSocket pour une connexion avec un Appareil Bluetooth donné.
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run() {
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            // Annule toujours la recherche, elle ralentit la connexion
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            // Fait une connexion vers le BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                // Appel bloquant qui retourne uniquement sur une connexion réussie ou une exeption

                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                // Ferme le Socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            // Réinitialise le ConnectThread puisque nous avons terminé
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            // Démarre le ConnectedThread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     *
     *
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            // Récupère les flux d'entrée et sortie du BluetoothSocket
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            // Continue d'écouter le flux d'entré tant que nous sommes connecté
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    // Lit le flux d'entrée
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    // Envoie des octets obtenus vers l'activité d'interface
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                // Renvoie le message envoyé vers l'activité d'interface
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
