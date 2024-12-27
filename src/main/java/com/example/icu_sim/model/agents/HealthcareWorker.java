// backend/src/main/java/com/example/icu_sim/model/agents/HealthcareWorker.java
package com.example.icu_sim.model.agents;

import com.example.icu_sim.model.data.Cell;
import com.example.icu_sim.model.bacteria.KlebsiellaPneumoniae;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class HealthcareWorker extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(HealthcareWorker.class);

    private boolean infected;
    private KlebsiellaPneumoniae knn; // Estado de KNN en el trabajador
    private Random random;

    public HealthcareWorker(String uniqueId, Cell initialCell) {
        super(uniqueId, initialCell);
        this.infected = false;
        this.knn = new KlebsiellaPneumoniae();
        this.random = new Random();
    }

    @Override
    public void step() {
        // Lógica de infección y recuperación
        if (!infected) {
            // Posibilidad de colonización basada en la presencia de KNN en la celda
            KlebsiellaPneumoniae knnInCell = getCurrentCell().getKnn();
            if (knnInCell.getState() == KlebsiellaPneumoniae.State.INFECTED && knnInCell.getQuantity() > 0) {
                double colonizationChance = 0.15; // 15%
                if (random.nextDouble() < colonizationChance) {
                    this.infected = true;
                    this.knn.setState(KlebsiellaPneumoniae.State.INFECTED);
                    logger.info("{} se ha infectado con KNN.", getUniqueId());
                }
            }
        } else {
            // Lógica de recuperación
            double recoveryChance = 0.05; // 5%
            if (random.nextDouble() < recoveryChance) {
                this.infected = false;
                this.knn.setState(KlebsiellaPneumoniae.State.SUSCEPTIBLE);
                logger.info("{} se ha recuperado de KNN.", getUniqueId());
            }
        }

        // Implementar otras acciones, como moverse a otra celda
    }

    // Getters y Setters
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
}
