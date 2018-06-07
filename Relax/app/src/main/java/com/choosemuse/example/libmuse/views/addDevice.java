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
public class addDevice extends Fragment  {
    private final String TAG = "addDevice";
    private MuseManagerAndroid manager;
    private Muse muse;
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

        return rootView;
    }
//TODO add muse device connection here
    // TODO figure out how to share the muse manager to home activity


}
