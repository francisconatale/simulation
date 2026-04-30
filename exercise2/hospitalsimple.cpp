#include "hospitalsimple.hpp"
#include <eosim/core/entity.hpp>
#include <eosim/dist/numbergenerator.hpp>
#include <iostream>

using namespace eosim::core;
using namespace eosim::dist;

using namespace std;

HospitalSimple::HospitalSimple(unsigned int cantCamas, double tasaArribos, double tiempoEstadia):
// se contruyen los eventos B, los eventos C, las distribuciones, los recursos y los histogramas
								tasaArribos(tasaArribos),
								tiempoEstadia(tiempoEstadia),
								pF(*this),
								sP(*this),
								rc(*this),
								dc(*this),
								arribos(MT19937, tasaArribos),
								estadia(MT19937, tiempoEstadia),
                                robaCamas(MT19937, 120.0, 4.0),
								camas(cantCamas, cantCamas),
								tEspera("Tiempos de Espera"),
								tUso("Tiempo de uso"),
								lCola("Largos Medios de Colas", *this) {}

HospitalSimple::~HospitalSimple() {}

void HospitalSimple::init() {
	// registro los eventos B
	registerBEvent(&rc);
	registerBEvent(&dc);
	registerBEvent(&pF);
	registerBEvent(&sP);

	// registro las distribuciones
	registerDist(&arribos);
	registerDist(&robaCamas);
	registerDist(&estadia);
}

void HospitalSimple::doInitialSchedules() {
	// agendo el primer paciente
	schedule(0.0, new Entity(), pacienteF);
	schedule(0.0, new Entity(), robaCama);
}

