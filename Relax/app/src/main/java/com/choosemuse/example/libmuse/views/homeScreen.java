package com.choosemuse.example.libmuse.views;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.choosemuse.example.libmuse.R;

// Created by Lokmane Krizou on 6/6/2018.
public class homeScreen extends Fragment{
    private final String TAG = "homeScreen";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        android.view.View rootView = inflater.inflate(R.layout.fragment_homescreen, container, false);
        Log.d("Start Fragment",TAG);
        return rootView;
    }

}


