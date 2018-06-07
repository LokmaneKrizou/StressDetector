package com.choosemuse.example.libmuse.views;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.choosemuse.example.libmuse.R;

import static android.content.Context.MODE_PRIVATE;

// Created by Lokmane Krizou on 6/6/2018.
public class settings extends Fragment {
    private final String TAG = "settings";

    RadioGroup actions_list;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        android.view.View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        Log.d("Start Fragment", TAG);

        actions_list = rootView.findViewById(R.id.actions_list);
        final SharedPreferences shared_pref = getContext().getSharedPreferences(TAG, MODE_PRIVATE);
        final SharedPreferences.Editor editor = shared_pref.edit();

        actions_list.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                editor.putInt("action_type", actions_list.indexOfChild(group.findViewById(checkedId)));
                editor.apply();
                int read_option = shared_pref.getInt("action_type", 0);
                Log.d("TAG", read_option + "");
            }
        });

        return rootView;
    }
}
