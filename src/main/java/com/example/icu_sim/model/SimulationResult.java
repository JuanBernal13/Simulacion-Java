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
        List<Map<String, Object>> cellsData = new ArrayList<>();
        for(int x=0; x<grid.getWidth(); x++) {
            for(int y=0; y<grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                Map<String, Object> cellInfo = new HashMap<>();
                cellInfo.put("x", x);
                cellInfo.put("y", y);
                cellInfo.put("knnState", cell.getKnn().getState().toString());
                cellInfo.put("knnSensitivity", cell.getKnn().getSensitivity().toString());
                cellInfo.put("knnQuantity", cell.getKnn().getQuantity());

                List<String> agentIds = new ArrayList<>();
                for(Agent ag : cell.getAgents()) {
                    agentIds.add(ag.getUniqueId());
                }
                cellInfo.put("agents", agentIds);
                cellsData.add(cellInfo);
            }
        }
        state.put("cells", cellsData);
        this.gridState.add(state);
    }
}
