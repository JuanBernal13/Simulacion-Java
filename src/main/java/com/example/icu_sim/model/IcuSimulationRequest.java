// backend/src/main/java/com/example/icu_sim/model/IcuSimulationRequest.java
package com.example.icu_sim.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IcuSimulationRequest {
    private int nPatients = 50;
    private int nWorkers = 5;
    private int maxSteps;
    private int gridWidth;
    private int gridHeight;

    // Ya existentes
    private double arrivalRate;
    private double mutationRate;

    // NUEVOS PARÁMETROS
    private double hygieneFactorMean;   // Media del factor de higiene (0..1)
    private double hygieneFactorStd;    // Desv. estándar del factor de higiene
    private double ppeFactor;           // Factor EPP (0..1)
    private double workerMovementProb;  // Probabilidad de movimiento de trabajadores
    private double patientMovementProb; // Probabilidad de movimiento de pacientes
    private long seed;                  // Semilla para la simulación (0 => no reproducible)

    private boolean saveLogs;


    public IcuSimulationRequest() {}

    // Getters & Setters
    public int getNPatients() {
        return nPatients;
    }

    public void setNPatients(int nPatients) {
        this.nPatients = nPatients;
    }

    public int getNWorkers() {
        return nWorkers;
    }

    public void setNWorkers(int nWorkers) {
        this.nWorkers = nWorkers;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    public double getArrivalRate() {
        return arrivalRate;
    }

    public void setArrivalRate(double arrivalRate) {
        this.arrivalRate = arrivalRate;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public double getHygieneFactorMean() {
        return hygieneFactorMean;
    }

    public void setHygieneFactorMean(double hygieneFactorMean) {
        this.hygieneFactorMean = hygieneFactorMean;
    }

    public double getHygieneFactorStd() {
        return hygieneFactorStd;
    }

    public void setHygieneFactorStd(double hygieneFactorStd) {
        this.hygieneFactorStd = hygieneFactorStd;
    }

    public double getPpeFactor() {
        return ppeFactor;
    }

    public void setPpeFactor(double ppeFactor) {
        this.ppeFactor = ppeFactor;
    }

    public double getWorkerMovementProb() {
        return workerMovementProb;
    }

    public void setWorkerMovementProb(double workerMovementProb) {
        this.workerMovementProb = workerMovementProb;
    }

    public double getPatientMovementProb() {
        return patientMovementProb;
    }

    public void setPatientMovementProb(double patientMovementProb) {
        this.patientMovementProb = patientMovementProb;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public boolean isSaveLogs() {
        return saveLogs;
    }

    public void setSaveLogs(boolean saveLogs) {
        this.saveLogs = saveLogs;
    }

    @Override
    public String toString() {
        return "IcuSimulationRequest{" +
                "nPatients=" + nPatients +
                ", nWorkers=" + nWorkers +
                ", maxSteps=" + maxSteps +
                ", gridWidth=" + gridWidth +
                ", gridHeight=" + gridHeight +
                ", arrivalRate=" + arrivalRate +
                ", mutationRate=" + mutationRate +
                ", hygieneFactorMean=" + hygieneFactorMean +
                ", hygieneFactorStd=" + hygieneFactorStd +
                ", ppeFactor=" + ppeFactor +
                ", workerMovementProb=" + workerMovementProb +
                ", patientMovementProb=" + patientMovementProb +
                ", seed=" + seed +
                '}';
    }
}
