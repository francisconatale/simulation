#include <eosim/core/experiment.hpp>
#include "hospitalsimple.hpp"
#include "constantes.hpp"
#include <iostream>

int main () {
    using namespace eosim::core;

    HospitalSimple model;
    Experiment exp;

    model.connectToExp(&exp);
    exp.setSeed(12345);

    std::cout << "Simulando flujo de pacientes por 300 dias...\n";
    exp.run(300.0);
    std::cout << "Simulacion finalizada.\n\n";

    std::cout << "=== ESTADISTICAS DE COLAS (Largo Medio) ===\n";
    model.lColaEnfermera.print(10);
    model.lColaMedico.print(10);
    model.lColaPartera.print(10);
    model.lColaConjunta.print(10);

    std::cout << "\n=== ESTADISTICAS DE TIEMPOS DE ESPERA ===\n";
    model.tEsperaEnfermera.print(10);
    model.tEsperaMedico.print(10);
    model.tEsperaPartera.print(10);
    model.tEsperaConjunta.print(10);

    return 0;
}
