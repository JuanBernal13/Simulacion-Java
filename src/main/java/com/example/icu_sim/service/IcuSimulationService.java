package com.example.icu_sim.service;

import com.example.icu_sim.model.*;
import com.example.icu_sim.model.agents.HealthcareWorker;
import com.example.icu_sim.model.agents.Patient;
import com.example.icu_sim.model.agents.Agent;
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
    private StringBuilder logBuffer;

    // Listas de agentes y grid
    private List<HealthcareWorker> workers;
    private List<Patient> patients;
    private Grid grid;
    private SimulationResult result;

    public SimulationResult runSimulation(IcuSimulationRequest request) {
        // Semilla
        if(request.getSeed() != 0) {
            random = new Random(request.getSeed());
            logger.info("Usando semilla fija: {}", request.getSeed());
        } else {
            random = new Random();
            logger.info("Usando semilla aleatoria.");
        }

        // Logs
        if(request.isSaveLogs()) {
            logBuffer = new StringBuilder();
        }

        logger.info("Iniciando simulación: {}", request);

        // Crear Grid
        grid = new Grid(request.getGridWidth(), request.getGridHeight());

        // Crear Workers
        workers = new ArrayList<>();
        for(int i=0; i<request.getNWorkers(); i++){
            Cell c = getRandomCell(grid);
            double hVal = sampleNormal(request.getHygieneFactorMean(), request.getHygieneFactorStd());
            hVal = Math.max(0, Math.min(1, hVal));
            HealthcareWorker hw = new HealthcareWorker("HW-"+i, c, hVal, request.getPpeFactor(), request.getWorkerBaseInfectionChance());
            workers.add(hw);
        }

        // Crear Pacientes
        patients = new ArrayList<>();
        for(int i=0; i<request.getNPatients(); i++){
            Cell c = getRandomCell(grid);
            Patient p = new Patient("P-"+i, c, request.getColonizationChance(), request.getInfectionFromColonizedChance());
            patients.add(p);
        }

        // Infectar celdas
        initializeInfectedCells(grid, 8, 200); // Infecta 8 celdas con 200 de KNN

        // Objeto resultado
        result = new SimulationResult();
        result.setTotalWorkers(workers.size());
        result.setTotalPatients(patients.size());

        // Bucle de simulación
        for(int step=1; step<=request.getMaxSteps(); step++){
            logEvent("== Paso " + step + " ==", request.isSaveLogs());

            // 1. Llega algún paciente
            spawnNewPatients(grid, request);

            // 2. Actualizar KNN
            updateKnn(request);

            // 3. Mover y step() en Workers
            for(HealthcareWorker hw : workers){
                hw.step(step, this);
            }

            // 4. Mover y step() en Patients
            for(Patient p : new ArrayList<>(patients)){
                p.step(step, this);
                p.occupyIcuBedIfNeeded(this);
            }

            // 5. Asignar tratamiento
            applyTreatments(request);

            // 6. Métricas
            long infectedP = patients.stream().filter(Patient::isInfected).count();
            double pctP = patients.size()>0 ? (infectedP*100.0)/patients.size() : 0.0;
            result.getPctPatientsInfected().add(pctP);

            long infectedW = workers.stream().filter(HealthcareWorker::isInfected).count();
            double pctW = workers.size()>0 ? (infectedW*100.0)/workers.size() : 0.0;
            result.getPctWorkersInfected().add(pctW);

            // 7. Guardar grid y bacterias
            result.addGridState(grid);
            result.addBacteriaCounts(grid);

            logEvent(String.format("Paso %d => PacInfect=%.2f%%, WorkInfect=%.2f%%",
                    step, pctP, pctW), request.isSaveLogs());
        }

        // Guardar logs en .txt
        if(request.isSaveLogs()) {
            writeLogsToFile("simulation_logs.txt");
        }

        logger.info("Simulación completada.");
        return result;
    }

    private void applyTreatments(IcuSimulationRequest req) {
        // Ordenar pacientes según triage
        patients.sort((p1, p2) -> Double.compare(p2.getTriagePriority(), p1.getTriagePriority()));

        for(Patient patient : patients) {
            if(patient.isInfected()) {
                // Seleccionamos tratamiento según la sensibilidad
                applyOneTreatment(patient, patient.getKnn().getSensitivity(), req.isSaveLogs());
            }
        }
    }

    private void applyOneTreatment(Patient patient, Sensitivity s, boolean saveLogs) {
        double r = random.nextDouble();
        switch(s){
            case SUSCEPTIBLE_TO_TREATMENT_A:
                // 70% => partially cure
                if(r < 0.7) {
                    patient.partiallyCure();
                    logEvent("Tratamiento A para "+patient.getUniqueId(), saveLogs);
                }
                break;
            case RESISTANT_TO_TREATMENT_A:
                // 50% => partially cure
                if(r < 0.5) {
                    patient.partiallyCure();
                    logEvent("Tratamiento B para "+patient.getUniqueId(), saveLogs);
                }
                break;
            case SUSCEPTIBLE_TO_TREATMENT_B:
                // 60% => partially cure
                if(r < 0.6) {
                    patient.partiallyCure();
                    logEvent("Tratamiento C para "+patient.getUniqueId(), saveLogs);
                }
                break;
            case RESISTANT_TO_TREATMENT_B:
            case RESISTANT_TO_TREATMENT_C:
                // 40% => partially cure
                if(r < 0.4) {
                    patient.partiallyCure();
                    logEvent("Tratamiento agresivo para "+patient.getUniqueId(), saveLogs);
                }
                break;
            default:
                break;
        }
    }

    private void spawnNewPatients(Grid grid, IcuSimulationRequest req) {
        if(random.nextDouble() < req.getArrivalRate()) {
            Cell c = getRandomCell(grid);
            String id = "P-NEW-"+patients.size();
            Patient newP = new Patient(id, c, req.getColonizationChance(), req.getInfectionFromColonizedChance());
            patients.add(newP);
            logEvent("Llega nuevo paciente: "+id, req.isSaveLogs());
        }
    }

    private void updateKnn(IcuSimulationRequest req) {
        for(int x=0; x<grid.getWidth(); x++){
            for(int y=0; y<grid.getHeight(); y++){
                Cell cell = grid.getCell(x,y);
                KlebsiellaPneumoniae knn = cell.getKnn();
                knn.tryMutate(req.getMutationRate());

                if(knn.getState() == State.INFECTED && knn.getQuantity() > 0){
                    // Reproducción dependiente de la tasa específica
                    if(random.nextDouble() < knn.getReproductionRate()) {
                        int growth = 5;
                        if(cell.isIcuCell()) growth = 3;
                        knn.increaseQuantity(growth);
                    }

                    // Muerte
                    if(random.nextDouble() < 0.03){ // 3%
                        knn.decreaseQuantity(5);
                        if(knn.getQuantity() <= 0){
                            knn.setState(State.SUSCEPTIBLE);
                        }
                    }

                    // Movimiento más dinámico
                    double movementProbability = calculateMovementProbability(knn, cell, grid);
                    if(random.nextDouble() < movementProbability && knn.getQuantity() >= 10){
                        moveBacteria(cell, grid, knn, x, y, req);
                    }
                }
            }
        }
    }

    private double calculateMovementProbability(KlebsiellaPneumoniae knn, Cell cell, Grid grid) {
        double baseMovement;
        switch(knn.getState()) {
            case INFECTED:
                baseMovement = 0.3; // Mayor probabilidad de movimiento
                break;
            case COLONIZED:
                baseMovement = 0.1;
                break;
            default:
                baseMovement = 0.05;
        }
        double densityFactor = knn.getQuantity() / 100.0;
        double virulenceFactor = knn.getVirulenceFactor();
        return baseMovement * (1 + densityFactor) * virulenceFactor;
    }

    private void moveBacteria(Cell currentCell, Grid grid, KlebsiellaPneumoniae knn, int x, int y, IcuSimulationRequest req) {
        List<Cell> potentialCells = new ArrayList<>();
        // Obtener celdas vecinas con menos bacterias
        for(int dx=-1; dx<=1; dx++) {
            for(int dy=-1; dy<=1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if(nx >=0 && nx < grid.getWidth() && ny >=0 && ny < grid.getHeight()) {
                    Cell neighbor = grid.getCell(nx, ny);
                    if(neighbor.getKnn().getQuantity() < currentCell.getKnn().getQuantity()) {
                        potentialCells.add(neighbor);
                    }
                }
            }
        }
        if(!potentialCells.isEmpty()) {
            Cell targetCell = potentialCells.get(random.nextInt(potentialCells.size()));
            int moveAmount = Math.min(10, knn.getQuantity());
            knn.decreaseQuantity(moveAmount);
            KlebsiellaPneumoniae tknn = targetCell.getKnn();
            if(tknn.getState() == State.SUSCEPTIBLE || tknn.getState() == State.INFECTED) {
                tknn.setState(State.INFECTED);
                tknn.increaseQuantity(moveAmount);
                logEvent(String.format("KNN se movió de (%d,%d) a (%d,%d) con cantidad %d",
                        x, y, targetCell.getX(), targetCell.getY(), moveAmount), req.isSaveLogs());
            }
            if(knn.getQuantity() <= 0){
                knn.setState(State.SUSCEPTIBLE);
            }
        }
    }

    private void initializeInfectedCells(Grid grid, int count, int quantity) {
        int attempts = 0;
        while(count > 0 && attempts < grid.getWidth() * grid.getHeight()) {
            int x = random.nextInt(grid.getWidth());
            int y = random.nextInt(grid.getHeight());
            Cell cell = grid.getCell(x,y);
            if(cell.getKnn().getState() != State.INFECTED){
                cell.getKnn().setState(State.INFECTED);
                cell.getKnn().setQuantity(quantity);
                logger.info("Celda ({},{}) infectada con {} KNN", x,y,quantity);
                count--;
            }
            attempts++;
        }
        if(count > 0){
            logger.warn("No se pudo infectar el número deseado de celdas.");
        }
    }

    private Cell getRandomCell(Grid grid){
        int x = random.nextInt(grid.getWidth());
        int y = random.nextInt(grid.getHeight());
        return grid.getCell(x,y);
    }

    private double sampleNormal(double mean, double std){
        double u = random.nextDouble();
        double v = random.nextDouble();
        double z = Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v);
        return mean + z*std;
    }

    // Métodos para mover y gestionar agentes
    public void moveAgent(Agent a) {
        int cx = a.getCurrentCell().getX();
        int cy = a.getCurrentCell().getY();

        int nx = cx + (random.nextInt(3) -1);
        int ny = cy + (random.nextInt(3) -1);

        nx = Math.max(0, Math.min(nx, grid.getWidth()-1));
        ny = Math.max(0, Math.min(ny, grid.getHeight()-1));

        Cell nextCell = grid.getCell(nx, ny);
        if(nextCell != null && nextCell != a.getCurrentCell()){
            a.setCurrentCell(nextCell);
            logEvent(a.getUniqueId()+" se movió a ("+nx+","+ny+")", false);
        }
    }

    public void removePatient(Patient p) {
        patients.remove(p);
        result.setTotalPatients(patients.size());
    }

    private void logEvent(String msg, boolean saveLogs){
        logger.debug(msg);
        if(saveLogs && logBuffer != null){
            logBuffer.append(msg).append("\n");
        }
    }

    private void writeLogsToFile(String filename){
        try(FileWriter fw = new FileWriter(filename);
            PrintWriter pw = new PrintWriter(fw)) {
            pw.print(logBuffer.toString());
            logger.info("Logs guardados en {}", filename);
        } catch(IOException e){
            logger.error("Error al escribir logs: {}", e.getMessage());
        }
    }
}
