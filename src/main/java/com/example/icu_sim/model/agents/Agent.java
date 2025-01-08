package com.example.icu_sim.model.agents;

import com.example.icu_sim.service.IcuSimulationService;
import com.example.icu_sim.model.data.Cell;

public abstract class Agent {
    private String uniqueId;
    private Cell currentCell;

    public Agent(String uniqueId, Cell initialCell) {
        this.uniqueId = uniqueId;
        this.currentCell = initialCell;
        if (this.currentCell != null) {
            this.currentCell.addAgent(this);
        }
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public Cell getCurrentCell() {
        return currentCell;
    }

    public void setCurrentCell(Cell newCell) {
        if (this.currentCell != null) {
            this.currentCell.removeAgent(this);
        }
        this.currentCell = newCell;
        if (this.currentCell != null) {
            this.currentCell.addAgent(this);
        }
    }

    public abstract void step(int currentStep, IcuSimulationService service);
}
