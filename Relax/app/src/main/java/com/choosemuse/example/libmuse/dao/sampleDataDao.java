package com.choosemuse.example.libmuse.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import com.choosemuse.example.libmuse.MuseDataType;
import com.choosemuse.example.libmuse.entity.sampleData;

import java.util.List;

@Dao
public interface sampleDataDao {

    @Query("SELECT * FROM sampleData")
    List<sampleData> getAll();


    @Query("SELECT participant_data FROM sampleData")
    List<Double> getParticipantData();

    @Query("SELECT COUNT(*) from sampleData")
    int countTasks();

    @Insert
    void insert(sampleData sampleData);
    @Insert
    void insertAll(sampleData... sampleData);

    @Delete
    void delete(sampleData sampleData);
    @Update
    void update(sampleData sampleData);

}
