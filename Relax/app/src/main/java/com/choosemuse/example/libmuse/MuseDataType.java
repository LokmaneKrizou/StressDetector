package com.choosemuse.example.libmuse;

public class MuseDataType {
    public double[] eeg;
    public double[] alpha = {0, 0, 0, 0};
    public double[] beta = {0, 0, 0, 0};
    public double[] gamma = {0, 0, 0, 0};
    public double[] theta = {0, 0, 0, 0};
    public double[] delta = {0, 0, 0, 0};

    MuseDataType (double[] alpha, double[] beta, double[] gamma, double[] theta) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.theta = theta;
    }

    MuseDataType () {

    }

    MuseDataType (double[] eeg, double[] alpha, double[] beta, double[] gamma, double[] theta, double[] delta) {
        this.eeg = eeg;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.theta = theta;
        this.delta = delta;
    }
}
