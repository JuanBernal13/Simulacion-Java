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
        Grid grid = new Grid(request.getGridWidth(), request.getGridHeight());

        // Crear Workers
        List<HealthcareWorker> workers = new ArrayList<>();
        for(int i=0; i<request.getNWorkers(); i++){
            Cell c = getRandomCell(grid);
            HealthcareWorker hw = new HealthcareWorker("HW-"+i, c);

            double hVal = sampleNormal(request.getHygieneFactorMean(), request.getHygieneFactorStd());
            hVal = Math.max(0, Math.min(1, hVal));
            hw.setHygieneFactor(hVal);
            hw.setPpeFactor(request.getPpeFactor());

            workers.add(hw);
        }

        // Crear Pacientes
        List<Patient> patients = new ArrayList<>();
        for(int i=0; i<request.getNPatients(); i++){
            Cell c = getRandomCell(grid);
            Patient p = new Patient("P-"+i, c);
            // Ajustar prob. colonización/infección desde request
            p.setColonizationChance(request.getColonizationChance());
            p.setInfectionFromColonizedChance(request.getInfectionFromColonizedChance());
            patients.add(p);
        }

        // Infectar celdas
        initializeInfectedCells(grid, 8, 200); // Infecta 8 celdas con 200 de KNN

        // Objeto resultado
        SimulationResult result = new SimulationResult();
        result.setTotalWorkers(workers.size());
        result.setTotalPatients(patients.size());

        // Bucle
        for(int step=1; step<=request.getMaxSteps(); step++){
            logEvent("== Paso " + step + " ==", request.isSaveLogs());

            // 1. Llega algún paciente
            spawnNewPatients(grid, patients, request);

            // 2. Actualizar KNN
            updateKnn(grid, request);

            // 3. Mover y step() en Workers
            for(HealthcareWorker hw : workers){
                hw.step();
                if(random.nextDouble() < request.getWorkerMovementProb()) {
                    moveAgent(hw, grid, request.isSaveLogs());
                }
            }

            // 4. Mover y step() en Patients
            List<Patient> discharged = new ArrayList<>();
            for(Patient p : patients){
                p.step();
                if(!p.isInIcu() && random.nextDouble() < request.getPatientMovementProb()) {
                    moveAgent(p, grid, request.isSaveLogs());
                }
                p.occupyIcuBedIfNeeded();
                if(p.canBeDischarged()) {
                    discharged.add(p);
                }
            }
            // Dar de alta
            for(Patient dp : discharged){
                if(dp.isInIcu()) {
                    dp.getCurrentCell().freeBed();
                }
                dp.getCurrentCell().removeAgent(dp);
                patients.remove(dp);
                logEvent("Dado de alta: "+dp.getUniqueId(), request.isSaveLogs());
            }

            // 5. Asignar tratamiento
            applyTreatments(workers, patients, request);

            // 6. Métricas
            long infectedP = patients.stream().filter(Patient::isInfected).count();
            double pctP = patients.size()>0 ? (infectedP*100.0)/patients.size() : 0.0;
            result.getPctPatientsInfected().add(pctP);

            long infectedW = workers.stream().filter(HealthcareWorker::isInfected).count();
            double pctW = workers.size()>0 ? (infectedW*100.0)/workers.size() : 0.0;
            result.getPctWorkersInfected().add(pctW);

            // 7. Guardar grid
            result.addGridState(grid);

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

    private void applyTreatments(List<HealthcareWorker> workers, List<Patient> patients, IcuSimulationRequest req) {
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
                if(r<0.7) {
                    patient.partiallyCure();
                    logEvent("Tratamiento A para "+patient.getUniqueId(), saveLogs);
                }
                break;
            case RESISTANT_TO_TREATMENT_A:
                // 50% => partially cure
                if(r<0.5) {
                    patient.partiallyCure();
                    logEvent("Tratamiento B para "+patient.getUniqueId(), saveLogs);
                }
                break;
            case SUSCEPTIBLE_TO_TREATMENT_C:
                // 60% => partially cure
                if(r<0.6) {
                    patient.partiallyCure();
                    logEvent("Tratamiento C para "+patient.getUniqueId(), saveLogs);
                }
                break;
            case RESISTANT_TO_TREATMENT_B:
            case RESISTANT_TO_TREATMENT_C:
                // 40% => partially cure
                if(r<0.4) {
                    patient.partiallyCure();
                    logEvent("Tratamiento agresivo para "+patient.getUniqueId(), saveLogs);
                }
                break;
            default:
                break;
        }
    }

    private void spawnNewPatients(Grid grid, List<Patient> patients, IcuSimulationRequest req) {
        if(random.nextDouble() < req.getArrivalRate()) {
            Cell c = getRandomCell(grid);
            String id = "P-NEW-"+patients.size();
            Patient newP = new Patient(id, c);
            newP.setColonizationChance(req.getColonizationChance());
            newP.setInfectionFromColonizedChance(req.getInfectionFromColonizedChance());
            patients.add(newP);
            logEvent("Llega nuevo paciente: "+id, req.isSaveLogs());
        }
    }

    private void updateKnn(Grid grid, IcuSimulationRequest req) {
        for(int x=0; x<grid.getWidth(); x++){
            for(int y=0; y<grid.getHeight(); y++){
                Cell cell = grid.getCell(x,y);
                KlebsiellaPneumoniae knn = cell.getKnn();
                knn.tryMutate(req.getMutationRate());

                if(knn.getState()== State.INFECTED && knn.getQuantity()>0){
                    // Reproducción
                    if(random.nextDouble() < 0.2) { // 20% chance
                        int growth = 5;
                        if(cell.isIcuCell()) growth = 3;
                        knn.increaseQuantity(growth);
                    }

                    // Muerte
                    if(random.nextDouble() < 0.03){ // 3%
                        knn.decreaseQuantity(5);
                        if(knn.getQuantity()<=0){
                            knn.setState(State.SUSCEPTIBLE);
                        }
                    }

                    // Movimiento
                    if(random.nextDouble()<0.1 && knn.getQuantity()>=10){
                        int moveAmount = 10;
                        int nx = x + (random.nextInt(3)-1);
                        int ny = y + (random.nextInt(3)-1);

                        nx = Math.max(0, Math.min(nx, grid.getWidth()-1));
                        ny = Math.max(0, Math.min(ny, grid.getHeight()-1));

                        if(!(nx==x && ny==y)){
                            knn.decreaseQuantity(moveAmount);
                            Cell targetCell = grid.getCell(nx, ny);
                            KlebsiellaPneumoniae tknn = targetCell.getKnn();
                            if(tknn.getState()==State.SUSCEPTIBLE || tknn.getState()==State.INFECTED) {
                                tknn.setState(State.INFECTED);
                                tknn.increaseQuantity(moveAmount);
                            }
                            if(knn.getQuantity()<=0){
                                knn.setState(State.SUSCEPTIBLE);
                            }
                        }
                    }
                }
            }
        }
    }

    private void initializeInfectedCells(Grid grid, int count, int quantity) {
        for(int i=0; i<count; i++){
            int x = random.nextInt(grid.getWidth());
            int y = random.nextInt(grid.getHeight());
            Cell cell = grid.getCell(x,y);
            if(cell.getKnn().getState()!=State.INFECTED){
                cell.getKnn().setState(State.INFECTED);
                cell.getKnn().setQuantity(quantity);
                logger.info("Celda ({},{}) infectada con {} KNN", x,y,quantity);
            } else {
                i--;
            }
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

    private void moveAgent(Agent a, Grid grid, boolean saveLogs){
        int cx = a.getCurrentCell().getX();
        int cy = a.getCurrentCell().getY();

        int nx = cx + (random.nextInt(3) -1);
        int ny = cy + (random.nextInt(3) -1);

        nx = Math.max(0, Math.min(nx, grid.getWidth()-1));
        ny = Math.max(0, Math.min(ny, grid.getHeight()-1));

        Cell nextCell = grid.getCell(nx, ny);
        if(nextCell!= null && nextCell != a.getCurrentCell()){
            a.setCurrentCell(nextCell);
            logEvent(a.getUniqueId()+" se movió a ("+nx+","+ny+")", saveLogs);
        }
    }

    private void logEvent(String msg, boolean saveLogs){
        logger.debug(msg);
        if(saveLogs && logBuffer!=null){
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
