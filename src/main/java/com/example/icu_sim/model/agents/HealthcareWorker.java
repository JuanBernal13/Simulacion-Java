package com.example.icu_sim.model.agents;

import com.example.icu_sim.service.IcuSimulationService;
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

    // Nuevo parámetro para la probabilidad base de infección
    private double workerBaseInfectionChance;

    public HealthcareWorker(String uniqueId, Cell initialCell, double hygieneFactor, double ppeFactor, double workerBaseInfectionChance) {
        super(uniqueId, initialCell);
        this.infected = false;
        this.knn = new KlebsiellaPneumoniae();
        this.random = new Random();
        this.hygieneFactor = hygieneFactor;
        this.ppeFactor = ppeFactor;
        this.workerBaseInfectionChance = workerBaseInfectionChance;
    }

    @Override
    public void step(int currentStep, IcuSimulationService service) {
        if (!infected) {
            KlebsiellaPneumoniae cellKnn = getCurrentCell().getKnn();
            if (cellKnn.getState() == State.INFECTED && cellKnn.getQuantity() > 0) {
                // Base chance ajustable desde la solicitud de simulación
                double effectiveChance = workerBaseInfectionChance * (1 - hygieneFactor) * (1 - ppeFactor);

                if (random.nextDouble() < effectiveChance) {
                    this.infected = true;
                    this.knn.setState(State.INFECTED);
                    logger.info("{} se ha infectado (worker).", getUniqueId());
                }
            }
        } else {
            // Chance de recuperarse
            double recoveryChance = 0.03; // Un poco baja para que se mantenga infectado
            if (random.nextDouble() < recoveryChance) {
                this.infected = false;
                this.knn.setState(State.SUSCEPTIBLE);
                logger.info("{} se ha recuperado (worker).", getUniqueId());
            }
        }

        // Movilidad
        if (random.nextDouble() < 0.1) { // 10% de probabilidad de movimiento por paso
            service.moveAgent(this);
        }
    }

    // Getters & Setters
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

    public double getWorkerBaseInfectionChance() {
        return workerBaseInfectionChance;
    }

    public void setWorkerBaseInfectionChance(double workerBaseInfectionChance) {
        this.workerBaseInfectionChance = workerBaseInfectionChance;
    }
}
