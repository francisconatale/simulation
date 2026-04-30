# 📋 Template de Eventos — Patrón Feeder / Salida

> Patrón base que se repite en toda simulación de colas con recursos limitados.

---

## 🔁 Patrón General

```
ENTIDAD llega
    │
    ▼
¿Recurso disponible?
   ┌──┴──┐
  SÍ    NO
   │     │
   ▼     ▼
planifica  mete en cola
salida     registra longitud de cola
registra
tEspera=0
   │
   ▼
genera nueva entidad
(agenda próximo feeder)
```

---

## 📥 Evento Feeder — Template

**Responsabilidades:**
1. Intentar adquirir el recurso
2. Si hay recurso → planificar la salida + registrar espera
3. Si no hay recurso → encolar + registrar longitud de cola
4. Siempre → generar la próxima entidad entrante

```cpp
void XFeeder::eventRoutine(Entity* who) {
    MiModelo& m = dynamic_cast<MiModelo&>(owner);

    if (m.recurso.isAvailable(N)) {
        // --- RECURSO LIBRE ---
        m.recurso.acquire(N);

        // registra tiempo de espera (quien llegó directo no esperó)
        m.tEspera.log(m.getSimTime() - who->getClock());

        // planifica la salida de esta entidad
        m.schedule(m.tiempoEnSistema.sample(), who, salidaID);

    } else {
        // --- RECURSO OCUPADO ---

        // registra cuántos hay esperando antes de entrar
        m.lCola.log(m.cola.size());

        // encola la entidad
        m.cola.push(who);
    }

    // genera la próxima entidad entrante (siempre)
    m.schedule(m.tiempoEntreArribos.sample(), new Entity(), feederID);
}
```

### Entradas
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `who` | `Entity*` | La entidad que acaba de llegar |
| `m.recurso` | `Bin` | Recurso compartido (camas, servidores, etc.) |
| `N` | `int` | Cantidad de unidades del recurso que necesita |
| `m.tiempoEnSistema` | distribución | Cuánto tiempo usará el recurso |
| `m.tiempoEntreArribos` | distribución | Cuándo llega la próxima entidad |

### Salidas / Efectos
| Efecto | Condición |
|--------|-----------|
| `recurso.acquire(N)` | Si había lugar |
| `schedule(salida)` | Si había lugar |
| `tEspera.log(0)` | Si había lugar (espera = 0) |
| `lCola.log(size)` | Si no había lugar |
| `cola.push(who)` | Si no había lugar |
| `schedule(feeder)` con nueva entidad | Siempre |

---

## 📤 Evento Salida — Template

**Responsabilidades:**
1. Liberar el recurso
2. Si hay cola → sacar el primero, asignarle el recurso, registrar su espera, planificar su salida
3. Si no hay cola → no hacer nada más
4. Eliminar la entidad que se va

```cpp
void XSalida::eventRoutine(Entity* who) {
    MiModelo& m = dynamic_cast<MiModelo&>(owner);

    // libera el recurso
    m.recurso.returnBin(N);

    if (!m.cola.empty()) {
        // --- HAY ENTIDADES ESPERANDO ---
        m.recurso.acquire(N);

        // saca al primero de la cola
        Entity* siguiente = m.cola.pop();

        // registra cuántos quedaron en cola
        m.lCola.log(m.cola.size());

        // registra cuánto esperó
        m.tEspera.log(m.getSimTime() - siguiente->getClock());

        // planifica su salida
        m.schedule(m.tiempoEnSistema.sample(), siguiente, salidaID);
    }

    // elimina la entidad que se va
    delete who;
}
```

### Entradas
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `who` | `Entity*` | La entidad que termina y se va |
| `m.recurso` | `Bin` | Recurso a liberar |
| `N` | `int` | Cantidad de unidades a devolver |
| `m.cola` | `Queue` | Cola de entidades esperando |

### Salidas / Efectos
| Efecto | Condición |
|--------|-----------|
| `recurso.returnBin(N)` | Siempre |
| `recurso.acquire(N)` | Si había alguien en cola |
| `cola.pop()` | Si había alguien en cola |
| `lCola.log(size)` | Si había alguien en cola |
| `tEspera.log(espera)` | Si había alguien en cola |
| `schedule(salida)` para el siguiente | Si había alguien en cola |
| `delete who` | Siempre |

---

## 📊 Estadísticas del Patrón

| Histograma | Se registra en | Qué captura |
|------------|---------------|-------------|
| `tEspera` | Feeder (recurso libre) y Salida (saca de cola) | Tiempo desde que llegó hasta que obtuvo el recurso |
| `lCola` | Feeder (recurso ocupado) y Salida (saca de cola) | Ocupación de la cola en ese instante |

---

## 🔄 Variantes del Patrón

### Variante: recurso de N unidades (RobaCamas)
Igual al patrón base pero con `acquire(2)` / `returnBin(2)`. Si falla, reintenta con `schedule(t, who, mismoEventoID)` en lugar de encolar.

### Variante: proliferación en fallo
En lugar de reintentar con la misma entidad, crear una nueva por cada fallo:
```cpp
// en lugar de: m.schedule(2.0, who, mismoID);
m.schedule(2.0, who, mismoID);
m.schedule(2.0, new Entity(), mismoID); // genera competidor extra
```

---

> **Regla mnemotécnica:**
> - *Feeder*: ¿hay lugar? → adentro y agenda salida. ¿no hay? → cola. Siempre genera el siguiente.
> - *Salida*: libera → mira la cola → si hay alguien, lo atiende. Siempre borra al que se fue.
