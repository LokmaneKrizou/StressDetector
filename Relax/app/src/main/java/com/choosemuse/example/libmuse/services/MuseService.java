package com.choosemuse.example.libmuse.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Error;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseErrorListener;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;

import java.util.List;

// Created by Lokmane Krizou on 6/7/2018.
public class MuseService extends Service {

    public static String ACTION_MUSE_CONNECTED = "ACTION_MUSE_CONNECTED";
    public static String ACTION_MUSE_DISCONNECTED = "ACTION_MUSE_DISCONNECTED";
    public static String ACTION_MUSE_ALPHA = "ACTION_MUSE_ALPHA";
    public static String ACTION_MUSE_BETA = "ACTION_MUSE_BETA";
    public static String ACTION_MUSE_GAMMA = "ACTION_MUSE_GAMMA";
    public static String ACTION_MUSE_THETA = "ACTION_MUSE_THETA";
    public static String ACTION_MUSE_WAVES = "ACTION_MUSE_WAVES";
    public static String EXTRA_DATA = "EXTRA_DATA";

    public int waveCounter = 0;
    double[] newAverage = {0 ,0, 0, 0};

    private MuseManagerAndroid manager;
    private Muse muse;
    private MuseConnectionListener connectionListener = new ConnectionListener();
    private MuseDataListener dataListener = new DataListener();
    private MuseErrorListener errorListener = new ErrorListener();

    @Override
    public void onCreate() {
        super.onCreate();

        manager = MuseManagerAndroid.getInstance();
        manager.setContext(this);
        manager.setMuseListener(new MuseListener() {
            @Override
            public void museListChanged() {
                Toast.makeText(getBaseContext(), "Trying to connect!",
                        Toast.LENGTH_LONG).show();
                List<Muse> detectedMuses = manager.getMuses();
                for(Muse m : detectedMuses) {
                    if(m.getName().contains("8A5B") || m.getName().contains("8334")) {
                        muse = m;
                        break;
                    }
                }

                manager.stopListening();
                muse.registerDataListener(dataListener, MuseDataPacketType.ALPHA_RELATIVE);
                muse.registerDataListener(dataListener, MuseDataPacketType.BETA_RELATIVE);
                muse.registerDataListener(dataListener, MuseDataPacketType.GAMMA_RELATIVE);
                muse.registerDataListener(dataListener, MuseDataPacketType.THETA_RELATIVE);
                muse.registerConnectionListener(connectionListener);
                muse.registerErrorListener(errorListener);
                muse.runAsynchronously();
            }
        });

        manager.startListening();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ErrorListener extends MuseErrorListener {
        @Override
        public void receiveError(Error error) {
            //what to do in the UI when you get an error
        }
    }

    private class ConnectionListener extends MuseConnectionListener {
        @Override
        public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {
            //what to do with the connection packet
            if(p.getCurrentConnectionState() == ConnectionState.CONNECTED) {
                Log.d("service","CONNECTED");
                sendBroadcast(new Intent(MuseService.ACTION_MUSE_CONNECTED));
                Toast.makeText(getBaseContext(), "Connected to Muse",
                        Toast.LENGTH_LONG).show();
            }
            if(p.getCurrentConnectionState() == ConnectionState.DISCONNECTED) {
                Log.d("service","DISCONNECTED");
                Toast.makeText(getBaseContext(), "Disconnected from Muse",
                        Toast.LENGTH_LONG).show();
                sendBroadcast(new Intent(MuseService.ACTION_MUSE_DISCONNECTED));
            }
        }
    }

    private class DataListener extends MuseDataListener {
        @Override
        public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {

            double avg_alpha = 0, avg_beta = 0, avg_gamma = 0, avg_theta = 0;
            //received data package
            double[] buffer = new double[6];
            buffer[0] = p.getEegChannelValue(Eeg.EEG1);
            buffer[1] = p.getEegChannelValue(Eeg.EEG2);
            buffer[2] = p.getEegChannelValue(Eeg.EEG3);
            buffer[3] = p.getEegChannelValue(Eeg.EEG4);
            buffer[4] = p.getEegChannelValue(Eeg.AUX_LEFT);
            buffer[5] = p.getEegChannelValue(Eeg.AUX_RIGHT);

            if(p.packetType().equals(MuseDataPacketType.ALPHA_RELATIVE)) {
//                Intent newData = new Intent(MuseService.ACTION_MUSE_ALPHA);
//                newData.putExtra(MuseService.EXTRA_DATA, buffer);
//                Log.i("data waves", String.valueOf(buffer[0]) + " | " + String.valueOf(buffer[1]) +
//                        " | " + String.valueOf(buffer[2]) + " | " + String.valueOf(buffer[3]));
//                sendBroadcast(newData);
                avg_alpha = getAverage(buffer);
            }

            else if(p.packetType().equals(MuseDataPacketType.BETA_RELATIVE)) {
//                Intent newData = new Intent(MuseService.ACTION_MUSE_BETA);
//                newData.putExtra(MuseService.EXTRA_DATA, buffer);
//                Log.i("data beta", String.valueOf(buffer[0]) + " | " + String.valueOf(buffer[1]) +
//                        " | " + String.valueOf(buffer[2]) + " | " + String.valueOf(buffer[3]));
//                sendBroadcast(newData);
                avg_beta = getAverage(buffer);
            }

            else if(p.packetType().equals(MuseDataPacketType.THETA_RELATIVE)) {
//                Intent newData = new Intent(MuseService.ACTION_MUSE_THETA);
//                newData.putExtra(MuseService.EXTRA_DATA, buffer);
//                Log.i("data theta", String.valueOf(buffer[0]) + " | " + String.valueOf(buffer[1]) +
//                        " | " + String.valueOf(buffer[2]) + " | " + String.valueOf(buffer[3]));
//                sendBroadcast(newData);
                avg_theta = getAverage(buffer);
            }

            else if(p.packetType().equals(MuseDataPacketType.GAMMA_RELATIVE)) {
//                Intent newData = new Intent(MuseService.ACTION_MUSE_GAMMA);
//                newData.putExtra(MuseService.EXTRA_DATA, buffer);
//                Log.i("data gamma", String.valueOf(buffer[0]) + " | " + String.valueOf(buffer[1]) +
//                        " | " + String.valueOf(buffer[2]) + " | " + String.valueOf(buffer[3]));
//                sendBroadcast(newData);
                avg_gamma = getAverage(buffer);
            }

            if(avg_alpha != 0)
                newAverage[0] = avg_alpha;
            if(avg_beta != 0)
                newAverage[1] = avg_beta;
            if(avg_gamma != 0)
                newAverage[2] = avg_gamma;
            if(avg_theta != 0)
                newAverage[3] = avg_theta;

            if(isNotZeroOrNan(newAverage)) {
                Log.i("data average", newAverage[0] + " | " + newAverage[1] + " | " +
                        newAverage[2] + " | " + newAverage[3]);

                Intent newData = new Intent(MuseService.ACTION_MUSE_WAVES);
                newData.putExtra(MuseService.EXTRA_DATA, newAverage);
                sendBroadcast(newData);

                newAverage[0] = 0;
                newAverage[1] = 0;
                newAverage[2] = 0;
                newAverage[3] = 0;
            }
        }

        boolean isNotZeroOrNan(double[] array) {
            if(array[0] == 0 || array[1] == 0 || array[2] == 0 || array[3] == 0)
                return  false;
            if(Double.isNaN(array[0]) || Double.isNaN(array[1]) || Double.isNaN(array[2]) || Double.isNaN(array[3]))
                return  false;

            return true;
        }

        Double getAverage(double[] buffer) {
            double sum = 0;
            int count = 0;
            for(int i = 0; i < 4; i++)
                if (!Double.isNaN(buffer[i])) {
                    sum += buffer[i];
                    count = i;
                }

            return  sum / count;
        }

        @Override
        public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
            //received artifact package
        }
    }
}
