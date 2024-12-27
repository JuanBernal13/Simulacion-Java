// backend/src/main/java/com/example/icu_sim/model/SimulationResult.java
package com.example.icu_sim.model;

import com.example.icu_sim.model.agents.Agent;
import com.example.icu_sim.model.data.Cell;
import com.example.icu_sim.model.data.Grid;

import java.util.*;

public class SimulationResult {
    private int totalWorkers;
    private int totalPatients;
    private List<Double> pctPatientsInfected;
    private List<Double> pctWorkersInfected;
    private List<Map<String, Object>> gridState;

    public SimulationResult() {
        this.pctPatientsInfected = new ArrayList<>();
        this.pctWorkersInfected = new ArrayList<>();
        this.gridState = new ArrayList<>();
    }

    public int getTotalWorkers() {
        return totalWorkers;
    }

    public void setTotalWorkers(int totalWorkers) {
        this.totalWorkers = totalWorkers;
    }

    public int getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(int totalPatients) {
        this.totalPatients = totalPatients;
    }

    public List<Double> getPctPatientsInfected() {
        return pctPatientsInfected;
    }

    public void setPctPatientsInfected(List<Double> pctPatientsInfected) {
        this.pctPatientsInfected = pctPatientsInfected;
    }

    public List<Double> getPctWorkersInfected() {
        return pctWorkersInfected;
    }

    public void setPctWorkersInfected(List<Double> pctWorkersInfected) {
        this.pctWorkersInfected = pctWorkersInfected;
    }

    public List<Map<String, Object>> getGridState() {
        return gridState;
    }

    public void setGridState(List<Map<String, Object>> gridState) {
        this.gridState = gridState;
    }

    public void addGridState(Grid grid) {
        Map<String, Object> state = new HashMap<>();
        Cell[][] cells = grid.getCells();
        List<Map<String, Object>> cellStates = new ArrayList<>();

        for(int x = 0; x < grid.getWidth(); x++) {
            for(int y = 0; y < grid.getHeight(); y++) {
                Cell cell = cells[x][y];
                Map<String, Object> cellState = new HashMap<>();
                cellState.put("x", x);
                cellState.put("y", y);
                cellState.put("knnState", cell.getKnn().getState().toString());
                cellState.put("knnSensitivity", cell.getKnn().getSensitivity().toString());
                cellState.put("knnQuantity", cell.getKnn().getQuantity());

                List<String> agentIds = new ArrayList<>();
                for(Agent agent : cell.getAgents()) {
                    agentIds.add(agent.getUniqueId());
                }
                cellState.put("agents", agentIds);

                cellStates.add(cellState);
            }
        }

        state.put("cells", cellStates);
        this.gridState.add(state);
    }
}
