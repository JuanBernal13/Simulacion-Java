// backend/src/main/java/com/example/icu_sim/model/Cell.java
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

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.agents = new ArrayList<>();
        this.knn = new KlebsiellaPneumoniae();
    }

    // Getters y Setters
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

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
