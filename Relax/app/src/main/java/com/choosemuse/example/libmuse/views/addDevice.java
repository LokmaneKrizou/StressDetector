package com.choosemuse.example.libmuse.views;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.choosemuse.example.libmuse.R;

// Created by Lokmane Krizou on 6/6/2018.
public class addDevice extends Fragment {
    private final String TAG = "addDevice";


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        android.view.View rootView = inflater.inflate(R.layout.fragment_add_dvice, container, false);
        Log.d("Start Fragment",TAG);

        return rootView;
    }

}
