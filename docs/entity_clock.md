# 🧩 Entity — Comportamiento del Clock

> Entender cuándo se setea `getClock()` es clave para calcular correctamente los tiempos de espera y uso.

---

## 📄 Implementación

```cpp
Entity::Entity(): bEv(0), clock(0) {}

void Entity::setClock(double clock) {
    this->clock = clock;
}

double Entity::getClock() {
    return clock;
}
```

`clock` arranca en `0` y **solo se actualiza cuando la entidad es agendada** con `schedule()`.

---

## ⏱️ Cuándo se setea el clock

```
Entidad llega al sistema
        │
        ▼
  schedule(feeder)
  setClock = t_llegada
        │
        ▼
  ¿Recurso disponible?
     ┌──┴──┐
    SÍ    NO
     │     │
     ▼     ▼
schedule(salida)   cola.push(who)
setClock = t_adq   ← NO se agenda
                   ← clock NO cambia
                   ← sigue siendo t_llegada
     │                     │
     ▼                     ▼ (cuando se libera un recurso)
  ...               schedule(salida)
                    setClock = t_adq
```

---

## 📊 Estados del clock a lo largo del ciclo

| Momento | Se agenda | `getClock()` vale |
|---------|-----------|-------------------|
| Llega al sistema | `schedule(feeder)` | `t_llegada` |
| Entra a la cola | nada | `t_llegada` ✅ |
| Adquiere el recurso | `schedule(salida)` | `t_adquisicion` ✅ |
| Sale del sistema | nada (se hace delete) | `t_adquisicion` ✅ |

---

## 🧮 Por qué los cálculos funcionan

### `tEspera` — tiempo en cola

Se calcula en dos lugares:

**En el Feeder** (entró directo, no esperó):
```cpp
// getClock() == t_llegada
// getSimTime() == t_llegada (mismo instante)
m.tEspera.log(m.getSimTime() - who->getClock()); // → 0
```

**En la Salida** (estaba en cola):
```cpp
Entity* ent = m.cola.pop();
// ent->getClock() == t_llegada  (nunca fue pisado mientras esperaba)
// getSimTime()    == t_adquisicion
m.tEspera.log(m.getSimTime() - ent->getClock()); // → tiempo real en cola ✅
```

---

### `tUso` — tiempo usando el recurso

Se calcula en la Salida:
```cpp
// who->getClock() == t_adquisicion  (seteado cuando se agendó esta salida)
// getSimTime()    == t_salida
m.tUso.log(m.getSimTime() - who->getClock()); // → tiempo real de uso ✅
```

---

## 🔑 Regla general

> `getClock()` siempre refleja **el momento en que se agendó el último evento** sobre esa entidad.
> La cola no agenda nada → no toca el clock → el clock "congela" el momento de llegada mientras la entidad espera.

---

## ⚠️ Trampa frecuente

Si agendás algo sobre la entidad mientras está en cola (por ejemplo, un timeout), el clock se pisaría y perderías el `t_llegada`:

```cpp
// ❌ esto rompe tEspera:
m.schedule(10.0, who, timeoutID); // setClock = t_ahora, t_llegada se pierde
m.cola.push(who);
```

Si necesitás ese caso, hay que guardar el `t_llegada` por separado antes de agendar.
