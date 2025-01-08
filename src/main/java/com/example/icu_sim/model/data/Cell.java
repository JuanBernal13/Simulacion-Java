package com.example.icu_sim.model.data;

import com.example.icu_sim.model.agents.Agent;
import com.example.icu_sim.model.bacteria.KlebsiellaPneumoniae;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    private int x;
    private int y;
    private List<Agent> agents;
    private KlebsiellaPneumoniae knn;

    private boolean isIcuCell;
    private int icuCapacity;
    private int usedBeds;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.agents = new ArrayList<>();
        this.knn = new KlebsiellaPneumoniae();
        this.isIcuCell = false;
        this.icuCapacity = 0;
        this.usedBeds = 0;
    }

    public boolean isIcuCell() {
        return isIcuCell;
    }

    public void setIcuCell(boolean icuCell) {
        isIcuCell = icuCell;
    }

    public int getIcuCapacity() {
        return icuCapacity;
    }

    public void setIcuCapacity(int icuCapacity) {
        this.icuCapacity = icuCapacity;
    }

    public boolean hasFreeBed() {
        return usedBeds < icuCapacity;
    }

    public void occupyBed() {
        if (hasFreeBed()) {
            usedBeds++;
        }
    }

    public void freeBed() {
        if (usedBeds > 0) {
            usedBeds--;
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public List<Agent> getAgents() { return agents; }

    public void addAgent(Agent agent) {
        this.agents.add(agent);
    }

    public void removeAgent(Agent agent) {
        this.agents.remove(agent);
    }

    public KlebsiellaPneumoniae getKnn() {
        return knn;
    }

    public void setKnn(KlebsiellaPneumoniae knn) {
        this.knn = knn;
    }
}
