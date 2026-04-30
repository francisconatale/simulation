#include "autoserviciosimple.hpp"
#include <eosim/core/entity.hpp>
#include <eosim/dist/numbergenerator.hpp>
#include <iostream>

using namespace eosim::core;
using namespace eosim::dist;
using namespace std;

AutoServicioSimple::AutoServicioSimple(unsigned int cantCafeteras, double tasaArribos):
								tasaArribos(tasaArribos),
								llegadaCliente(*this),
								salidaCliente(*this),
								arribos(MT19937, tasaArribos),
								preparacionCafe(MT19937, 27.0, 1.5),
								cafeteras(cantCafeteras, cantCafeteras),
								tEspera("Tiempos de Espera"),
								lCola("Largos Medios de Colas", *this),
								tUso("Tiempo de uso") {}

AutoServicioSimple::~AutoServicioSimple() {}

void AutoServicioSimple::init() {
	registerBEvent(&salidaCliente);
	registerBEvent(&llegadaCliente);
	registerDist(&preparacionCafe);
	registerDist(&arribos);
}

void AutoServicioSimple::doInitialSchedules() {
	schedule(0.0, new Entity(), ::llegadaCliente);
}
