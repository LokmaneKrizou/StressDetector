package com.choosemuse.example.libmuse.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "dataUi")
public class dataUi {

    @PrimaryKey(autoGenerate = true)
    private int uiDataID;

    @ColumnInfo(name = "alpha")
    private double alpha;

    @ColumnInfo(name = "beta")
    private double beta;

    @ColumnInfo(name = "gamma")
    private double gamma;

    @ColumnInfo(name = "theta")
    private double theta;



    public int getUiDataID() {
        return uiDataID;
    }
    public void setUiDataID(int uiDataID) {
        this.uiDataID = uiDataID;
    }

    public double getAlpha() {
        return alpha;
    }
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }
    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getGamma() {
        return gamma;
    }
    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getTheta() {
        return theta;
    }
    public void setTheta(double theta) {
        this.theta = theta;
    }


    @Override
    public String toString()
    {
        StringBuffer buffer= new StringBuffer();
        buffer.append(this.uiDataID);
        buffer.append(" ");
        buffer.append(this.alpha);
        buffer.append(" ");
        buffer.append(this.beta);
        buffer.append(" ");
        buffer.append(this.gamma);
        buffer.append(" ");
        buffer.append(this.theta);
        buffer.append(" ");
        return buffer.toString();
    }
}
