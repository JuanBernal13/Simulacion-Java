#!/usr/bin/env python
# -*- coding: utf-8 -*-

import requests
import matplotlib.pyplot as plt
import sys
import json
import argparse
import csv
from typing import Dict, Any


def run_simulation(
    base_url: str,
    n_patients: int,
    n_workers: int,
    max_steps: int,
    grid_width: int,
    grid_height: int,
    arrival_rate: float,
    mutation_rate: float,
    hygiene_factor_mean: float,
    hygiene_factor_std: float,
    ppe_factor: float,
    worker_movement_prob: float,
    patient_movement_prob: float,
    colonization_chance: float,
    infection_from_colonized: float,
    seed: int,
    save_logs: bool
) -> Dict[str, Any]:
    url = f"{base_url}/api/simulation/run"
    params = {
        "nPatients": n_patients,
        "nWorkers": n_workers,
        "maxSteps": max_steps,
        "gridWidth": grid_width,
        "gridHeight": grid_height,
        "arrivalRate": arrival_rate,
        "mutationRate": mutation_rate,
        "hygieneFactorMean": hygiene_factor_mean,
        "hygieneFactorStd": hygiene_factor_std,
        "ppeFactor": ppe_factor,
        "workerMovementProb": worker_movement_prob,
        "patientMovementProb": patient_movement_prob,
        "colonizationChance": colonization_chance,
        "infectionFromColonizedChance": infection_from_colonized,
        "seed": seed,
        "saveLogs": save_logs
    }

    print("Enviando solicitud con parámetros:\n", json.dumps(params, indent=2))

    try:
        resp = requests.post(url, json=params)
        resp.raise_for_status()
        return resp.json()
    except requests.RequestException as e:
        print("Error conectando con la API:", e)
        sys.exit(1)


def export_grid_states_to_csv(sim_result: Dict[str, Any], csv_path: str):
    grid_states = sim_result.get("gridState", [])
    if not grid_states:
        print("No hay gridState en la simulación.")
        return

    fieldnames = ["day", "x", "y", "knnState", "knnSensitivity", "knnQuantity", "agents"]

    with open(csv_path, mode='w', newline='', encoding='utf-8') as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()

        for day_index, day_data in enumerate(grid_states):
            cells = day_data.get("cells", [])
            for cell in cells:
                row = {
                    "day": day_index+1,
                    "x": cell["x"],
                    "y": cell["y"],
                    "knnState": cell["knnState"],
                    "knnSensitivity": cell["knnSensitivity"],
                    "knnQuantity": cell["knnQuantity"],
                    "agents": ";".join(cell.get("agents", []))
                }
                writer.writerow(row)
    print(f"Estados del grid exportados a {csv_path}")


def plot_results(sim_result: Dict[str, Any]):
    pct_patients_infected = sim_result.get("pctPatientsInfected", [])
    pct_workers_infected = sim_result.get("pctWorkersInfected", [])
    total_workers = sim_result.get("totalWorkers", 0)
    total_patients = sim_result.get("totalPatients", 0)

    if not isinstance(pct_patients_infected, list) or not isinstance(pct_workers_infected, list):
        print("Los datos de % infectados no son listas.")
        return

    steps = list(range(1, len(pct_patients_infected)+1))

    plt.figure(figsize=(12,6))
    plt.subplot(2,1,1)
    plt.plot(steps, pct_patients_infected, label="% Pacientes Infectados", color='red')
    plt.title("Evolución de % Pacientes Infectados")
    plt.xlabel("Paso")
    plt.ylabel("% Infectados")
    plt.grid(True)
    plt.legend()

    plt.subplot(2,1,2)
    plt.plot(steps, pct_workers_infected, label="% Trabajadores Infectados", color='orange')
    plt.title("Evolución de % Trabajadores Infectados")
    plt.xlabel("Paso")
    plt.ylabel("% Infectados")
    plt.grid(True)
    plt.legend()

    plt.tight_layout()
    plt.show()

    print("\n--- Resultados de la Simulación ---")
    print(f"Total Trabajadores: {total_workers}")
    print(f"Total Pacientes iniciales: {total_patients}")
    if pct_patients_infected:
        print(f"% Pacientes Infectados al final: {pct_patients_infected[-1]:.2f}%")
    if pct_workers_infected:
        print(f"% Trabajadores Infectados al final: {pct_workers_infected[-1]:.2f}%")


def main():
    parser = argparse.ArgumentParser(description="Cliente para Simulacion ICU con probabilidades ajustadas.")
    parser.add_argument("--url", default="http://localhost:8080",
                        help="URL base de la API. Default: http://localhost:8080")
    parser.add_argument("--nPatients", type=int, default=50)
    parser.add_argument("--nWorkers", type=int, default=10)
    parser.add_argument("--maxSteps", type=int, default=30)
    parser.add_argument("--gridWidth", type=int, default=10)
    parser.add_argument("--gridHeight", type=int, default=10)
    parser.add_argument("--arrivalRate", type=float, default=0.01)
    parser.add_argument("--mutationRate", type=float, default=0.01)
    parser.add_argument("--hygieneFactorMean", type=float, default=0.5)
    parser.add_argument("--hygieneFactorStd", type=float, default=0.1)
    parser.add_argument("--ppeFactor", type=float, default=0.3)
    parser.add_argument("--workerMovementProb", type=float, default=0.8)
    parser.add_argument("--patientMovementProb", type=float, default=0.5)
    parser.add_argument("--colonizationChance", type=float, default=0.25)
    parser.add_argument("--infectionFromColonizedChance", type=float, default=0.15)
    parser.add_argument("--seed", type=int, default=0)
    parser.add_argument("--saveLogs", action='store_true')
    parser.add_argument("--outputCsv", default="grid_states.csv", help="Archivo CSV para exportar el grid.")
    args = parser.parse_args()

    result = run_simulation(
        base_url=args.url,
        n_patients=args.nPatients,
        n_workers=args.nWorkers,
        max_steps=args.maxSteps,
        grid_width=args.gridWidth,
        grid_height=args.gridHeight,
        arrival_rate=args.arrivalRate,
        mutation_rate=args.mutationRate,
        hygiene_factor_mean=args.hygieneFactorMean,
        hygiene_factor_std=args.hygieneFactorStd,
        ppe_factor=args.ppeFactor,
        worker_movement_prob=args.workerMovementProb,
        patient_movement_prob=args.patientMovementProb,
        colonization_chance=args.colonizationChance,
        infection_from_colonized=args.infectionFromColonizedChance,
        seed=args.seed,
        save_logs=args.saveLogs
    )

    if result:
        export_grid_states_to_csv(result, args.outputCsv)
        plot_results(result)
    else:
        print("No se obtuvieron resultados de la simulación.")


if __name__ == "__main__":
    main()
    