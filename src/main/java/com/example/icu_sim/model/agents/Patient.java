package com.example.icu_sim.model.agents;

import com.example.icu_sim.model.bacteria.KlebsiellaPneumoniae;
import com.example.icu_sim.model.bacteria.KlebsiellaPneumoniae.State;
import com.example.icu_sim.model.data.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class Patient extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(Patient.class);

    private boolean infected;
    private boolean colonized;
    private KlebsiellaPneumoniae knn;
    private double triagePriority;
    private boolean inIcu;
    private Random random;

    // Ajustaremos en tiempo de ejecución:
    private double colonizationChance;
    private double infectionFromColonizedChance;

    public Patient(String uniqueId, Cell initialCell) {
        super(uniqueId, initialCell);
        this.infected = false;
        this.colonized = false;
        this.knn = new KlebsiellaPneumoniae();
        this.triagePriority = 0.0;
        this.inIcu = false;
        this.random = new Random();

        // Valores por defecto. Luego, en el servicio, los cambiamos al asignar
        this.colonizationChance = 0.25;
        this.infectionFromColonizedChance = 0.15;
    }

    @Override
    public void step() {
        if (!infected && !colonized) {
            KlebsiellaPneumoniae cellKnn = getCurrentCell().getKnn();
            if(cellKnn.getState() == State.INFECTED && cellKnn.getQuantity() > 0) {
                if(random.nextDouble() < colonizationChance) {
                    this.colonized = true;
                    this.knn.setState(State.COLONIZED);
                    logger.info("{} se ha colonizado (patient).", getUniqueId());
                }
            }
        }

        if(colonized && !infected) {
            if(random.nextDouble() < infectionFromColonizedChance) {
                this.infected = true;
                this.knn.setState(State.INFECTED);
                logger.info("{} se ha infectado (patient).", getUniqueId());
            }
        }

        // Asignar prioridad
        if (infected) {
            triagePriority = 1.0;
        } else if (colonized) {
            triagePriority = 0.5;
        } else {
            triagePriority = 0.0;
        }
    }

    public void partiallyCure() {
        // 50% chance de quedar colonizado tras curar infección
        double remainColonizedChance = 0.5;
        if(random.nextDouble() < remainColonizedChance) {
            this.infected = false;
            this.colonized = true;
            this.knn.setState(State.COLONIZED);
            logger.info("{} se curó pero sigue colonizado.", getUniqueId());
        } else {
            this.infected = false;
            this.colonized = false;
            this.knn.setState(State.SUSCEPTIBLE);
            this.knn.resetQuantity();
            logger.info("{} se curó totalmente (patient).", getUniqueId());
        }
    }

    public boolean canBeDischarged() {
        // Probabilidad moderada de alta
        if(!infected && !colonized && random.nextDouble() < 0.05) {
            return true;
        }
        return false;
    }

    public void occupyIcuBedIfNeeded() {
        if(triagePriority >= 1.0 && !inIcu) {
            if(!getCurrentCell().isIcuCell()) {
                return;
            }
            if(getCurrentCell().hasFreeBed()) {
                getCurrentCell().occupyBed();
                inIcu = true;
                logger.info("{} entró a la UCI (celda {}, {})",
                        getUniqueId(), getCurrentCell().getX(), getCurrentCell().getY());
            }
        }
    }

    // Getters & Setters
    public double getColonizationChance() {
        return colonizationChance;
    }

    public void setColonizationChance(double colonizationChance) {
        this.colonizationChance = colonizationChance;
    }

    public double getInfectionFromColonizedChance() {
        return infectionFromColonizedChance;
    }

    public void setInfectionFromColonizedChance(double infectionFromColonizedChance) {
        this.infectionFromColonizedChance = infectionFromColonizedChance;
    }

    public boolean isInfected() {
        return infected;
    }

    public boolean isColonized() {
        return colonized;
    }

    public KlebsiellaPneumoniae getKnn() {
        return knn;
    }

    public double getTriagePriority() {
        return triagePriority;
    }

    public boolean isInIcu() {
        return inIcu;
    }
}
