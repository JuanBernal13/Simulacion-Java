# frontend/icu_simulation.py
import requests
import matplotlib.pyplot as plt
import sys
import json

def run_simulation():
    """
    Envía una solicitud POST a la API de simulación ICU con los parámetros especificados
    y devuelve la respuesta JSON si la solicitud es exitosa.
    """
    url = "http://localhost:8080/api/simulation/run"
    params = {
        "nPatients": 6,
        "nWorkers": 8,
        "maxSteps": 30,  # Aproximadamente un año
        "gridWidth": 10,  # Definir el tamaño del grid
        "gridHeight": 10
    }

    print("Enviando solicitud con parámetros:", params)

    try:
        resp = requests.post(url, json=params)
        resp.raise_for_status()  # Lanza una excepción para códigos de error HTTP
        return resp.json()
    except requests.exceptions.RequestException as e:
        print("Error al conectar con la API:", e)
        sys.exit(1)

def plot_results(sim_result):
    """
    Toma los resultados de la simulación y genera gráficas para visualizar las métricas obtenidas.
    """
    # Verificar que la respuesta contenga los datos esperados
    expected_keys = [
        "totalWorkers",
        "totalPatients",
        "pctPatientsInfected",
        "pctWorkersInfected",
        "gridState"
    ]

    missing_keys = [key for key in expected_keys if key not in sim_result]
    if missing_keys:
        print(f"Advertencia: Las siguientes claves no se encuentran en la respuesta: {missing_keys}")

    # Extraer datos
    total_workers = sim_result.get("totalWorkers", 0)
    total_patients = sim_result.get("totalPatients", 0)
    pct_patients_infected = sim_result.get("pctPatientsInfected", [])
    pct_workers_infected = sim_result.get("pctWorkersInfected", [])
    grid_state = sim_result.get("gridState", [])

    # Validar que pct_patients_infected y pct_workers_infected sean listas
    if isinstance(pct_patients_infected, float):
        print("Error: 'pctPatientsInfected' es un float. Se esperaba una lista.")
        sys.exit(1)
    if isinstance(pct_workers_infected, float):
        print("Error: 'pctWorkersInfected' es un float. Se esperaba una lista.")
        sys.exit(1)

    # Validar que ambas listas tengan la misma longitud
    if len(pct_patients_infected) != len(pct_workers_infected):
        print("Error: Las listas 'pctPatientsInfected' y 'pctWorkersInfected' tienen longitudes diferentes.")
        sys.exit(1)

    # Crear eje de tiempo
    days = list(range(1, len(pct_patients_infected) + 1))

    # Crear una figura con dos subplots
    plt.figure(figsize=(14, 8))

    # Subplot 1: % Pacientes Infectados
    plt.subplot(2, 1, 1)
    plt.plot(days, pct_patients_infected, label="% Pacientes Infectados", color='red')
    plt.xlabel("Día")
    plt.ylabel("% Infectados")
    plt.title("Porcentaje de Pacientes Infectados a lo Largo del Año")
    plt.legend()
    plt.grid(True)

    # Subplot 2: % Trabajadores Infectados
    plt.subplot(2, 1, 2)
    plt.plot(days, pct_workers_infected, label="% Trabajadores Infectados", color='orange')
    plt.xlabel("Día")
    plt.ylabel("% Infectados")
    plt.title("Porcentaje de Trabajadores Infectados a lo Largo del Año")
    plt.legend()
    plt.grid(True)

    plt.tight_layout()
    plt.show()

    # Mostrar información adicional en la consola
    print("\n--- Resultados de la Simulación ---")
    print(f"Total Trabajadores: {total_workers}")
    print(f"Total Pacientes: {total_patients}")
    if len(pct_patients_infected) > 0:
        print(f"% Pacientes Infectados al Final: {pct_patients_infected[-1]:.2f}%")
    if len(pct_workers_infected) > 0:
        print(f"% Trabajadores Infectados al Final: {pct_workers_infected[-1]:.2f}%")

def plot_grid_state(sim_result, day=None):
    """
    Genera una representación visual del estado del grid en un día específico.
    Si no se especifica el día, se muestra el último día.
    """
    grid_state = sim_result.get("gridState", [])
    if not grid_state:
        print("No hay datos de grid para visualizar.")
        return

    if day is None:
        day = len(grid_state) - 1  # Último día

    if day < 0 or day >= len(grid_state):
        print("Día especificado fuera del rango de la simulación.")
        return

    cells = grid_state[day].get("cells", [])
    grid_width = max(cell['x'] for cell in cells) + 1
    grid_height = max(cell['y'] for cell in cells) + 1

    # Crear una matriz para visualizar el estado de KNN
    knn_matrix = [[' ' for _ in range(grid_width)] for _ in range(grid_height)]

    for cell in cells:
        x = cell['x']
        y = cell['y']
        knn_state = cell['knnState']
        knn_quantity = cell.get('knnQuantity', 0)
        if knn_state == "SUSCEPTIBLE":
            knn_matrix[y][x] = 'S'
        elif knn_state == "COLONIZED":
            knn_matrix[y][x] = 'C'
        elif knn_state == "INFECTED":
            knn_matrix[y][x] = 'I'

    # Crear una figura para visualizar el grid
    plt.figure(figsize=(6, 6))
    plt.imshow([[1 if cell == 'I' else 0.5 if cell == 'C' else 0 for cell in row] for row in knn_matrix],
               cmap='Reds', interpolation='nearest')
    plt.title(f"Estado del Grid en el Día {day + 1}")
    plt.xlabel("X")
    plt.ylabel("Y")

    # Añadir etiquetas
    for y in range(grid_height):
        for x in range(grid_width):
            plt.text(x, y, knn_matrix[y][x], ha='center', va='center', color='black')

    plt.colorbar(label='Estado de KNN')
    plt.show()

def main():
    print("Ejecutando la simulación ICU para un año (365 días)...")
    result = run_simulation()
    if result:
        print("Simulación completada. Graficando resultados...")
        plot_results(result)
        plot_grid_state(result)  # Visualizar el grid en el último día
    else:
        print("No se pudieron obtener resultados de la simulación.")

if __name__ == "__main__":
    main()
