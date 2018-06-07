package com.choosemuse.example.libmuse.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.choosemuse.example.libmuse.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

// Created by Lokmane Krizou on 6/6/2018.
public class homeScreen extends Fragment{
    private final String TAG = "homeScreen";
    SharedPreferences preferences;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        android.view.View rootView = inflater.inflate(R.layout.fragment_homescreen, container, false);
        Log.d("Start Fragment",TAG);

        preferences = getContext().getSharedPreferences("settings", MODE_PRIVATE);

        if (getSharedValue("action_type") == 0)
            showImageFeedback();

        return rootView;
    }

// TODO add Graph view (MpChartAndroid)
// TODO add Stress level and animate the circle view(change the color according to the stress level)
// TODO start the feedback whenever there is problem with the stress

    void showImageFeedback() {

        AlertDialog.Builder alert_add = new AlertDialog.Builder(getContext());
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View view = factory.inflate(R.layout.image_dialog, null);
        view.findViewById(R.id.image_feedback).setBackgroundResource(R.drawable.meme_1);
        alert_add.setView(view);
        alert_add.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int clicked_id) {}
        });

        alert_add.show();
    }

    public Integer getSharedValue(String key) {
            return preferences.getInt(key, 0);
    }

    Drawable getRandomDrawable() {
        Field[] drawablesFields = com.choosemuse.example.libmuse.R.drawable.class.getFields();
        ArrayList<Drawable> drawables = new ArrayList<>();

        for (Field field : drawablesFields) {
            try {
                Log.i("LOG_TAG", "com.choosemuse.example.libmuse.R.drawable." + field.getName());
                drawables.add(getResources().getDrawable(field.getInt(null)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return drawables.get(new Random().nextInt(drawables.size() + 1));
    }
}