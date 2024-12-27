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

    private static final Random random = new Random();

    public KlebsiellaPneumoniae() {
        this.state = State.SUSCEPTIBLE;
        this.sensitivity = Sensitivity.SUSCEPTIBLE_TO_TREATMENT_A;
        this.quantity = 0;
    }

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
        if(random.nextDouble() < mutationRate) {
            Sensitivity[] vals = Sensitivity.values();
            this.sensitivity = vals[random.nextInt(vals.length)];
        }
    }
}
