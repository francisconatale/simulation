# Simulación de Teoría de Colas en Java

Implementación pura de cálculos analíticos (sin GUI) para los modelos:
- **M/M/1** — Un servidor, capacidad infinita
- **M/M/C** — C servidores en paralelo, capacidad infinita
- **M/M/C/K** — C servidores, capacidad finita K
- **Redes de colas abiertas de Jackson** — Red de nodos M/M/c

---

## Compilar y ejecutar

```bash
mkdir out
javac src/*.java -d out
java -cp out Main
```

---

## Archivos

| Archivo | Descripción |
|---|---|
| `src/MM1Queue.java` | Modelo M/M/1 |
| `src/MMCQueue.java` | Modelo M/M/C (Erlang-C) |
| `src/MMCKQueue.java` | Modelo M/M/C/K (capacidad finita) |
| `src/QueueNode.java` | Nodo de red de colas |
| `src/QueueNetwork.java` | Red de Jackson abierta |
| `src/Main.java` | Demos y punto de entrada |

---

## M/M/1

**Parámetros:** `lambda` (λ), `mu` (μ)  
**Condición de estabilidad:** ρ = λ/μ < 1

| Métrica | Fórmula |
|---|---|
| ρ (utilización) | λ / μ |
| P₀ | 1 − ρ |
| Pₙ | (1−ρ) · ρⁿ |
| L (en sistema) | ρ / (1−ρ) |
| Lq (en cola) | ρ² / (1−ρ) |
| W (tiempo sistema) | 1 / (μ−λ) |
| Wq (tiempo cola) | λ / [μ(μ−λ)] |

```java
MM1Queue q = new MM1Queue(4.0, 6.0); // lambda=4, mu=6
q.printReport();

// Acceso directo:
double lq = q.getLq();
double wq = q.getWq();
double pn = q.getPn(3);       // P(N=3)
double pgt = q.getPMoreThan(5); // P(N>5)
```

---

## M/M/C

**Parámetros:** `lambda` (λ), `mu` (μ por servidor), `c` (servidores)  
**Condición de estabilidad:** ρ = λ/(c·μ) < 1

| Métrica | Fórmula |
|---|---|
| a (carga ofrecida) | λ / μ |
| ρ (utilización por servidor) | λ / (c·μ) |
| P₀ | [Σₖ₌₀ᶜ⁻¹ aᵏ/k! + aᶜ/(c!·(1−ρ))]⁻¹ |
| C(c,a) (Erlang-C) | [aᶜ / (c!·(1−ρ))] · P₀ |
| Lq | C(c,a) · ρ / (1−ρ) |
| L | Lq + a |
| Wq | Lq / λ |
| W | Wq + 1/μ |

```java
MMCQueue q = new MMCQueue(10.0, 4.0, 3); // lambda=10, mu=4, c=3
q.printReport();

double erlangC = q.getErlangC(); // P(cliente espera)
double wq = q.getWq();
```

---

## M/M/C/K

**Parámetros:** `lambda` (λ), `mu` (μ por servidor), `c` (servidores), `K` (capacidad máxima, K ≥ c)  
**No requiere condición de estabilidad** (K finito garantiza estado estacionario).

| Métrica | Fórmula |
|---|---|
| Pₙ (n ≤ c) | P₀ · aⁿ / n! |
| Pₙ (c < n ≤ K) | P₀ · aⁿ / (c! · cⁿ⁻ᶜ) |
| PK (bloqueo) | Pₖ (prob. sistema lleno) |
| λeff | λ · (1 − PK) |
| L | Σₙ₌₀ᴷ n · Pₙ |
| Lq | Σₙ₌ᶜ₊₁ᴷ (n−c) · Pₙ |
| W | L / λeff |
| Wq | Lq / λeff |

```java
MMCKQueue q = new MMCKQueue(8.0, 3.0, 2, 5); // lambda=8, mu=3, c=2, K=5
q.printReport();

double pk  = q.getBlockingProbability(); // P(sistema lleno)
double leff = q.getEffectiveLambda();   // tasa efectiva
double pWait = q.getProbabilityWait();  // P(cliente admitido espera)
```

---

## Red de Colas de Jackson (Abierta)

**Parámetros:**
- Nodos: cada uno con `mu`, `servers` (c), `externalRate` (λ externo)
- Matriz de ruteo `p[i][j]`: probabilidad de ir del nodo i al nodo j

**Ecuaciones de tráfico:** (I − Pᵀ) · Λ = λ  
Resuelto por eliminación gaussiana con pivoteo parcial.

**Por el Teorema de Jackson:** cada nodo se analiza como M/M/c independiente con Λᵢ.

```java
QueueNode nodeA = new QueueNode("A", 6.0, 1, 3.0); // (nombre, mu, c, lambda_ext)
QueueNode nodeB = new QueueNode("B", 5.0, 2, 1.0);
QueueNode nodeC = new QueueNode("C", 4.0, 1, 0.0);

double[][] routing = {
    //  →A    →B    →C
    { 0.00, 0.60, 0.00 },  // desde A
    { 0.10, 0.00, 0.50 },  // desde B
    { 0.00, 0.20, 0.00 }   // desde C
};

QueueNetwork net = new QueueNetwork(
    new QueueNode[]{nodeA, nodeB, nodeC}, routing
);
net.solve();
net.printReport();

// Acceso por nodo:
System.out.println(nodeA.getL());  // L en nodo A
System.out.println(nodeA.getWq()); // Wq en nodo A

// Totales de red:
System.out.println(net.getNetworkL());  // suma de L_i
System.out.println(net.getNetworkW()); // W de extremo a extremo
```

---

## Convenciones de notación

| Símbolo | Significado |
|---|---|
| λ | Tasa de llegada (clientes/tiempo) |
| μ | Tasa de servicio por servidor |
| c | Número de servidores |
| K | Capacidad máxima del sistema |
| ρ | Intensidad de tráfico / utilización |
| L | Número promedio en el sistema |
| Lq | Número promedio en cola |
| W | Tiempo promedio en el sistema |
| Wq | Tiempo promedio en cola |
| Λᵢ | Tasa total de llegadas al nodo i (red) |
