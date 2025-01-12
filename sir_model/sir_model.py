import numpy as np
from scipy.integrate import odeint
import matplotlib.pyplot as plt

# Modelo SIE
def sir_model(y, t, beta, gamma, mu):
    S, I, R = y
    N = S + I + R
    dSdt = mu * N - beta * S * I / N - mu * S
    dIdt = beta * S * I / N - gamma * I - mu * I
    dRdt = gamma * I - mu * R
    return [dSdt, dIdt, dRdt]

def main():
    # Parámetros del modelo
    beta = 0.3      # Tasa de transmisión
    gamma = 0.1     # Tasa de recuperación
    mu = 0.01       # Tasa de nacimiento/mortalidad

    # Condiciones iniciales
    N = 1000        # Población total
    I0 = 1          # Individuo inicialmente infectado
    R0 = 0          # Inicialmente recuperados
    S0 = N - I0 - R0  # Inicialmente susceptibles
    y0 = [S0, I0, R0]

    # Intervalo de tiempo (en días)
    t = np.linspace(0, 160, 160)

    # Resolver las ecuaciones diferenciales
    solution = odeint(sir_model, y0, t, args=(beta, gamma, mu))
    S, I, R = solution.T

    # Graficar los resultados
    plt.figure(figsize=(10,6))
    plt.plot(t, S, 'b', label='Susceptibles')
    plt.plot(t, I, 'r', label='Infectados')
    plt.plot(t, R, 'g', label='Recuperados')
    plt.xlabel('Tiempo / días')
    plt.ylabel('Número de individuos')
    plt.title('Modelo SIR con Nacimientos y Muertes')
    plt.legend()
    plt.grid(True)
    plt.show()

if __name__ == "__main__":
    main()
