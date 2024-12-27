// backend/src/main/java/com/example/icu_sim/model/agents/Patient.java
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

    public Patient(String uniqueId, Cell initialCell) {
        super(uniqueId, initialCell);
        this.infected = false;
        this.colonized = false;
        this.knn = new KlebsiellaPneumoniae();
        this.triagePriority = 0.0;
        this.inIcu = false;
        this.random = new Random();
    }

    @Override
    public void step() {
        // Ajustamos las probabilidades para evitar picos tan bruscos
        double colonizationChance = 0.15; // antes 0.25
        double infectionChanceFromColonized = 0.10; // antes 0.20

        if (!infected && !colonized) {
            KlebsiellaPneumoniae knnInCell = getCurrentCell().getKnn();
            if (knnInCell.getState() == State.INFECTED && knnInCell.getQuantity() > 0) {
                if (random.nextDouble() < colonizationChance) {
                    this.colonized = true;
                    this.knn.setState(State.COLONIZED);
                    logger.info("{} se ha colonizado con KNN.", getUniqueId());
                }
            }
        }

        // De COLONIZED a INFECTED (bajamos la prob.)
        if (colonized && !infected) {
            if (random.nextDouble() < infectionChanceFromColonized) {
                this.infected = true;
                this.knn.setState(State.INFECTED);
                logger.info("{} se ha infectado con KNN.", getUniqueId());
            }
        }

        // Ajustar prioridad
        if (infected) {
            this.triagePriority = 1.0;
        } else if (colonized) {
            this.triagePriority = 0.5;
        } else {
            this.triagePriority = 0.0;
        }
    }

    // Pacientes se curan de "infected", pero podemos dejarlos en COLONIZED
    // para que no desaparezca toda la infección de golpe.
    public void partiallyCure() {
        // 70% chance de curarse completamente, 30% chance quedar colonizado
        double chanceRemainColonized = 0.3;
        if (random.nextDouble() < chanceRemainColonized) {
            this.infected = false;
            this.colonized = true;
            this.knn.setState(State.COLONIZED);
            logger.info("{} se curó de la infección pero sigue colonizado.", getUniqueId());
        } else {
            // Curación total
            this.infected = false;
            this.colonized = false;
            this.knn.setState(State.SUSCEPTIBLE);
            this.knn.resetQuantity();
            logger.info("{} se ha curado totalmente (sin colonización).", getUniqueId());
        }
    }

    // Para reducir la salida masiva de la simulación, bajamos la prob de discharge
    public boolean canBeDischarged() {
        // Ejemplo: 2% en vez de 5%
        if (!infected && !colonized && random.nextDouble() < 0.02) {
            return true;
        }
        return false;
    }

    // Métodos UCI
    public void occupyIcuBedIfNeeded() {
        if (this.triagePriority >= 1.0 && !this.inIcu) {
            if (!getCurrentCell().isIcuCell()) {
                return;
            }
            if (getCurrentCell().hasFreeBed()) {
                getCurrentCell().occupyBed();
                this.inIcu = true;
                logger.info("{} ha ingresado a la UCI (celda {}, {}).",
                        getUniqueId(), getCurrentCell().getX(), getCurrentCell().getY());
            }
        }
    }

    // Getters & Setters
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

    public boolean isInIcu() {
        return inIcu;
    }

    public void setInIcu(boolean inIcu) {
        this.inIcu = inIcu;
    }
}
