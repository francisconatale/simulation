# 🏥 Simulación de Hospital — Roadmap del Código

> Simulación discreta de eventos en un hospital con camas limitadas, cola de pacientes y un "roba-camas" concurrente.

---

## 🗺️ Visión General

El sistema modela **dos flujos paralelos** que compiten por el mismo recurso: las **camas del hospital**.

```
[Pacientes normales]  ──┐
                        ├──▶  [ CAMAS (bin) ]  ──▶  estadísticas
[RobaCamas]           ──┘
```

Ambos usan el mecanismo de **eventos discretos** (`BEvent`) de la librería `eosim::core`. Cada evento tiene una rutina (`eventRoutine`) que decide qué pasa cuando "dispara".

---

## 📦 Entidades y Eventos

### 1. `PacienteFeeder` — Generador de pacientes

**Rol:** Fabrica pacientes y los manda al sistema continuamente.

```
[PacienteFeeder dispara]
        │
        ▼
  ¿Hay cama libre?
     ┌──┴──┐
    SÍ    NO
     │     │
     ▼     ▼
 ocupa   entra a la cola
 cama    (h.cola.push)
     │
     ▼
 agenda SalidaPaciente
 (en h.estadia.sample() tiempo)
     │
     ▼
 agenda NUEVO PacienteFeeder
 (en h.arribos.sample() tiempo)
```

📊 **Registra:**
- `h.tEspera` → tiempo que el paciente esperó en cola antes de obtener cama
- `h.lCola` → longitud de la cola al momento de la llegada

---

### 2. `SalidaPaciente` — Alta del paciente

**Rol:** Libera la cama cuando el paciente termina su estadía.

```
[SalidaPaciente dispara]
        │
        ▼
  devuelve cama (returnBin 1)
        │
        ▼
  ¿Hay alguien en la cola?
     ┌──┴──┐
    SÍ    NO
     │     │
     ▼     ▼
 saca al  fin del evento
 primero
 de la cola
     │
     ▼
 le asigna cama inmediatamente
 agenda su SalidaPaciente
     │
     ▼
 registra tEspera del que esperó
 elimina la entidad que se fue (delete who)
```

📊 **Registra:**
- `h.tEspera` → tiempo en cola del paciente que recién fue admitido desde la cola
- `h.lCola` → longitud actual de la cola

---

### 3. `RobaCamas` — El competidor externo

**Rol:** Intenta tomar **2 camas** a la vez. Si no puede, reintenta en 2 unidades de tiempo.

```
[RobaCamas dispara]
        │
        ▼
  ¿Hay 2 camas disponibles?
     ┌──┴──┐
    SÍ    NO
     │     │
     ▼     ▼
 toma 2  agenda reintento
 camas   en t+2.0
     │
     ▼
 registra tEspera
 agenda DevuelveCama
 (en h.robaCamas.sample() tiempo)
```

---

### 4. `DevuelveCama` — Devolución por el roba-camas

**Rol:** Libera las 2 camas que el roba-camas tomó y vuelve a planificar otro intento.

```
[DevuelveCama dispara]
        │
        ▼
  devuelve 2 camas (returnBin 2)
        │
        ▼
  agenda nuevo RobaCamas en t+10.0
```

---

## ⏱️ Ciclo de Vida Completo

```
t=0
 │
 ├──▶ PacienteFeeder (primer paciente)
 │         │
 │         └──▶ [cada arribo agenda el siguiente]
 │
 ├──▶ RobaCamas (primer intento)
 │         │
 │         └──▶ si falla → reintenta en t+2
 │               si éxito → DevuelveCama en t+robaCamas.sample()
 │                               └──▶ nuevo RobaCamas en t+10
 │
 └──▶ ... simulación continúa hasta condición de parada
```

---

## 📊 Estadísticas Recolectadas

| Variable      | Qué mide                                              | Dónde se registra              |
|---------------|-------------------------------------------------------|--------------------------------|
| `h.tEspera`   | Tiempo que cada entidad esperó para obtener camas     | `PacienteFeeder`, `SalidaPaciente`, `RobaCamas` |
| `h.lCola`     | Longitud de la cola en el momento de la llegada o alta | `PacienteFeeder`, `SalidaPaciente` |

---

## 🔑 Conceptos Clave del Framework

| Concepto         | Descripción                                                     |
|------------------|-----------------------------------------------------------------|
| `BEvent`         | Evento base. Se hereda para definir comportamiento con `eventRoutine` |
| `Entity`         | Unidad que viaja por el sistema. Tiene un reloj (`getClock()`)  |
| `h.schedule(t, who, id)` | Agenda el evento `id` para la entidad `who` en `t` unidades futuras |
| `h.camas` (bin)  | Recurso compartido. Se toma con `acquire(n)` y se devuelve con `returnBin(n)` |
| `h.cola`         | Cola FIFO de entidades esperando una cama                       |
| `sample()`       | Toma un valor de una distribución aleatoria (tiempos variables) |

---

## 💡 Resumen Conceptual

> El código implementa una **simulación de eventos discretos** donde dos tipos de "actores" compiten por un recurso escaso (camas). Los pacientes normales forman una cola ordenada; el roba-camas es un actor externo que interrumpe tomando de a 2. El sistema registra cuánto esperó cada actor y qué tan larga estaba la cola, lo que permite analizar la eficiencia del hospital bajo distintos parámetros.
