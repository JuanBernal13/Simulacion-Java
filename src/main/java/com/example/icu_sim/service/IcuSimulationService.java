// backend/src/main/java/com/example/icu_sim/service/IcuSimulationService.java
package com.example.icu_sim.service;

import com.example.icu_sim.model.*;
import com.example.icu_sim.model.agents.Agent;
import com.example.icu_sim.model.agents.HealthcareWorker;
import com.example.icu_sim.model.agents.Patient;
import com.example.icu_sim.model.bacteria.KlebsiellaPneumoniae;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.icu_sim.model.data.Cell;
import com.example.icu_sim.model.data.Grid;

import java.util.*;

@Service
public class IcuSimulationService {

    private static final Logger logger = LoggerFactory.getLogger(IcuSimulationService.class);
    private Random random = new Random();

    public SimulationResult runSimulation(IcuSimulationRequest request) {
        logger.info("Iniciando simulación ICU con los siguientes parámetros:");
        logger.info("Número de Trabajadores: {}", request.getNWorkers());
        logger.info("Número de Pacientes: {}", request.getNPatients());
        logger.info("Número de Pasos: {}", request.getMaxSteps());
        logger.info("Tamaño del Grid: {}x{}", request.getGridWidth(), request.getGridHeight());

        // Inicializar Grid
        Grid grid = new Grid(request.getGridWidth(), request.getGridHeight());

        // Inicializar agentes
        List<HealthcareWorker> workers = new ArrayList<>();
        List<Patient> patients = new ArrayList<>();

        for(int i = 0; i < request.getNWorkers(); i++) {
            Cell initialCell = getRandomCell(grid);
            HealthcareWorker hw = new HealthcareWorker("HW-" + i, initialCell);
            workers.add(hw);
            logger.debug("Agregado Trabajador de Salud: {}", hw.getUniqueId());
        }

        for(int i = 0; i < request.getNPatients(); i++) {
            Cell initialCell = getRandomCell(grid);
            Patient p = new Patient("P-" + i, initialCell);
            patients.add(p);
            logger.debug("Agregado Paciente: {}", p.getUniqueId());
        }

        // Inicializar algunas celdas con KNN infectado y cantidad inicial
        initializeKnnInfected(grid, 5, 500); // Infecta 5 celdas con 500 KNN cada una

        // Inicializar el objeto de resultados
        SimulationResult result = new SimulationResult();
        result.setTotalWorkers(workers.size());
        result.setTotalPatients(patients.size());

        // Ejecutar pasos de simulación
        for(int step = 1; step <= request.getMaxSteps(); step++) {
            logger.debug("=== Paso de Simulación: {} ===", step);

            // Actualizar KNN en cada celda (reproducción, muerte y movimiento)
            updateKnn(grid);

            // Actualizar agentes
            for(HealthcareWorker hw : workers) {
                hw.step();
                moveAgent(hw, grid); // Movimiento opcional
            }

            for(Patient p : patients) {
                p.step();
                moveAgent(p, grid); // Movimiento opcional
            }

            // Implementar lógica de Triage y tratamiento
            assignResources(workers, patients, grid);

            // Recolectar datos del paso actual
            long infectedPatients = patients.stream().filter(Patient::isInfected).count();
            double pctPatientsInfected = (request.getNPatients() > 0) ?
                    ((double) infectedPatients / request.getNPatients()) * 100 : 0.0;
            result.getPctPatientsInfected().add(pctPatientsInfected);
            logger.debug("Paso {}: {}% de Pacientes Infectados", step, pctPatientsInfected);

            long infectedWorkers = workers.stream().filter(HealthcareWorker::isInfected).count();
            double pctWorkersInfected = (request.getNWorkers() > 0) ?
                    ((double) infectedWorkers / request.getNWorkers()) * 100 : 0.0;
            result.getPctWorkersInfected().add(pctWorkersInfected);
            logger.debug("Paso {}: {}% de Trabajadores Infectados", step, pctWorkersInfected);

            // Guardar el estado del grid
            result.addGridState(grid);
        }

        // Registrar resultados finales
        logger.info("Simulación completada.");
        logger.info(String.format("Total Pacientes Infectados: %d (%.2f%%)",
                patients.stream().filter(Patient::isInfected).count(),
                (double) patients.stream().filter(Patient::isInfected).count() / request.getNPatients() * 100));
        logger.info(String.format("Total Trabajadores Infectados: %d (%.2f%%)",
                workers.stream().filter(HealthcareWorker::isInfected).count(),
                (double) workers.stream().filter(HealthcareWorker::isInfected).count() / request.getNWorkers() * 100));

        return result;
    }

    private Cell getRandomCell(Grid grid) {
        int x = random.nextInt(grid.getWidth());
        int y = random.nextInt(grid.getHeight());
        return grid.getCell(x, y);
    }

    private void moveAgent(Agent agent, Grid grid) {
        // Implementar lógica de movimiento aquí
        // Por ejemplo, moverse a una celda adyacente aleatoria
        int currentX = agent.getCurrentCell().getX();
        int currentY = agent.getCurrentCell().getY();

        int newX = currentX + random.nextInt(3) - 1; // -1, 0, 1
        int newY = currentY + random.nextInt(3) - 1; // -1, 0, 1

        // Asegurar que las nuevas coordenadas están dentro del grid
        newX = Math.max(0, Math.min(newX, grid.getWidth() - 1));
        newY = Math.max(0, Math.min(newY, grid.getHeight() - 1));

        Cell newCell = grid.getCell(newX, newY);
        if(newCell != null && newCell != agent.getCurrentCell()) {
            agent.setCurrentCell(newCell);
            logger.debug("{} se ha movido a la celda ({}, {}).", agent.getUniqueId(), newX, newY);
        }
    }

    private void assignResources(List<HealthcareWorker> workers, List<Patient> patients, Grid grid) {
        // Ordenar pacientes por prioridad de triage (descendente)
        patients.sort((p1, p2) -> Double.compare(p2.getTriagePriority(), p1.getTriagePriority()));

        for(Patient patient : patients) {
            if(patient.getTriagePriority() > 0.0) {
                // Asignar recursos, por ejemplo, tratamiento antibiótico basado en sensibilidad
                KlebsiellaPneumoniae.Sensitivity sensitivity = patient.getKnn().getSensitivity();
                applyTreatment(patient, sensitivity);
            }
        }
    }

    private void applyTreatment(Patient patient, KlebsiellaPneumoniae.Sensitivity sensitivity) {
        // Implementar lógica de tratamiento aquí
        // Por ejemplo, cambiar el estado de KNN basado en la sensibilidad
        switch(sensitivity) {
            case SUSCEPTIBLE_TO_TREATMENT_A:
                // Simular tratamiento A exitoso
                if(random.nextDouble() < 0.8) { // 80% de éxito
                    patient.setColonized(false);
                    patient.setTriagePriority(0.0);
                    patient.getKnn().setState(KlebsiellaPneumoniae.State.SUSCEPTIBLE);
                    patient.getKnn().resetQuantity(); // Resetear cantidad tras tratamiento
                    logger.info("Tratamiento A exitoso para {}", patient.getUniqueId());
                }
                break;
            case RESISTANT_TO_TREATMENT_A:
                // Simular tratamiento B
                if(random.nextDouble() < 0.6) { // 60% de éxito
                    patient.setInfected(false);
                    patient.setTriagePriority(0.0);
                    patient.getKnn().setState(KlebsiellaPneumoniae.State.SUSCEPTIBLE);
                    patient.getKnn().resetQuantity(); // Resetear cantidad tras tratamiento
                    logger.info("Tratamiento B exitoso para {}", patient.getUniqueId());
                }
                break;
            // Agrega más casos según sea necesario
            default:
                break;
        }
    }

    private void initializeKnnInfected(Grid grid, int numberOfInfectedCells, int initialQuantity) {
        // Seleccionar aleatoriamente algunas celdas para iniciar con KNN infectado
        int width = grid.getWidth();
        int height = grid.getHeight();

        for(int i = 0; i < numberOfInfectedCells; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Cell cell = grid.getCell(x, y);
            if(cell.getKnn().getState() != KlebsiellaPneumoniae.State.INFECTED) {
                cell.getKnn().setState(KlebsiellaPneumoniae.State.INFECTED);
                cell.getKnn().setQuantity(initialQuantity);
                logger.info("Celda ({}, {}) inicializada con KNN INFECTADO y cantidad {}", x, y, initialQuantity);
            } else {
                // Si ya está infectado, intentar otra celda
                i--;
            }
        }
    }

    private void updateKnn(Grid grid) {
        // Crear una copia temporal para almacenar los cambios de KNN después del movimiento
        int width = grid.getWidth();
        int height = grid.getHeight();

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                Cell cell = grid.getCell(x, y);
                KlebsiellaPneumoniae knn = cell.getKnn();
                if(knn.getState() == KlebsiellaPneumoniae.State.INFECTED && knn.getQuantity() > 0) {
                    // Reproducción
                    double reproductionRate = 0.1; // 10% de probabilidad de reproducirse cada paso
                    if(random.nextDouble() < reproductionRate) {
                        knn.increaseQuantity(10); // Aumentar cantidad de KNN en 10
                        logger.debug("KNN en celda ({}, {}) ha reproducido, nueva cantidad: {}", x, y, knn.getQuantity());
                    }

                    // Muerte
                    double deathRate = 0.05; // 5% de probabilidad de morir cada paso
                    if(random.nextDouble() < deathRate && knn.getQuantity() > 0) {
                        knn.decreaseQuantity(5); // Disminuir cantidad de KNN en 5
                        logger.debug("KNN en celda ({}, {}) ha muerto, nueva cantidad: {}", x, y, knn.getQuantity());
                        if(knn.getQuantity() <= 0) {
                            knn.setState(KlebsiellaPneumoniae.State.SUSCEPTIBLE);
                            logger.info("KNN en celda ({}, {}) ha desaparecido.", x, y);
                        }
                    }

                    // Movimiento
                    double movementChance = 0.2; // 20% de probabilidad de mover KNN
                    if(random.nextDouble() < movementChance && knn.getQuantity() >= 10) {
                        // Mover 10 KNN a una celda adyacente aleatoria
                        int moveAmount = 10;
                        int targetX = x + random.nextInt(3) - 1; // -1, 0, 1
                        int targetY = y + random.nextInt(3) - 1; // -1, 0, 1

                        // Asegurar que las coordenadas de destino están dentro del grid
                        targetX = Math.max(0, Math.min(targetX, width - 1));
                        targetY = Math.max(0, Math.min(targetY, height - 1));

                        if(targetX == x && targetY == y) {
                            // No mover a la misma celda
                            continue;
                        }

                        // Reducir KNN en la celda actual
                        knn.decreaseQuantity(moveAmount);

                        // Aumentar KNN en la celda de destino
                        Cell targetCell = grid.getCell(targetX, targetY);
                        KlebsiellaPneumoniae targetKnn = targetCell.getKnn();
                        if(targetKnn.getState() == KlebsiellaPneumoniae.State.INFECTED || targetKnn.getState() == KlebsiellaPneumoniae.State.SUSCEPTIBLE) {
                            targetKnn.setState(KlebsiellaPneumoniae.State.INFECTED);
                            targetKnn.increaseQuantity(moveAmount);
                            logger.debug("KNN se movió de ({}, {}) a ({}, {}) con cantidad {}", x, y, targetX, targetY, moveAmount);
                        }

                        // Si después del movimiento la cantidad es 0, cambiar el estado a SUSCEPTIBLE
                        if(knn.getQuantity() <= 0) {
                            knn.setState(KlebsiellaPneumoniae.State.SUSCEPTIBLE);
                            logger.info("KNN en celda ({}, {}) ha desaparecido.", x, y);
                        }
                    }
                }
            }
        }
    }
}
