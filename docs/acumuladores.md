# Acumuladores — `Observation` vs `TimeWeighted`

## `Observation`

Suma los valores y divide por la cantidad de observaciones. Promedio clásico.

```cpp
tEspera.log(5.0);
tEspera.log(3.0);
tEspera.log(8.0);
// promedio = (5 + 3 + 8) / 3 = 5.33
```

---

## `TimeWeighted`

Suma los valores multiplicados por cuánto tiempo duraron, y divide por el tiempo total. Promedio ponderado por duración.

```cpp
void TimeWeighted::log(double value) {
    Histogram::log(value, m.getSimTime() - lastTime);
    // peso = tiempo transcurrido desde el último log
    lastTime = m.getSimTime();
}
```

Ejemplo con `lCola`:

```
t=0  → log(0)  peso=3   (0 en cola durante 3 unidades)
t=3  → log(2)  peso=5   (2 en cola durante 5 unidades)
t=8  → log(1)  peso=2   (1 en cola durante 2 unidades)

promedio = (0×3 + 2×5 + 1×2) / (3+5+2) = 12/10 = 1.2
```

Sin ponderación daría `(0+2+1)/3 = 1.0`, ignorando que la cola de 2 duró más. Con `TimeWeighted` ese tiempo importa.

### Por qué importa la ponderación

Con un caso extremo se ve claro:

```
cola = 100 personas  durante    1 segundo
cola =   0 personas  durante  999 segundos

Observation   → (100 + 0) / 2          = 50    ← engañoso
TimeWeighted  → (100×1 + 0×999) / 1000 = 0.1   ← real
```

---

## Comparación

| | `Observation` | `TimeWeighted` |
|--|---------------|----------------|
| Peso de cada log | igual para todos | tiempo desde el log anterior |
| Qué modela | eventos puntuales independientes | estado continuo del sistema |
| Ejemplos típicos | `tEspera`, `tUso` | `lCola`, ocupación de recursos |
| Pregunta que responde | ¿cuánto esperó cada entidad? | ¿cuál fue el estado promedio del sistema? |

---

## Regla para elegir

- ¿Estás midiendo algo que le pasa a una entidad? → `Observation`
- ¿Estás midiendo el estado de algo del sistema a lo largo del tiempo? → `TimeWeighted`

---

## Error frecuente — `tUso` como `TimeWeighted`

```cpp
// incorrecto:
eosim::statics::TimeWeighted tUso;

// correcto:
eosim::statics::Observation tUso;
```

`tUso` registra cuánto usó el recurso cada entidad individualmente. Es una observación puntual, no un estado continuo. Usar `TimeWeighted` pondería cada uso por el tiempo transcurrido desde el log anterior, lo que no tiene significado real.
