package com.choosemuse.example.libmuse.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.choosemuse.example.libmuse.entity.dataUi;

import java.util.List;

@Dao
public interface dataUiDao {

    @Query("SELECT alpha FROM dataUi")
    double getAlpha();
    @Query("SELECT beta FROM dataUi")
    double getBeta();
    @Query("SELECT theta FROM dataUi")
    double getTetha();
    @Query("SELECT Gamma FROM dataUi")
    double getGamma();

    @Query("SELECT * FROM dataUi")
    List<dataUi> getAll();


    @Insert
    void insertAll(dataUi... dataUis);
    @Insert
    void insert(dataUi dataUi);

    @Delete
    void delete(dataUi dataUi);

    @Update
    void update(dataUi dataUi);
}
