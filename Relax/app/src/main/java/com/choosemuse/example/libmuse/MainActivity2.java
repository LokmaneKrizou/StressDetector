/**
 * Example of using libmuse library on android.
 * Interaxon, Inc. 2016
 */

package com.choosemuse.example.libmuse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import com.choosemuse.libmuse.AnnotationData;
import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.LibmuseVersion;
import com.choosemuse.libmuse.MessageType;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConfiguration;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseFileFactory;
import com.choosemuse.libmuse.MuseFileReader;
import com.choosemuse.libmuse.MuseFileWriter;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;
import com.choosemuse.libmuse.MuseVersion;
import com.choosemuse.libmuse.Result;
import com.choosemuse.libmuse.ResultLevel;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;


public class MainActivity2 extends AppCompatActivity implements OnClickListener {


    private final String TAG = "TestLibMuseAndroid";

    /**
     * The MuseManager is how you detect Muse headbands and receive notifications
     * when the list of available headbands changes.
     */
    private MuseManagerAndroid manager;

    /**
     * A Muse refers to a Muse headband.  Use this to connect/disconnect from the
     * headband, register listeners to receive EEG data and get headband
     * configuration and version information.
     */
    private Muse muse;

    /**
     * The ConnectionListener will be notified whenever there is a change in
     * the connection state of a headband, for example when the headband connects
     * or disconnects.
     *
     * Note that ConnectionListener is an inner class at the bottom of this file
     * that extends MuseConnectionListener.
     */
    private ConnectionListener connectionListener;

    /**
     * The DataListener is how you will receive EEG (and other) data from the
     * headband.
     *
     * Note that DataListener is an inner class at the bottom of this file
     * that extends MuseDataListener.
     */
    private DataListener dataListener;

    /**
     * Data comes in from the headband at a very fast rate; 220Hz, 256Hz or 500Hz,
     * depending on the type of headband and the preset configuration.  We buffer the
     * data that is read until we can update the UI.
     *
     * The stale flags indicate whether or not new data has been received and the buffers
     * hold the values of the last data packet received.  We are displaying the EEG, ALPHA_RELATIVE
     * and ACCELEROMETER values in this example.
     *
     * Note: the array lengths of the buffers are taken from the comments in
     * MuseDataPacketType, which specify 3 values for accelerometer and 6
     * values for EEG and EEG-derived packets.
     */
    private final double[] eegBuffer = new double[6];
    private boolean eegStale;
    private final double[] alphaBuffer = new double[6];
    private boolean alphaStale;
    private final double[] thetaBuffer = new double[6];
    private boolean thetaStale;
    private final double[] betaBuffer = new double[6];
    private boolean betaStale;
    private final double[] gammaBuffer= new double[6];
    private boolean gammaStale;

    ArrayList<MuseDataType> museData = new ArrayList<>();

    ArrayList<TextView> alphaText, betaText, thetaText, gammaText;
    /**
     * We will be updating the UI using a handler instead of in packet handlers because
     * packets come in at a very high frequency and it only makes sense to update the UI
     * at about 60fps. The update functions do some string allocation, so this reduces our memory
     * footprint and makes GC pauses less frequent/noticeable.
     */
    private final Handler handler = new Handler();

    /**
     * In the UI, the list of Muses you can connect to is displayed in a Spinner object for this example.
     * This spinner adapter contains the MAC addresses of all of the headbands we have discovered.
     */
    private ArrayAdapter<String> spinnerAdapter;

    /**
     * It is possible to pause the data transmission from the headband.  This boolean tracks whether
     * or not the data transmission is enabled as we allow the user to pause transmission in the UI.
     */
    private boolean dataTransmission = true;

    /**
     * To save data to a file, you should use a MuseFileWriter.  The MuseFileWriter knows how to
     * serialize the data packets received from the headband into a compact binary format.
     * To read the file back, you would use a MuseFileReader.
     */
    private final AtomicReference<MuseFileWriter> fileWriter = new AtomicReference<>();

    /**
     * We don't want file operations to slow down the UI, so we will defer those file operations
     * to a handler on a separate thread.
     */
    private final AtomicReference<Handler> fileHandler = new AtomicReference<>();


    //--------------------------------------
    // Lifecycle / Connection code


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager = MuseManagerAndroid.getInstance();
        manager.setContext(this);
        Log.i(TAG, "LibMuse version=" + LibmuseVersion.instance().getString());
        WeakReference<MainActivity2> weakActivity =
                new WeakReference<>(this);

        connectionListener = new ConnectionListener(weakActivity);
        dataListener = new DataListener(weakActivity);
        manager.setMuseListener(new MuseL(weakActivity));

        // Muse 2016 (MU-02) headbands use Bluetooth Low Energy technology to
        // simplify the connection process.  This requires access to the COARSE_LOCATION
        // or FINE_LOCATION permissions.  Make sure we have these permissions before
        // proceeding.

        // Load and initialize our UI.
        initUI();

//        fileThread.start();

        // Start our asynchronous updates of the UI.
        handler.post(tickUi);
    }

    protected void onPause() {
        super.onPause();
        manager.stopListening();
    }

    public boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.refresh) {
            manager.stopListening();
            manager.startListening();

        } else if (v.getId() == R.id.connect) {
            manager.stopListening();

            List<Muse> availableMuses = manager.getMuses();
            Spinner musesSpinner = findViewById(R.id.muses_spinner);

            if (availableMuses.size() < 1 || musesSpinner.getAdapter().getCount() < 1) {
                Log.w(TAG, "There is nothing to connect to");
            } else {
                muse = availableMuses.get(musesSpinner.getSelectedItemPosition());
                // Unregister all prior listeners and register our data listener to
                // receive the MuseDataPacketTypes we are interested in.  If you do
                // not register a listener for a particular data type, you will not
                // receive data packets of that type.
                muse.unregisterAllListeners();
                muse.registerConnectionListener(connectionListener);
                muse.registerDataListener(dataListener, MuseDataPacketType.EEG);
                muse.registerDataListener(dataListener, MuseDataPacketType.ALPHA_RELATIVE);
                muse.registerDataListener(dataListener, MuseDataPacketType.BETA_RELATIVE);
                muse.registerDataListener(dataListener, MuseDataPacketType.GAMMA_RELATIVE);
                muse.registerDataListener(dataListener, MuseDataPacketType.THETA_RELATIVE);
//                muse.registerDataListener(dataListener, MuseDataPacketType.BATTERY);
//                muse.registerDataListener(dataListener, MuseDataPacketType.DRL_REF);
//                muse.registerDataListener(dataListener, MuseDataPacketType.QUANTIZATION);

                muse.runAsynchronously();
            }

        } else if (v.getId() == R.id.disconnect) {
            if (muse != null) {
                muse.disconnect();
            }

        } else if (v.getId() == R.id.pause) {
            if (muse != null) {
                dataTransmission = !dataTransmission;
                muse.enableDataTransmission(dataTransmission);
            }
        }
    }

    //--------------------------------------
    // Permissions

    /**
     * The ACCESS_COARSE_LOCATION permission is required to use the
     * Bluetooth Low Energy library and must be requested at runtime for Android 6.0+
     * On an Android 6.0 device, the following code will display 2 dialogs,
     * one to provide context and the second to request the permission.
     * On an Android device running an earlier version, nothing is displayed
     * as the permission is granted from the manifest.
     *
     * If the permission is not granted, then Muse 2016 (MU-02) headbands will
     * not be discovered and a SecurityException will be thrown.
     */

    //--------------------------------------
    // Listeners

    /**
     * You will receive a callback to this method each time a headband is discovered.
     * In this example, we update the spinner with the MAC address of the headband.
     */
    public void museListChanged() {
        final List<Muse> list = manager.getMuses();
        spinnerAdapter.clear();
        for (Muse m : list) {
            spinnerAdapter.add(m.getName() + " - " + m.getMacAddress());
        }
    }

    /**
     * You will receive a callback to this method each time there is a change to the
     * connection state of one of the headbands.
     * @param p     A packet containing the current and prior connection states
     * @param muse  The headband whose state changed.
     */
    public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {

        final ConnectionState current = p.getCurrentConnectionState();

        // Format a message to show the change of connection state in the UI.
        final String status = p.getPreviousConnectionState() + " -> " + current;
        Log.i(TAG, status);

        // Update the UI with the change in connection state.
        handler.post(new Runnable() {
            @Override
            public void run() {

                final TextView statusText = findViewById(R.id.con_status);
                statusText.setText(status);

                final MuseVersion museVersion = muse.getMuseVersion();
                final TextView museVersionText = findViewById(R.id.version);
                // If we haven't yet connected to the headband, the version information
                // will be null.  You have to connect to the headband before either the
                // MuseVersion or MuseConfiguration information is known.
                if (museVersion != null) {
                    final String version = museVersion.getFirmwareType() + " - "
                            + museVersion.getFirmwareVersion() + " - "
                            + museVersion.getProtocolVersion();
                    museVersionText.setText(version);
                } else {
                    museVersionText.setText(R.string.undefined);
                }
            }
        });

        if (current == ConnectionState.DISCONNECTED) {
            Log.i(TAG, "Muse disconnected:" + muse.getName());
            // Save the data file once streaming has stopped.
            //saveFile();
            // We have disconnected from the headband, so set our cached copy to null.
            this.muse = null;
        }
    }

    /**
     * You will receive a callback to this method each time the headband sends a MuseDataPacket
     * that you have registered.  You can use different listeners for different packet types or
     * a single listener for all packet types as we have done here.
     * @param pkt     The data packet containing the data from the headband (eg. EEG data)
     * @param muse  The headband that sent the information.
     */
    public void receiveMuseDataPacket(final MuseDataPacket pkt, final Muse muse) {

        final long n = pkt.valuesSize();
        Log.d("packet type", pkt.packetType().toString());

        if (pkt.packetType().equals(MuseDataPacketType.EEG)) {
            getEegChannelValues(eegBuffer, pkt);
            eegStale = true;
        }
        if (pkt.packetType().equals(MuseDataPacketType.THETA_RELATIVE)) {
            getEegChannelValues(thetaBuffer, pkt);
            thetaStale = true;
        }
        if (pkt.packetType().equals(MuseDataPacketType.ALPHA_RELATIVE)) {
            getEegChannelValues(alphaBuffer, pkt);
            alphaStale = true;
        }
        if (pkt.packetType().equals(MuseDataPacketType.BETA_RELATIVE)) {
            getEegChannelValues(betaBuffer, pkt);
            betaStale = true;
        }
        if (pkt.packetType().equals(MuseDataPacketType.GAMMA_RELATIVE)) {
            getEegChannelValues(gammaBuffer, pkt);
            gammaStale = true;
        }
    }

    /**
     * Helper methods to get different packet values. These methods simply store the
     * data in the buffers for later display in the UI.
     */
    private void getEegChannelValues(double[] buffer, MuseDataPacket p) {
        buffer[0] = p.getEegChannelValue(Eeg.EEG1);
        buffer[1] = p.getEegChannelValue(Eeg.EEG2);
        buffer[2] = p.getEegChannelValue(Eeg.EEG3);
        buffer[3] = p.getEegChannelValue(Eeg.EEG4);
        buffer[4] = p.getEegChannelValue(Eeg.AUX_LEFT);
        buffer[5] = p.getEegChannelValue(Eeg.AUX_RIGHT);
    }


    //--------------------------------------
    // UI Specific methods

    /**
     * Initializes the UI of the example application.
     */
    private void initUI() {
        setContentView(R.layout.activity_main2);
        Button refreshButton = findViewById(R.id.refresh);
        refreshButton.setOnClickListener(this);
        Button connectButton = findViewById(R.id.connect);
        connectButton.setOnClickListener(this);
        Button disconnectButton =findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(this);
        Button pauseButton = findViewById(R.id.pause);
        pauseButton.setOnClickListener(this);

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        Spinner musesSpinner = findViewById(R.id.muses_spinner);
        musesSpinner.setAdapter(spinnerAdapter);
    }

    /**
     * The runnable that is used to update the UI at 60Hz.
     */
    private final Runnable tickUi = new Runnable() {
        @Override
        public void run() {
            MuseDataType newMuseData = new MuseDataType();

            if (eegStale)
                updateEeg();

            if (thetaStale) {
                newMuseData.theta = thetaBuffer;
                updateTheta();
            }

            if (alphaStale) {
                newMuseData.alpha = alphaBuffer;
                updateAlpha();
            }

            if (betaStale) {
                newMuseData.beta = betaBuffer;
                updateBeta();
            }

            if (gammaStale) {
                newMuseData.gamma = gammaBuffer;
                updateGamma();
            }

            museData.add(newMuseData);
            printWaveArray(museData.get(museData.size() - 1));

            handler.postDelayed(tickUi, 1000 / 60);
        }
    };

    void printWaveArray(MuseDataType museData) {
        Log.d("Print array alpha", museData.alpha[0] + " | " + museData.alpha[1] + " | " +
                museData.alpha[2] + " | " + museData.alpha[3]);
        Log.d("Print array beta", museData.beta[0] + " | " + museData.beta[1] + " | " +
                museData.beta[2] + " | " + museData.beta[3]);
        Log.d("Print array theta", museData.theta[0] + " | " + museData.theta[1] + " | " +
                museData.theta[2] + " | " + museData.theta[3]);
        Log.d("Print array gamma", museData.gamma[0] + " | " + museData.gamma[1] + " | " +
                museData.gamma[2] + " | " + museData.gamma[3]);
    }

    /**
     * The following methods update the TextViews in the UI with the data
     * from the buffers.
     */
    private void updateWaveValue(ArrayList<TextView> textViews) {
        for(int i = 0; i < 4; i++) {
            textViews.get(i).setText(String.format(Locale.ENGLISH, "%6.2f", thetaBuffer[i]));
        }
    }
    private void updateTheta() {
        TextView elem1 = findViewById(R.id.theta1);
        elem1.setText(String.format(Locale.ENGLISH, "%6.2f", thetaBuffer[0]));
        TextView elem2 = findViewById(R.id.theta2);
        elem2.setText(String.format(Locale.ENGLISH, "%6.2f", thetaBuffer[1]));
        TextView elem3 = findViewById(R.id.theta3);
        elem3.setText(String.format(Locale.ENGLISH, "%6.2f", thetaBuffer[2]));
        TextView elem4 = findViewById(R.id.theta4);
        elem4.setText(String.format(Locale.ENGLISH, "%6.2f", thetaBuffer[3]));
    }

    private void updateGamma() {
        TextView elem1 = findViewById(R.id.gamma1);
        elem1.setText(String.format(Locale.ENGLISH, "%6.2f", gammaBuffer[0]));
        TextView elem2 = findViewById(R.id.gamma2);
        elem2.setText(String.format(Locale.ENGLISH, "%6.2f", gammaBuffer[1]));
        TextView elem3 = findViewById(R.id.gamma3);
        elem3.setText(String.format(Locale.ENGLISH, "%6.2f", gammaBuffer[2]));
        TextView elem4 = findViewById(R.id.gamma4);
        elem4.setText(String.format(Locale.ENGLISH, "%6.2f", gammaBuffer[3]));
    }

    private void updateBeta() {
        TextView elem1 = findViewById(R.id.beta1);
        elem1.setText(String.format(Locale.ENGLISH, "%6.2f", betaBuffer[0]));
        TextView elem2 = findViewById(R.id.beta2);
        elem2.setText(String.format(Locale.ENGLISH, "%6.2f", betaBuffer[1]));
        TextView elem3 = findViewById(R.id.beta3);
        elem3.setText(String.format(Locale.ENGLISH, "%6.2f", betaBuffer[2]));
        TextView elem4 = findViewById(R.id.beta4);
        elem4.setText(String.format(Locale.ENGLISH, "%6.2f", betaBuffer[3]));
    }

    private void updateAlpha() {
        TextView elem1 = findViewById(R.id.elem1);
        elem1.setText(String.format(Locale.ENGLISH, "%6.2f", alphaBuffer[0]));
        TextView elem2 = findViewById(R.id.elem2);
        elem2.setText(String.format(Locale.ENGLISH, "%6.2f", alphaBuffer[1]));
        TextView elem3 = findViewById(R.id.elem3);
        elem3.setText(String.format(Locale.ENGLISH, "%6.2f", alphaBuffer[2]));
        TextView elem4 = findViewById(R.id.elem4);
        elem4.setText(String.format(Locale.ENGLISH, "%6.2f", alphaBuffer[3]));
    }

    private void updateEeg() {
        TextView tp9 = findViewById(R.id.eeg_tp9);
        TextView fp1 = findViewById(R.id.eeg_af7);
        TextView fp2 = findViewById(R.id.eeg_af8);
        TextView tp10 = findViewById(R.id.eeg_tp10);
        tp9.setText(String.format(Locale.ENGLISH, "%6.2f", eegBuffer[0]));
        fp1.setText(String.format(Locale.ENGLISH, "%6.2f", eegBuffer[1]));
        fp2.setText(String.format(Locale.ENGLISH, "%6.2f", eegBuffer[2]));
        tp10.setText(String.format(Locale.ENGLISH, "%6.2f", eegBuffer[3]));
    }

    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    context.openFileOutput("muse_records.json", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Log.d(TAG, "File saved");
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("muse_records.json");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }

        return ret;
    }

    //--------------------------------------
    // Listener translators

    // Each of these classes extend from the appropriate listener and contain a weak reference
    // to the activity. Each class simply forwards the messages it receives back to the Activity.
    class MuseL extends MuseListener {
        final WeakReference<MainActivity2> activityRef;

        MuseL(final WeakReference<MainActivity2> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void museListChanged() {
            activityRef.get().museListChanged();
        }
    }

    class ConnectionListener extends MuseConnectionListener {
        final WeakReference<MainActivity2> activityRef;

        ConnectionListener(final WeakReference<MainActivity2> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {
            activityRef.get().receiveMuseConnectionPacket(p, muse);
        }
    }

    class DataListener extends MuseDataListener {
        final WeakReference<MainActivity2> activityRef;

        DataListener(final WeakReference<MainActivity2> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
            activityRef.get().receiveMuseDataPacket(p, muse);
        }

        @Override
        public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
//            activityRef.get().receiveMuseArtifactPacket(p, muse);
        }
    }
}
