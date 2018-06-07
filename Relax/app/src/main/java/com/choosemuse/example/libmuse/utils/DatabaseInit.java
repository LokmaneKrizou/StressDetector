package com.choosemuse.example.libmuse.utils;


import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;


import com.choosemuse.example.libmuse.database.RelaxDatabase;
import com.choosemuse.example.libmuse.entity.dataUi;
import com.choosemuse.example.libmuse.entity.sampleData;

import java.util.List;

public class DatabaseInit {

    private static final String TAG = "DatabaseInit";

    public static void populateAsync(@NonNull final RelaxDatabase db) {
        PopulateDbAsync task = new PopulateDbAsync(db);
        task.execute();
    }

    public static void populateSync(@NonNull final RelaxDatabase db) {
        populateWithSampleData(db);
      }

    public static dataUi addDataUi(final RelaxDatabase db, dataUi dataUi) {
        db.dataUi().insertAll(dataUi);
        return dataUi;
    }

    public static void populateWithSampleData(RelaxDatabase db) {

        List<dataUi> dataUiList = db.dataUi().getAll();

        for(int i = 0; i< dataUiList.size(); i++)
        {
            Log.d(TAG, String.valueOf(dataUiList.get(i)));
        }

        Log.d(DatabaseInit.TAG, "Rows Count: " + dataUiList.size());
    }

    public static void printSampleData(RelaxDatabase db) {

        List<dataUi> dataUiList = db.dataUi().getAll();

        for(int i = 0; i< dataUiList.size(); i++)
        {
            Log.d(TAG, String.valueOf(dataUiList.get(i)));
        }

        Log.d(DatabaseInit.TAG, "Rows Count: " + dataUiList.size());
    }

    public static void printTasks(RelaxDatabase db) {

        List<sampleData> appList = db.sampleData().getAll();

        for(int i=0;i<appList.size();i++)
        {
            Log.d(TAG, appList.get(i).toString());
        }

        Log.d(DatabaseInit.TAG, "Rows Count: " + appList.size());
    }

    private static sampleData addTask(final RelaxDatabase db, sampleData sampleData) {
        db.sampleData().insertAll(sampleData);
        return sampleData;
    }


    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final RelaxDatabase mDb;

        PopulateDbAsync(RelaxDatabase db) {
            mDb = db;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            populateWithSampleData(mDb);
           return null;
        }

    }

}
