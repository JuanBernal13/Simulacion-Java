// backend/src/main/java/com/example/icu_sim/model/bacteria/KlebsiellaPneumoniae.java
package com.example.icu_sim.model.bacteria;

import java.util.Random;

public class KlebsiellaPneumoniae {

    public enum State {
        SUSCEPTIBLE,
        COLONIZED,
        INFECTED
    }

    // Se amplían las sensibilidades
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
    private int quantity; // Número de microorganismos KNN en la celda

    private static final Random random = new Random();

    public KlebsiellaPneumoniae() {
        this.state = State.SUSCEPTIBLE;
        this.sensitivity = Sensitivity.SUSCEPTIBLE_TO_TREATMENT_A;
        this.quantity = 0;
    }

    // Getters y Setters
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

    // Métodos para incrementar y decrementar la cantidad
    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        this.quantity = Math.max(this.quantity - amount, 0);
    }

    public void resetQuantity() {
        this.quantity = 0;
    }

    /**
     * Simula la mutación de la bacteria cambiando su sensibilidad aleatoriamente
     * con una pequeña probabilidad.
     */
    public void tryMutate(double mutationRate) {
        if (random.nextDouble() < mutationRate) {
            // Seleccionamos otra sensibilidad de forma aleatoria
            Sensitivity[] values = Sensitivity.values();
            this.sensitivity = values[random.nextInt(values.length)];
        }
    }
}
