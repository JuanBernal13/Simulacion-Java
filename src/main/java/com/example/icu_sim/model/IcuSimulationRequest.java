package com.example.icu_sim.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IcuSimulationRequest {

    // Parámetros principales
    private int nPatients = 50;
    private int nWorkers = 10;
    private int maxSteps = 30;
    private int gridWidth = 10;
    private int gridHeight = 10;

    // Parámetros de probabilidades
    private double arrivalRate = 0.01;
    private double mutationRate = 0.01;

    private double hygieneFactorMean = 0.5;
    private double hygieneFactorStd = 0.1;
    private double ppeFactor = 0.3;
    private double workerMovementProb = 0.8;
    private double patientMovementProb = 0.5;

    // NUEVOS: controlar colonización e infección
    private double colonizationChance = 0.25;        // p(colonización)
    private double infectionFromColonizedChance = 0.15; // p(infectarse si colonizado)

    private long seed = 0;       // 0 => random distinto cada vez
    private boolean saveLogs = false; // Para guardar logs en archivo .txt

    public IcuSimulationRequest() {}

    // Getters & Setters
    public int getNPatients() { return nPatients; }
    public void setNPatients(int nPatients) { this.nPatients = nPatients; }

    public int getNWorkers() { return nWorkers; }
    public void setNWorkers(int nWorkers) { this.nWorkers = nWorkers; }

    public int getMaxSteps() { return maxSteps; }
    public void setMaxSteps(int maxSteps) { this.maxSteps = maxSteps; }

    public int getGridWidth() { return gridWidth; }
    public void setGridWidth(int gridWidth) { this.gridWidth = gridWidth; }

    public int getGridHeight() { return gridHeight; }
    public void setGridHeight(int gridHeight) { this.gridHeight = gridHeight; }

    public double getArrivalRate() { return arrivalRate; }
    public void setArrivalRate(double arrivalRate) { this.arrivalRate = arrivalRate; }

    public double getMutationRate() { return mutationRate; }
    public void setMutationRate(double mutationRate) { this.mutationRate = mutationRate; }

    public double getHygieneFactorMean() { return hygieneFactorMean; }
    public void setHygieneFactorMean(double hygieneFactorMean) { this.hygieneFactorMean = hygieneFactorMean; }

    public double getHygieneFactorStd() { return hygieneFactorStd; }
    public void setHygieneFactorStd(double hygieneFactorStd) { this.hygieneFactorStd = hygieneFactorStd; }

    public double getPpeFactor() { return ppeFactor; }
    public void setPpeFactor(double ppeFactor) { this.ppeFactor = ppeFactor; }

    public double getWorkerMovementProb() { return workerMovementProb; }
    public void setWorkerMovementProb(double workerMovementProb) { this.workerMovementProb = workerMovementProb; }

    public double getPatientMovementProb() { return patientMovementProb; }
    public void setPatientMovementProb(double patientMovementProb) { this.patientMovementProb = patientMovementProb; }

    public double getColonizationChance() { return colonizationChance; }
    public void setColonizationChance(double colonizationChance) { this.colonizationChance = colonizationChance; }

    public double getInfectionFromColonizedChance() { return infectionFromColonizedChance; }
    public void setInfectionFromColonizedChance(double infectionFromColonizedChance) { this.infectionFromColonizedChance = infectionFromColonizedChance; }

    public long getSeed() { return seed; }
    public void setSeed(long seed) { this.seed = seed; }

    public boolean isSaveLogs() { return saveLogs; }
    public void setSaveLogs(boolean saveLogs) { this.saveLogs = saveLogs; }

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
                ", colonizationChance=" + colonizationChance +
                ", infectionFromColonizedChance=" + infectionFromColonizedChance +
                ", seed=" + seed +
                ", saveLogs=" + saveLogs +
                '}';
    }
}
