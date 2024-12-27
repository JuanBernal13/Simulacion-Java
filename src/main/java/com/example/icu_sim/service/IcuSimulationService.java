// backend/src/main/java/com/example/icu_sim/service/IcuSimulationService.java
package com.example.icu_sim.service;

import com.example.icu_sim.model.IcuSimulationRequest;
import com.example.icu_sim.model.SimulationResult;
import com.example.icu_sim.model.agents.Agent;
import com.example.icu_sim.model.agents.HealthcareWorker;
import com.example.icu_sim.model.agents.Patient;
import com.example.icu_sim.model.bacteria.KlebsiellaPneumoniae;
import com.example.icu_sim.model.bacteria.KlebsiellaPneumoniae.Sensitivity;
import com.example.icu_sim.model.bacteria.KlebsiellaPneumoniae.State;
import com.example.icu_sim.model.data.Cell;
import com.example.icu_sim.model.data.Grid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Service
public class IcuSimulationService {

    private static final Logger logger = LoggerFactory.getLogger(IcuSimulationService.class);
    private Random random;
    private double arrivalRate;
    private double mutationRate;

    // Buffer para guardar logs si es requerido
    private StringBuilder simulationLogBuffer;

    public SimulationResult runSimulation(IcuSimulationRequest request) {
        // 1. Configurar semilla
        if (request.getSeed() != 0) {
            this.random = new Random(request.getSeed());
            logger.info("Usando semilla fija: {}", request.getSeed());
        } else {
            this.random = new Random();
            logger.info("Usando semilla aleatoria (no reproducible).");
        }

        this.arrivalRate = request.getArrivalRate();
        this.mutationRate = request.getMutationRate();

        // Si guardamos logs, inicializamos el buffer
        if (request.isSaveLogs()) {
            simulationLogBuffer = new StringBuilder();
        }

        logger.info("Iniciando simulación ICU con parámetros: {}", request);

        // 2. Crear grid
        Grid grid = new Grid(request.getGridWidth(), request.getGridHeight());

        // 3. Crear trabajadores
        List<HealthcareWorker> workers = new ArrayList<>();
        for(int i = 0; i < request.getNWorkers(); i++) {
            Cell initialCell = getRandomCell(grid);
            HealthcareWorker hw = new HealthcareWorker("HW-" + i, initialCell);
            // Ajustar hygieneFactor y ppeFactor
            double hygieneVal = sampleNormal(request.getHygieneFactorMean(), request.getHygieneFactorStd());
            hygieneVal = Math.min(Math.max(hygieneVal, 0.0), 1.0);
            hw.setHygieneFactor(hygieneVal);
            hw.setPpeFactor(request.getPpeFactor());
            workers.add(hw);
        }

        // 4. Crear pacientes
        List<Patient> patients = new ArrayList<>();
        for(int i = 0; i < request.getNPatients(); i++) {
            Cell initialCell = getRandomCell(grid);
            Patient p = new Patient("P-" + i, initialCell);
            patients.add(p);
        }

        // 5. Infectar algunas celdas
        initializeKnnInfected(grid, 5, 300);

        // 6. Preparar resultado
        SimulationResult result = new SimulationResult();
        result.setTotalWorkers(workers.size());
        result.setTotalPatients(patients.size());

        // 7. Bucle de simulación
        for(int step = 1; step <= request.getMaxSteps(); step++) {
            logEvent(String.format("== Paso %d ==", step), request.isSaveLogs());

            // (a) Llegada de nuevos pacientes
            spawnNewPatientsIfAny(grid, patients, step, request.isSaveLogs());

            // (b) Actualizar KNN
            updateKnn(grid, request.isSaveLogs());

            // (c) Actualizar Agentes
            //    - Trabajadores
            for(HealthcareWorker hw : workers) {
                hw.step();
                if(random.nextDouble() < request.getWorkerMovementProb()) {
                    moveAgent(hw, grid, request.isSaveLogs());
                }
            }

            //    - Pacientes
            List<Patient> dischargedPatients = new ArrayList<>();
            for(Patient p : patients) {
                p.step();
                if(!p.isInIcu() && random.nextDouble() < request.getPatientMovementProb()) {
                    moveAgent(p, grid, request.isSaveLogs());
                }
                p.occupyIcuBedIfNeeded();
                if(p.canBeDischarged()) {
                    dischargedPatients.add(p);
                }
            }

            // (d) Dar de alta pacientes
            for(Patient dp : dischargedPatients) {
                if(dp.isInIcu()) {
                    dp.getCurrentCell().freeBed();
                }
                dp.getCurrentCell().removeAgent(dp);
                patients.remove(dp);
                logEvent("Paciente " + dp.getUniqueId() + " ha sido dado de alta.", request.isSaveLogs());
            }

            // (e) Tratamientos
            assignResources(workers, patients, grid, request.isSaveLogs());

            // (f) Métricas
            long infectedPatientsCount = patients.stream().filter(Patient::isInfected).count();
            double pctPatientsInfected = (patients.size() > 0)
                    ? (double) infectedPatientsCount / patients.size() * 100
                    : 0.0;
            result.getPctPatientsInfected().add(pctPatientsInfected);

            long infectedWorkersCount = workers.stream().filter(HealthcareWorker::isInfected).count();
            double pctWorkersInfected = (workers.size() > 0)
                    ? (double) infectedWorkersCount / workers.size() * 100
                    : 0.0;
            result.getPctWorkersInfected().add(pctWorkersInfected);

            // (g) Guardar estado grid
            result.addGridState(grid);

            logEvent(String.format("Al final del paso %d => %%Pacientes Infectados: %.2f%%, %%Trabajadores Infectados: %.2f%%",
                    step, pctPatientsInfected, pctWorkersInfected), request.isSaveLogs());
        }

        // 8. Al finalizar la simulación, si se activó saveLogs, generamos el archivo .txt
        if(request.isSaveLogs()) {
            writeLogsToFile("simulation_logs.txt");
        }

        logger.info("Simulación completada.");
        return result;
    }

    private void writeLogsToFile(String filename) {
        try (FileWriter fw = new FileWriter(filename);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.print(simulationLogBuffer.toString());
            logger.info("Logs de simulación guardados en {}", filename);
        } catch (IOException e) {
            logger.error("Error escribiendo archivo de logs: {}", e.getMessage());
        }
    }

    private void logEvent(String msg, boolean saveLogs) {
        logger.debug(msg);
        if(saveLogs && simulationLogBuffer != null) {
            simulationLogBuffer.append(msg).append("\n");
        }
    }

    private void spawnNewPatientsIfAny(Grid grid, List<Patient> patients, int step, boolean saveLogs) {
        if(random.nextDouble() < arrivalRate) {
            Cell cell = getRandomCell(grid);
            String newId = "P-NEW-" + step + "-" + patients.size();
            Patient newPatient = new Patient(newId, cell);
            patients.add(newPatient);
            logEvent("Ha llegado un nuevo paciente: " + newId, saveLogs);
        }
    }

    private void moveAgent(Agent agent, Grid grid, boolean saveLogs) {
        int currentX = agent.getCurrentCell().getX();
        int currentY = agent.getCurrentCell().getY();

        int newX = currentX + (random.nextInt(3) - 1);
        int newY = currentY + (random.nextInt(3) - 1);

        newX = Math.max(0, Math.min(newX, grid.getWidth() - 1));
        newY = Math.max(0, Math.min(newY, grid.getHeight() - 1));

        Cell newCell = grid.getCell(newX, newY);
        if(newCell != null && newCell != agent.getCurrentCell()) {
            agent.setCurrentCell(newCell);
            logEvent(agent.getUniqueId() + " se movió a (" + newX + ", " + newY + ")", saveLogs);
        }
    }

    private void assignResources(List<HealthcareWorker> workers, List<Patient> patients, Grid grid, boolean saveLogs) {
        // Ordenar por prioridad
        patients.sort((p1, p2) -> Double.compare(p2.getTriagePriority(), p1.getTriagePriority()));

        for(Patient patient : patients) {
            if(patient.getTriagePriority() > 0.0 && patient.isInfected()) {
                applyTreatment(patient, patient.getKnn().getSensitivity(), saveLogs);
            }
        }
    }

    // Usamos la "semi-curación": pasa a partiallyCure() en lugar de 100% curar
    private void applyTreatment(Patient patient, Sensitivity sensitivity, boolean saveLogs) {
        double rnd = random.nextDouble();
        switch(sensitivity) {
            case SUSCEPTIBLE_TO_TREATMENT_A:
                // 80% de exito => partially cure
                if(rnd < 0.8) {
                    patient.partiallyCure();
                    logEvent("Tratamiento A exitoso para " + patient.getUniqueId(), saveLogs);
                }
                break;
            case RESISTANT_TO_TREATMENT_A:
                // 60% => partially cure
                if(rnd < 0.6) {
                    patient.partiallyCure();
                    logEvent("Tratamiento B exitoso para " + patient.getUniqueId(), saveLogs);
                }
                break;
            case SUSCEPTIBLE_TO_TREATMENT_C:
                // 70% => partially cure
                if(rnd < 0.7) {
                    patient.partiallyCure();
                    logEvent("Tratamiento C exitoso para " + patient.getUniqueId(), saveLogs);
                }
                break;
            case RESISTANT_TO_TREATMENT_B:
            case RESISTANT_TO_TREATMENT_C:
                // 40% => partially cure
                if(rnd < 0.4) {
                    patient.partiallyCure();
                    logEvent("Tratamiento agresivo exitoso para " + patient.getUniqueId(), saveLogs);
                }
                break;
            default:
                break;
        }
    }

    private void updateKnn(Grid grid, boolean saveLogs) {
        int width = grid.getWidth();
        int height = grid.getHeight();

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                Cell cell = grid.getCell(x, y);
                KlebsiellaPneumoniae knn = cell.getKnn();

                knn.tryMutate(mutationRate);

                if(knn.getState() == State.INFECTED && knn.getQuantity() > 0) {
                    double reproductionRate = 0.1;
                    if(random.nextDouble() < reproductionRate) {
                        int growth = 4; // ajustado para evitar picos
                        if(cell.isIcuCell()) {
                            growth = 2;
                        }
                        knn.increaseQuantity(growth);
                    }

                    double deathRate = 0.03; // reducción leve
                    if(random.nextDouble() < deathRate && knn.getQuantity() > 0) {
                        knn.decreaseQuantity(4);
                        if(knn.getQuantity() <= 0) {
                            knn.setState(State.SUSCEPTIBLE);
                        }
                    }

                    double movementChance = 0.08; // algo menor
                    if(random.nextDouble() < movementChance && knn.getQuantity() >= 5) {
                        int moveAmount = 5; // menos abrupto
                        int targetX = x + (random.nextInt(3) - 1);
                        int targetY = y + (random.nextInt(3) - 1);

                        targetX = Math.max(0, Math.min(targetX, width - 1));
                        targetY = Math.max(0, Math.min(targetY, height - 1));

                        if(!(targetX == x && targetY == y)) {
                            knn.decreaseQuantity(moveAmount);
                            Cell targetCell = grid.getCell(targetX, targetY);
                            KlebsiellaPneumoniae targetKnn = targetCell.getKnn();
                            if(targetKnn.getState() == State.INFECTED
                                    || targetKnn.getState() == State.SUSCEPTIBLE) {
                                targetKnn.setState(State.INFECTED);
                                targetKnn.increaseQuantity(moveAmount);
                            }
                            if(knn.getQuantity() <= 0) {
                                knn.setState(State.SUSCEPTIBLE);
                            }
                        }
                    }
                }
            }
        }
    }

    private void initializeKnnInfected(Grid grid, int numberOfInfectedCells, int initialQuantity) {
        int width = grid.getWidth();
        int height = grid.getHeight();

        for(int i = 0; i < numberOfInfectedCells; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Cell cell = grid.getCell(x, y);
            if(cell.getKnn().getState() != State.INFECTED) {
                cell.getKnn().setState(State.INFECTED);
                cell.getKnn().setQuantity(initialQuantity);
                logger.info("Celda ({}, {}) inicializada con KNN INFECTADO, cantidad {}", x, y, initialQuantity);
            } else {
                i--;
            }
        }
    }

    private Cell getRandomCell(Grid grid) {
        int x = random.nextInt(grid.getWidth());
        int y = random.nextInt(grid.getHeight());
        return grid.getCell(x, y);
    }

    private double sampleNormal(double mean, double stdDev) {
        double u = random.nextDouble();
        double v = random.nextDouble();
        double z = Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v);
        return mean + z * stdDev;
    }
}
