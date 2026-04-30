#ifndef PACIENTE_HPP_
#define PACIENTE_HPP_

#include <eosim/core/bevent.hpp>
#include <eosim/core/entity.hpp>
#include <string>

// identificador del evento fijo PacienteFeeder
const std::string pacienteF = "PacienteFeeder";


class PacienteFeeder: public eosim::core::BEvent {
public:
	// constructor
	PacienteFeeder(eosim::core::Model& model);
	// destructor
	~PacienteFeeder();
	// rutina del evento fijo
	void eventRoutine(eosim::core::Entity* who);
};

const std::string robaCama = "RobaCama";

class RobaCamas: public eosim::core::BEvent {
public:

    RobaCamas(eosim::core::Model& model);
    ~RobaCamas();
    void eventRoutine(eosim::core::Entity* who);
};
const std::string devuelveCama = "DevuelveCama";

class DevuelveCama: public eosim::core::BEvent {
public:

    DevuelveCama(eosim::core::Model& model);
    ~DevuelveCama();
    void eventRoutine(eosim::core::Entity* who);
};

// identificador del evento fijo SalidaPaciente
const std::string salidaP = "SalidaPaciente";


class SalidaPaciente: public eosim::core::BEvent {
public:
	// constructor
	SalidaPaciente(eosim::core::Model& model);
	// destructor
	~SalidaPaciente();
	// rutina del evento fijo
	void eventRoutine(eosim::core::Entity* who);
};


#endif

