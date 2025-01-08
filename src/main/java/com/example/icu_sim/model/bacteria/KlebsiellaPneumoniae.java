package com.example.icu_sim.model.bacteria;

import java.util.Random;

public class KlebsiellaPneumoniae {

    public enum State {
        SUSCEPTIBLE,
        COLONIZED,
        INFECTED
    }

    public enum Sensitivity {
        SUSCEPTIBLE_TO_TREATMENT_A,
        RESISTANT_TO_TREATMENT_A,
        SUSCEPTIBLE_TO_TREATMENT_B,
        RESISTANT_TO_TREATMENT_B,
        SUSCEPTIBLE_TO_TREATMENT_C,
        RESISTANT_TO_TREATMENT_C
    }

    private State state;
    private Sensitivity sensitivity;
    private int quantity;

    // Nuevas variables
    private double virulenceFactor; // 0.0 a 1.0
    private double reproductionRate; // Tasa de reproducción por paso

    private static final Random random = new Random();

    public KlebsiellaPneumoniae() {
        this.state = State.SUSCEPTIBLE;
        this.sensitivity = Sensitivity.SUSCEPTIBLE_TO_TREATMENT_A;
        this.quantity = 0;
        this.virulenceFactor = 0.5; // Valor por defecto
        this.reproductionRate = 0.1; // Valor por defecto
    }

    // Getters y Setters para las nuevas variables
    public double getVirulenceFactor() {
        return virulenceFactor;
    }

    public void setVirulenceFactor(double virulenceFactor) {
        this.virulenceFactor = virulenceFactor;
    }

    public double getReproductionRate() {
        return reproductionRate;
    }

    public void setReproductionRate(double reproductionRate) {
        this.reproductionRate = reproductionRate;
    }

    // Resto de getters y setters
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Sensitivity getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(Sensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        this.quantity = Math.max(this.quantity - amount, 0);
    }

    public void resetQuantity() {
        this.quantity = 0;
    }

    public void tryMutate(double mutationRate) {
        if (random.nextDouble() < mutationRate) {
            Sensitivity[] vals = Sensitivity.values();
            this.sensitivity = vals[random.nextInt(vals.length)];
        }
    }
}
