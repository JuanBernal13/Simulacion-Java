// backend/src/main/java/com/example/icu_sim/model/agents/HealthcareWorker.java
package com.example.icu_sim.model.agents;

import com.example.icu_sim.model.bacteria.KlebsiellaPneumoniae;
import com.example.icu_sim.model.bacteria.KlebsiellaPneumoniae.State;
import com.example.icu_sim.model.data.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class HealthcareWorker extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(HealthcareWorker.class);

    private boolean infected;
    private KlebsiellaPneumoniae knn;
    private Random random;

    // Nuevos
    private double hygieneFactor; // (0..1)
    private double ppeFactor;     // (0..1)

    public HealthcareWorker(String uniqueId, Cell initialCell) {
        super(uniqueId, initialCell);
        this.infected = false;
        this.knn = new KlebsiellaPneumoniae();
        this.random = new Random();
        this.hygieneFactor = 0.5;
        this.ppeFactor = 0.0;
    }

    @Override
    public void step() {
        if (!infected) {
            KlebsiellaPneumoniae knnInCell = getCurrentCell().getKnn();
            if (knnInCell.getState() == State.INFECTED && knnInCell.getQuantity() > 0) {
                double baseColonizationChance = 0.15; // 15%

                // Ajustar prob de infección con higiene y EPP
                double effectiveChance = baseColonizationChance
                        * (1.0 - hygieneFactor)
                        * (1.0 - ppeFactor);

                if (random.nextDouble() < effectiveChance) {
                    this.infected = true;
                    this.knn.setState(State.INFECTED);
                    logger.info("{} se ha infectado con KNN.", getUniqueId());
                }
            }
        } else {
            // Lógica de recuperación
            double recoveryChance = 0.05;
            if (random.nextDouble() < recoveryChance) {
                this.infected = false;
                this.knn.setState(State.SUSCEPTIBLE);
                logger.info("{} se ha recuperado de KNN.", getUniqueId());
            }
        }
    }

    public boolean isInfected() {
        return infected;
    }

    public void setInfected(boolean infected) {
        this.infected = infected;
    }

    public KlebsiellaPneumoniae getKnn() {
        return knn;
    }

    public void setKnn(KlebsiellaPneumoniae knn) {
        this.knn = knn;
    }

    public double getHygieneFactor() {
        return hygieneFactor;
    }

    public void setHygieneFactor(double hygieneFactor) {
        this.hygieneFactor = hygieneFactor;
    }

    public double getPpeFactor() {
        return ppeFactor;
    }

    public void setPpeFactor(double ppeFactor) {
        this.ppeFactor = ppeFactor;
    }
}
