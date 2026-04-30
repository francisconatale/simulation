#ifndef PACIENTE_HPP_
#define PACIENTE_HPP_

#include <eosim/core/bevent.hpp>
#include <eosim/core/entity.hpp>
#include <string>

// identificador del evento fijo PacienteFeeder
const std::string llegadaCliente = "LlegadaCliente";


class LlegadaCliente: public eosim::core::BEvent {
public:
	// constructor
	LlegadaCliente(eosim::core::Model& model);
	// destructor
	~LlegadaCliente();
	// rutina del evento fijo
	void eventRoutine(eosim::core::Entity* who);
};

const std::string salidaCliente = "SalidaCliente";

class SalidaCliente: public eosim::core::BEvent {
public:

    SalidaCliente(eosim::core::Model& model);
    ~SalidaCliente();
    void eventRoutine(eosim::core::Entity* who);
};

#endif

