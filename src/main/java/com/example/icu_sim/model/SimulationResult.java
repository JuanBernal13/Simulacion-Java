package com.example.icu_sim.model;

import com.example.icu_sim.model.data.Cell;
import com.example.icu_sim.model.data.Grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationResult {
    private int totalWorkers;
    private int totalPatients;
    private List<Double> pctPatientsInfected;
    private List<Double> pctWorkersInfected;
    private List<Map<String, Object>> gridState;
    private List<Map<String, Object>> bacteriaCounts; // Nueva lista para cantidades

    public SimulationResult() {
        this.pctPatientsInfected = new ArrayList<>();
        this.pctWorkersInfected = new ArrayList<>();
        this.gridState = new ArrayList<>();
        this.bacteriaCounts = new ArrayList<>();
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

    public List<Map<String, Object>> getBacteriaCounts() {
        return bacteriaCounts;
    }

    public void setBacteriaCounts(List<Map<String, Object>> bacteriaCounts) {
        this.bacteriaCounts = bacteriaCounts;
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
                cellInfo.put("isIcuCell", cell.isIcuCell());
                cellInfo.put("icuCapacity", cell.getIcuCapacity());

                List<String> agentIds = new ArrayList<>();
                for(com.example.icu_sim.model.agents.Agent ag : cell.getAgents()) {
                    agentIds.add(ag.getUniqueId());
                }
                cellInfo.put("agents", agentIds);
                cellsData.add(cellInfo);
            }
        }
        state.put("cells", cellsData);
        this.gridState.add(state);
    }

    public void addBacteriaCounts(Grid grid) {
        Map<String, Object> counts = new HashMap<>();
        List<Map<String, Object>> cellsData = new ArrayList<>();
        for(int x=0; x<grid.getWidth(); x++) {
            for(int y=0; y<grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                Map<String, Object> cellInfo = new HashMap<>();
                cellInfo.put("x", x);
                cellInfo.put("y", y);
                cellInfo.put("knnQuantity", cell.getKnn().getQuantity());
                cellInfo.put("knnState", cell.getKnn().getState().toString());
                cellsData.add(cellInfo);
            }
        }
        counts.put("bacteriaCounts", cellsData);
        this.bacteriaCounts.add(counts);
    }
}
