// backend/src/main/java/com/example/icu_sim/controller/IcuSimulationController.java
package com.example.icu_sim.controller;

import com.example.icu_sim.model.IcuSimulationRequest;
import com.example.icu_sim.model.SimulationResult;
import com.example.icu_sim.service.IcuSimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// Importaciones de Spring Boot
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulation")
public class IcuSimulationController {

    private static final Logger logger = LoggerFactory.getLogger(IcuSimulationController.class);

    @Autowired
    private IcuSimulationService simulationService;

    // Endpoint para ejecutar la simulación
    @PostMapping("/run")
    public SimulationResult runSimulation(@RequestBody IcuSimulationRequest request) {
        logger.info("Received simulation request: {}", request.toString());
        return simulationService.runSimulation(request);
    }

    // Endpoint para verificar la salud del servicio
    @GetMapping("/health")
    public String getHealth() {
        return "ICU Simulation Service is running!";
    }
}
