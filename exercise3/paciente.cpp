#include "paciente.hpp"
#include "autoserviciosimple.hpp"
#include <iostream>
using namespace eosim::core;

LlegadaCliente::LlegadaCliente(Model& model): BEvent(llegadaCliente, model) {}
LlegadaCliente::~LlegadaCliente() {}


void LlegadaCliente::eventRoutine(Entity* who){
    AutoServicioSimple& h = dynamic_cast<AutoServicioSimple&>(owner);
    if(h.cafeteras.isAvailable(1)){
        h.cafeteras.acquire(1);
		std::cout << "un cliente fue atendido en" << h.getSimTime() << "\n";
        h.tEspera.log(h.getSimTime() - who->getClock());
		double preparacion = h.preparacionCafe.sample();
		h.tUso.log(preparacion);
        h.schedule(preparacion, who, salidaCliente);
    } else {
		h.cola.push(who);
		h.lCola.log(h.cola.size());
		std::cout << "un cliente esta esperando por su cafe en" << h.getSimTime() << "\n";
    }
	h.schedule(h.arribos.sample(), new Entity(), llegadaCliente);
}

SalidaCliente::SalidaCliente(Model& model): BEvent(salidaCliente, model) {}
SalidaCliente::~SalidaCliente() {}

void SalidaCliente::eventRoutine(Entity* who){
  AutoServicioSimple& h = dynamic_cast<AutoServicioSimple&>(owner);
  h.cafeteras.returnBin(1);
  if(!h.cola.empty()){
	Entity* ent = h.cola.pop();
	h.lCola.log(h.cola.size());
	h.tEspera.log(h.getSimTime() - ent->getClock());
	double preparacion = h.preparacionCafe.sample();
	h.tUso.log(preparacion);
	h.cafeteras.acquire(1);
	h.schedule(preparacion, ent, salidaCliente);
  }
  delete who;
}
