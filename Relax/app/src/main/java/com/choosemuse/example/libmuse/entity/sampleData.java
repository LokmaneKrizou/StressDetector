package com.choosemuse.example.libmuse.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.choosemuse.example.libmuse.MuseDataType;

import java.util.ArrayList;

@Entity(tableName = "sampleData")
public class sampleData {

    @PrimaryKey(autoGenerate = true)
    private int participantId;

    @ColumnInfo(name = "participant_data")
    private double participantData;


    public int getParticipantId() {
        return participantId;
    }
    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public double getParticipantData() {
        return participantData;
    }
    public void setParticipantData(double participantData) {
        this.participantData = participantData;
    }


    @Override
    public String toString()
    {
        StringBuffer buffer= new StringBuffer();
        buffer.append(this.participantId);
        buffer.append(" ");
        buffer.append(this.participantData);
        buffer.append(" ");
        return buffer.toString();
    }
}
