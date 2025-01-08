import requests
import matplotlib.pyplot as plt
import sys
import json
import matplotlib.animation as animation

def run_simulation():
    """
    Envía una solicitud POST a la API de simulación ICU con los parámetros especificados
    y devuelve la respuesta JSON si la solicitud es exitosa.
    """
    url = "http://localhost:8080/api/simulation/run"
    params = {
        "nPatients": 50,
        "nWorkers": 20,
        "maxSteps": 26280,  # 365 días * 24 pasos/día (8 horas * 3 pasos/hora)
        "gridWidth": 10,  # Definir el tamaño del grid
        "gridHeight": 10,
        "colonizationChance": 0.25,
        "infectionFromColonizedChance": 0.15,
        "seed": 42,  # Opcional: para reproducibilidad
        "saveLogs": True  # Opcional: para guardar logs
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
        "gridState",
        "bacteriaCounts"
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
    bacteria_counts = sim_result.get("bacteriaCounts", [])

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
    steps = len(pct_patients_infected)
    days = [step // 24 for step in range(1, steps + 1)]  # Convertir pasos a días

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
    bacteria_counts = sim_result.get("bacteriaCounts", [])
    if not grid_state or not bacteria_counts:
        print("No hay datos de grid para visualizar.")
        return

    if day is None:
        day = len(grid_state) - 1  # Último día

    if day < 0 or day >= len(grid_state):
        print("Día especificado fuera del rango de la simulación.")
        return

    cells = grid_state[day].get("cells", [])
    bacteria_day = bacteria_counts[day].get("bacteriaCounts", [])
    grid_width = max(cell['x'] for cell in cells) + 1
    grid_height = max(cell['y'] for cell in cells) + 1

    # Crear matrices para el estado y la cantidad de KNN
    knn_matrix = [[' ' for _ in range(grid_width)] for _ in range(grid_height)]
    quantity_matrix = [[0 for _ in range(grid_width)] for _ in range(grid_height)]

    for cell in cells:
        x = cell['x']
        y = cell['y']
        knn_state = cell['knnState']
        knn_quantity = cell.get('knnQuantity', 0)
        quantity_matrix[y][x] = knn_quantity
        if knn_state == "SUSCEPTIBLE":
            knn_matrix[y][x] = 'S'
        elif knn_state == "COLONIZED":
            knn_matrix[y][x] = 'C'
        elif knn_state == "INFECTED":
            knn_matrix[y][x] = 'I'

    # Crear una figura para visualizar el grid
    plt.figure(figsize=(8, 6))

    # Crear un heatmap para las cantidades de KNN
    plt.imshow(quantity_matrix, cmap='Blues', interpolation='nearest')
    plt.title(f"Cantidad de KPN en el Grid en el Día {day + 1}")
    plt.xlabel("X")
    plt.ylabel("Y")
    plt.colorbar(label='Cantidad de KPN')

    # Añadir etiquetas de estado
    for y in range(grid_height):
        for x in range(grid_width):
            plt.text(x, y, f"{knn_matrix[y][x]}\n{quantity_matrix[y][x]}",
                     ha='center', va='center', color='black', fontsize=8)

    plt.xticks(range(grid_width))
    plt.yticks(range(grid_height))
    plt.grid(True)
    plt.show()

def animate_grid(sim_result):
    """
    Genera una animación visualizando la evolución del grid a lo largo de los días.
    """
    grid_state = sim_result.get("gridState", [])
    bacteria_counts = sim_result.get("bacteriaCounts", [])
    if not grid_state or not bacteria_counts:
        print("No hay datos de grid para animar.")
        return

    fig, ax = plt.subplots(figsize=(8, 6))

    def update(frame):
        ax.clear()
        cells = grid_state[frame].get("cells", [])
        bacteria_day = bacteria_counts[frame].get("bacteriaCounts", [])
        grid_width = max(cell['x'] for cell in cells) + 1
        grid_height = max(cell['y'] for cell in cells) + 1

        # Crear matrices para el estado y la cantidad de KNN
        knn_matrix = [[' ' for _ in range(grid_width)] for _ in range(grid_height)]
        quantity_matrix = [[0 for _ in range(grid_width)] for _ in range(grid_height)]

        for cell in cells:
            x = cell['x']
            y = cell['y']
            knn_state = cell['knnState']
            knn_quantity = cell.get('knnQuantity', 0)
            quantity_matrix[y][x] = knn_quantity
            if knn_state == "SUSCEPTIBLE":
                knn_matrix[y][x] = 'S'
            elif knn_state == "COLONIZED":
                knn_matrix[y][x] = 'C'
            elif knn_state == "INFECTED":
                knn_matrix[y][x] = 'I'

        # Crear un heatmap para las cantidades de KNN
        cax = ax.imshow(quantity_matrix, cmap='Blues', interpolation='nearest')
        ax.set_title(f"Cantidad de KPN en el Grid en el Día {frame + 1}")
        ax.set_xlabel("X")
        ax.set_ylabel("Y")
        plt.colorbar(cax, ax=ax, label='Cantidad de KPN')

        # Añadir etiquetas de estado
        for y in range(grid_height):
            for x in range(grid_width):
                ax.text(x, y, f"{knn_matrix[y][x]}\n{quantity_matrix[y][x]}",
                        ha='center', va='center', color='black', fontsize=8)

        ax.set_xticks(range(grid_width))
        ax.set_yticks(range(grid_height))
        ax.grid(True)

    ani = animation.FuncAnimation(fig, update, frames=len(grid_state), repeat=False)
    plt.show()

def main():
    print("Ejecutando la simulación ICU para un año (365 días)...")
    result = run_simulation()
    if result:
        print("Simulación completada. Graficando resultados...")
        plot_results(result)
        plot_grid_state(result)  # Visualizar el grid en el último día
        animate_grid(result)     # Animar la evolución del grid
    else:
        print("No se pudieron obtener resultados de la simulación.")

if __name__ == "__main__":
    main()
