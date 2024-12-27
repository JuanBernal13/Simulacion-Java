// backend/src/main/java/com/example/icu_sim/model/agents/Patient.java
package com.example.icu_sim.model.agents;

import com.example.icu_sim.model.data.Cell;
import com.example.icu_sim.model.bacteria.KlebsiellaPneumoniae;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class Patient extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(Patient.class);

    private boolean infected;
    private boolean colonized;
    private KlebsiellaPneumoniae knn; // Estado de KNN en el paciente
    private double triagePriority; // Para el modelo de triage

    private Random random;

    public Patient(String uniqueId, Cell initialCell) {
        super(uniqueId, initialCell);
        this.infected = false;
        this.colonized = false;
        this.knn = new KlebsiellaPneumoniae();
        this.triagePriority = 0.0;
        this.random = new Random();
    }

    @Override
    public void step() {
        // Lógica de infección y recuperación
        if (!infected && !colonized) {
            // Posibilidad de colonización basada en la presencia de KNN en la celda
            KlebsiellaPneumoniae knnInCell = getCurrentCell().getKnn();
            if (knnInCell.getState() == KlebsiellaPneumoniae.State.INFECTED && knnInCell.getQuantity() > 0) {
                double colonizationChance = 0.25; // 25%
                if (random.nextDouble() < colonizationChance) {
                    this.colonized = true;
                    this.knn.setState(KlebsiellaPneumoniae.State.COLONIZED);
                    logger.info("{} se ha colonizado con KNN.", getUniqueId());
                }
            }
        }

        if (colonized && !infected) {
            double infectionChance = 0.20; // 20%
            if (random.nextDouble() < infectionChance) {
                this.infected = true;
                this.knn.setState(KlebsiellaPneumoniae.State.INFECTED);
                logger.info("{} se ha infectado con KNN.", getUniqueId());
            }
        }

        // Asignar prioridad de triage
        if (infected) {
            this.triagePriority = 1.0; // Máxima prioridad
        } else if (colonized) {
            this.triagePriority = 0.5; // Prioridad media
        } else {
            this.triagePriority = 0.0; // Sin prioridad
        }

        // Implementar recuperación o tratamiento si es necesario
    }

    // Getters y Setters
    public boolean isInfected() {
        return infected;
    }

    public void setInfected(boolean infected) {
        this.infected = infected;
    }

    public boolean isColonized() {
        return colonized;
    }

    public void setColonized(boolean colonized) {
        this.colonized = colonized;
    }

    public KlebsiellaPneumoniae getKnn() {
        return knn;
    }

    public void setKnn(KlebsiellaPneumoniae knn) {
        this.knn = knn;
    }

    public double getTriagePriority() {
        return triagePriority;
    }

    public void setTriagePriority(double triagePriority) {
        this.triagePriority = triagePriority;
    }
}
