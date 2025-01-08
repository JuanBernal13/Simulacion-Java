package com.example.icu_sim.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IcuSimulationRequest {

    // Parámetros principales
    private int nPatients = 50;
    private int nWorkers = 10;
    private int maxSteps = 26280; // 365 días * 24 pasos/día (8 horas * 3 pasos/hora)
    private int gridWidth = 10;
    private int gridHeight = 10;

    // Parámetros de probabilidades
    private double arrivalRate = 0.01; // Probabilidad de llegada de un nuevo paciente por paso
    private double mutationRate = 0.01;

    private double hygieneFactorMean = 0.6; // Aumentado de 0.5 a 0.6
    private double hygieneFactorStd = 0.1;
    private double ppeFactor = 0.5; // Aumentado de 0.3 a 0.5
    private double workerMovementProb = 0.1; // Probabilidad de movimiento por paso
    private double patientMovementProb = 0.05; // Probabilidad de movimiento por paso

    // NUEVOS: controlar colonización e infección
    private double colonizationChance = 0.25;        // p(colonización)
    private double infectionFromColonizedChance = 0.15; // p(infectarse si colonizado

    // Nuevos parámetros para gestión de tiempo
    private int stepsPerDay = 24; // 8 horas * 3 pasos por hora
    private int workingHoursPerDay = 8; // 8 horas diarias
    private int minutesPerStep = 20; // 20 minutos por paso

    // Nuevos parámetros para control de infección de trabajadores
    private double workerBaseInfectionChance = 0.02; // Probabilidad base de infección por paso

    // Nuevos parámetros para pacientes en UCI
    private double icuPatientInfectionFactor = 1.5; // Factor adicional de susceptibilidad en UCI

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

    public int getStepsPerDay() { return stepsPerDay; }
    public void setStepsPerDay(int stepsPerDay) { this.stepsPerDay = stepsPerDay; }

    public int getWorkingHoursPerDay() { return workingHoursPerDay; }
    public void setWorkingHoursPerDay(int workingHoursPerDay) { this.workingHoursPerDay = workingHoursPerDay; }

    public int getMinutesPerStep() { return minutesPerStep; }
    public void setMinutesPerStep(int minutesPerStep) { this.minutesPerStep = minutesPerStep; }

    public double getWorkerBaseInfectionChance() {
        return workerBaseInfectionChance;
    }

    public void setWorkerBaseInfectionChance(double workerBaseInfectionChance) {
        this.workerBaseInfectionChance = workerBaseInfectionChance;
    }

    public double getIcuPatientInfectionFactor() {
        return icuPatientInfectionFactor;
    }

    public void setIcuPatientInfectionFactor(double icuPatientInfectionFactor) {
        this.icuPatientInfectionFactor = icuPatientInfectionFactor;
    }

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
                ", stepsPerDay=" + stepsPerDay +
                ", workingHoursPerDay=" + workingHoursPerDay +
                ", minutesPerStep=" + minutesPerStep +
                ", workerBaseInfectionChance=" + workerBaseInfectionChance +
                ", icuPatientInfectionFactor=" + icuPatientInfectionFactor +
                ", seed=" + seed +
                ", saveLogs=" + saveLogs +
                '}';
    }
}
