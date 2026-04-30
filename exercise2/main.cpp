#include <eosim/dist/mt19937.hpp>
#include <eosim/core/experiment.hpp>
#include "hospitalsimple.hpp"
#include "constantes.hpp"

#include <iostream>
#include <fstream>

const unsigned int repeticiones = 4;

int main () {
	std::string s;
    using namespace eosim::core;
    //repito el experimento una cierta cantidad de veces
    for (int i = 0; i < repeticiones; i++) {
        HospitalSimple m(cantCamas, tasaArribos, tiempoEstadia);
        Experiment e;
        std::cout << "Arranco ...\n";
        m.connectToExp(&e);
        e.setSeed((unsigned long) i + 129);
        e.run(12000.0);
        std::cout << "Termine ...\n\n\n";
		m.lCola.print(10);
		std::cout << '\n';
		m.tEspera.print(52);
		std::cin >> s;
    }
}

