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

    private double hygieneFactor;
    private double ppeFactor;

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
            KlebsiellaPneumoniae cellKnn = getCurrentCell().getKnn();
            if(cellKnn.getState() == State.INFECTED && cellKnn.getQuantity() > 0) {
                // Base chance
                double baseChance = 0.15;
                double effectiveChance = baseChance * (1 - hygieneFactor) * (1 - ppeFactor);

                if(random.nextDouble() < effectiveChance) {
                    this.infected = true;
                    this.knn.setState(State.INFECTED);
                    logger.info("{} se ha infectado (worker).", getUniqueId());
                }
            }
        } else {
            // chance de recuperarse
            double recoveryChance = 0.03; // un poco baja para que se mantenga infectado
            if(random.nextDouble() < recoveryChance) {
                this.infected = false;
                this.knn.setState(State.SUSCEPTIBLE);
                logger.info("{} se ha recuperado (worker).", getUniqueId());
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
