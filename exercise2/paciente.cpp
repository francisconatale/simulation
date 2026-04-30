#include "paciente.hpp"
#include "hospitalsimple.hpp"
#include <iostream>
using namespace eosim::core;


// en el constructor se utiliza el identificador definido en paciente.hpp
// en el constructor se utiliza el identificador definido en pacientefeeder.hpp
PacienteFeeder::PacienteFeeder(Model& model): BEvent(pacienteF, model) {}

PacienteFeeder::~PacienteFeeder() {}


RobaCamas::RobaCamas(Model& model): BEvent(robaCama, model) {}

RobaCamas::~RobaCamas() {}

void RobaCamas::eventRoutine(Entity* who){
   // std::cout << "llego un robacamas " << who->getClock() << "\n";
    HospitalSimple& h = dynamic_cast<HospitalSimple&>(owner);
    if(h.camas.isAvailable(2)){
        h.camas.acquire(2);
        std::cout << "el roba camas pudo robar dos camas " << h.getSimTime() << "\n";
        h.tEspera.log(h.getSimTime() - who->getClock());
        h.schedule(h.robaCamas.sample(), who, devuelveCama);
    } else {
   //  std::cout << "el roba camas no pudo robar dos camas " << h.getSimTime() << "\n";
    h.schedule(2.0, who, robaCama);
    }
}

DevuelveCama::DevuelveCama(Model& model): BEvent(devuelveCama, model) {}

DevuelveCama::~DevuelveCama() {}

void DevuelveCama::eventRoutine(Entity* who){
  HospitalSimple& h = dynamic_cast<HospitalSimple&>(owner);
  std::cout << "el roba camas devolvio la cama" << h.getSimTime() << "\n";
  h.camas.returnBin(2);
  h.schedule(10.0, who, robaCama);
}

void PacienteFeeder::eventRoutine(Entity* who) {
	// se anuncia la llegada del paciente
	std::cout << "llego un paciente en " << who->getClock() << "\n";
	// se castea owner a un HospitalSimple
	HospitalSimple& h = dynamic_cast<HospitalSimple&>(owner);
	if (h.camas.isAvailable(1)) {
		h.camas.acquire(1);
		std::cout << "un paciente fue aceptado en una cama " << h.getSimTime() << "\n";
		h.tEspera.log(h.getSimTime() - who->getClock());
		double arribo = h.estadia.sample();
		h.tUso.log(arribo);
		h.schedule(arribo, who, salidaP);
	}
	else {
        // se acumulan datos en los histogramas
        h.lCola.log(h.cola.size());
		// se pone al paciente recien llegado en la cola
		h.cola.push(who);
	}
    // se agenda el arribo del un nuevo paciente
	h.schedule(h.arribos.sample(), new Entity(), pacienteF);
}

// en el constructor se utiliza el identificador definido en paciente.hpp
SalidaPaciente::SalidaPaciente(Model& model): BEvent(salidaP, model) {}

SalidaPaciente::~SalidaPaciente() {}

void SalidaPaciente::eventRoutine(Entity* who) {

	// se informa la salida de un paciente
	std::cout << "un paciente se retira en " << who->getClock() << "\n";
	// se castea owner a un HospitalSimple
	HospitalSimple& h = dynamic_cast<HospitalSimple&>(owner);
	   std::cout << "getClock=" << who->getClock()
              << " simTime=" << h.getSimTime() << "\n";
	// se retorna la cama que el paciente ocupaba
	h.camas.returnBin(1);
	if (!h.cola.empty()) {
		h.camas.acquire(1);
		std::cout << "un paciente fue aceptado en una cama " << h.getSimTime() << "\n";
        h.lCola.log(h.cola.size());
		Entity* ent = h.cola.pop();
		h.tEspera.log(h.getSimTime() - ent->getClock());
		h.schedule(h.estadia.sample(), ent, salidaP); // aca le cambia el tiempo al who
	}
	// se elimina  al paciente del sistema
	delete who;
}
