// backend/src/main/java/com/example/icu_sim/model/IcuSimulationRequest.java
package com.example.icu_sim.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignora propiedades desconocidas en el JSON
public class IcuSimulationRequest {
    private int nPatients=300;
    private int nWorkers=50;
    private int maxSteps;
    private int gridWidth;
    private int gridHeight;

    // Constructor por defecto
    public IcuSimulationRequest() {}

    // Getters y Setters
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

    @Override
    public String toString() {
        return "IcuSimulationRequest{" +
                "nPatients=" + nPatients +
                ", nWorkers=" + nWorkers +
                ", maxSteps=" + maxSteps +
                ", gridWidth=" + gridWidth +
                ", gridHeight=" + gridHeight +
                '}';
    }
}
