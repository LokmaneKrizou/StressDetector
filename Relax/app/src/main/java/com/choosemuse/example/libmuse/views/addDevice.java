package com.choosemuse.example.libmuse.views;


import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.choosemuse.example.libmuse.MuseDataType;
import com.choosemuse.example.libmuse.views.addDevice;
import com.choosemuse.example.libmuse.R;
import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.LibmuseVersion;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseFileWriter;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;
import com.choosemuse.libmuse.MuseVersion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

// Created by Lokmane Krizou on 6/6/2018.
public class addDevice extends Fragment implements View.OnClickListener {
    private final String TAG = "addDevice";
    private MuseManagerAndroid manager;
    private Muse muse;
    private ConnectionListener connectionListener;
    private DataListener dataListener;
    private final double[] eegBuffer = new double[6];
    private boolean eegStale;
    private final double[] alphaBuffer = new double[4];
    private boolean alphaStale;
    private final double[] thetaBuffer = new double[4];
    private boolean thetaStale;
    private final double[] betaBuffer = new double[4];
    private boolean betaStale;
    private final double[] gammaBuffer= new double[4];
    private boolean gammaStale;
    private final Handler handler = new Handler();
    private ArrayAdapter<String> spinnerAdapter;

    private boolean dataTransmission = true;
    private final AtomicReference<MuseFileWriter> fileWriter = new AtomicReference<>();

    private final AtomicReference<Handler> fileHandler = new AtomicReference<>();

    ArrayList<MuseDataType> museData = new ArrayList<>();

    android.view.View rootView;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView   = inflater.inflate(R.layout.fragment_add_dvice, container, false);
        Log.d("Start Fragment",TAG);
        manager = MuseManagerAndroid.getInstance();
        manager.setContext(getActivity());
        Log.i(TAG, "LibMuse version=" + LibmuseVersion.instance().getString());
        WeakReference<addDevice> weakActivity =
                new WeakReference<>(this);

        connectionListener = new ConnectionListener(weakActivity);
        dataListener = new DataListener(weakActivity);
        manager.setMuseListener(new MuseL(weakActivity));

        // Muse 2016 (MU-02) headbands use Bluetooth Low Energy technology to
        // simplify the connection process.  This requires access to the COARSE_LOCATION
        // or FINE_LOCATION permissions.  Make sure we have these permissions before
        // proceeding.

        initUI();
        // Load and initialize our UI.

        // Start our asynchronous updates of the UI.
        handler.post(tickUi);
        return rootView;
    }
//TODO add muse device connection here
    // TODO figure out how to share the muse manager to home activity

    public void onPause() {
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
            Spinner musesSpinner = rootView.findViewById(R.id.muses_spinner);

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
    public void museListChanged() {
        final List<Muse> list = manager.getMuses();
        spinnerAdapter.clear();
        for (Muse m : list) {
            spinnerAdapter.add(m.getName() + " - " + m.getMacAddress());
        }
    }


    public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {

        final ConnectionState current = p.getCurrentConnectionState();

        // Format a message to show the change of connection state in the UI.
        final String status = p.getPreviousConnectionState() + " -> " + current;
        Log.i(TAG, status);

        // Update the UI with the change in connection state.
        handler.post(new Runnable() {
            @Override
            public void run() {

                final TextView statusText = rootView.findViewById(R.id.con_status);
                statusText.setText(status);

                final MuseVersion museVersion = muse.getMuseVersion();
                final TextView museVersionText = rootView.findViewById(R.id.version);
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

    public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
    }

    private void getEegChannelValues(double[] buffer, MuseDataPacket p) {
        buffer[0] = p.getEegChannelValue(Eeg.EEG1);
        buffer[1] = p.getEegChannelValue(Eeg.EEG2);
        buffer[2] = p.getEegChannelValue(Eeg.EEG3);
        buffer[3] = p.getEegChannelValue(Eeg.EEG4);
        buffer[4] = p.getEegChannelValue(Eeg.AUX_LEFT);
        buffer[5] = p.getEegChannelValue(Eeg.AUX_RIGHT);
    }
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
    class MuseL extends MuseListener {
        final WeakReference<addDevice> activityRef;

        MuseL(final WeakReference<addDevice> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void museListChanged() {
            activityRef.get().museListChanged();
        }
    }

    class ConnectionListener extends MuseConnectionListener {
        final WeakReference<addDevice> activityRef;

        ConnectionListener(final WeakReference<addDevice> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {
            activityRef.get().receiveMuseConnectionPacket(p, muse);
        }
    }

    class DataListener extends MuseDataListener {
        final WeakReference<addDevice> activityRef;

        DataListener(final WeakReference<addDevice> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
            activityRef.get().receiveMuseDataPacket(p, muse);
        }

        @Override
        public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
            activityRef.get().receiveMuseArtifactPacket(p, muse);
        }
    }

    private void updateWaveValue(ArrayList<TextView> textViews) {
        for(int i = 0; i < 4; i++) {
            textViews.get(i).setText(String.format(Locale.ENGLISH, "%6.2f", thetaBuffer[i]));
        }
    }
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
    private void updateTheta() {
        TextView elem1 = rootView.findViewById(R.id.theta1);
        elem1.setText(String.format(Locale.ENGLISH, "%6.2f", thetaBuffer[0]));
        TextView elem2 = rootView.findViewById(R.id.theta2);
        elem2.setText(String.format(Locale.ENGLISH, "%6.2f", thetaBuffer[1]));
        TextView elem3 = rootView.findViewById(R.id.theta3);
        elem3.setText(String.format(Locale.ENGLISH, "%6.2f", thetaBuffer[2]));
        TextView elem4 = rootView.findViewById(R.id.theta4);
        elem4.setText(String.format(Locale.ENGLISH, "%6.2f", thetaBuffer[3]));
    }

    private void updateGamma() {
        TextView elem1 = rootView.findViewById(R.id.gamma1);
        elem1.setText(String.format(Locale.ENGLISH, "%6.2f", gammaBuffer[0]));
        TextView elem2 = rootView.findViewById(R.id.gamma2);
        elem2.setText(String.format(Locale.ENGLISH, "%6.2f", gammaBuffer[1]));
        TextView elem3 = rootView.findViewById(R.id.gamma3);
        elem3.setText(String.format(Locale.ENGLISH, "%6.2f", gammaBuffer[2]));
        TextView elem4 = rootView.findViewById(R.id.gamma4);
        elem4.setText(String.format(Locale.ENGLISH, "%6.2f", gammaBuffer[3]));
    }

    private void updateBeta() {
        TextView elem1 = rootView.findViewById(R.id.beta1);
        elem1.setText(String.format(Locale.ENGLISH, "%6.2f", betaBuffer[0]));
        TextView elem2 = rootView.findViewById(R.id.beta2);
        elem2.setText(String.format(Locale.ENGLISH, "%6.2f", betaBuffer[1]));
        TextView elem3 = rootView.findViewById(R.id.beta3);
        elem3.setText(String.format(Locale.ENGLISH, "%6.2f", betaBuffer[2]));
        TextView elem4 = rootView.findViewById(R.id.beta4);
        elem4.setText(String.format(Locale.ENGLISH, "%6.2f", betaBuffer[3]));
    }

    private void updateAlpha() {
        TextView elem1 = rootView.findViewById(R.id.elem1);
        elem1.setText(String.format(Locale.ENGLISH, "%6.2f", alphaBuffer[0]));
        TextView elem2 = rootView.findViewById(R.id.elem2);
        elem2.setText(String.format(Locale.ENGLISH, "%6.2f", alphaBuffer[1]));
        TextView elem3 = rootView.findViewById(R.id.elem3);
        elem3.setText(String.format(Locale.ENGLISH, "%6.2f", alphaBuffer[2]));
        TextView elem4 = rootView.findViewById(R.id.elem4);
        elem4.setText(String.format(Locale.ENGLISH, "%6.2f", alphaBuffer[3]));
    }

    private void updateEeg() {
        TextView tp9 = rootView.findViewById(R.id.eeg_tp9);
        TextView fp1 = rootView.findViewById(R.id.eeg_af7);
        TextView fp2 = rootView.findViewById(R.id.eeg_af8);
        TextView tp10 = rootView.findViewById(R.id.eeg_tp10);
        tp9.setText(String.format(Locale.ENGLISH, "%6.2f", eegBuffer[0]));
        fp1.setText(String.format(Locale.ENGLISH, "%6.2f", eegBuffer[1]));
        fp2.setText(String.format(Locale.ENGLISH, "%6.2f", eegBuffer[2]));
        tp10.setText(String.format(Locale.ENGLISH, "%6.2f", eegBuffer[3]));
    }

    private void initUI() {
        Button refreshButton = rootView.findViewById(R.id.refresh);
        refreshButton.setOnClickListener(this);
        Button connectButton = rootView.findViewById(R.id.connect);
        connectButton.setOnClickListener(this);
        Button disconnectButton =rootView.findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(this);
        Button pauseButton = rootView.findViewById(R.id.pause);
        pauseButton.setOnClickListener(this);

        spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        Spinner musesSpinner = rootView.findViewById(R.id.muses_spinner);
        musesSpinner.setAdapter(spinnerAdapter);
    }
}
