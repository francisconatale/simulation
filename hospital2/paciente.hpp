#ifndef PACIENTE_HPP_
#define PACIENTE_HPP_

#include <eosim/core/bevent.hpp>
#include <eosim/core/entity.hpp>
#include <string>

enum TipoCita { CITA_MEDICO, CITA_PARTERA, CITA_CONJUNTA };

class Embarazada: public eosim::core::Entity {
public:
    int semanaEmbarazo;
    TipoCita tipoProximaCita;
    bool primeraVisita;

    Embarazada(int semana = 12) : 
        semanaEmbarazo(semana), 
        tipoProximaCita(CITA_MEDICO), 
        primeraVisita(true) {}
};

class PacienteComun: public eosim::core::Entity {
public:
    PacienteComun() {}
};

// Eventos del Sistema
const std::string EV_GESTOR_SEMANAL = "GestorSemanal";
const std::string EV_ARRIBO_PACIENTE = "ArriboPaciente";
const std::string EV_SALIDA_ENFERMERA = "SalidaEnfermera";
const std::string EV_SALIDA_CONSULTA = "SalidaConsulta";

class GestorSemanal: public eosim::core::BEvent {
public:
	GestorSemanal(eosim::core::Model& model);
	void eventRoutine(eosim::core::Entity* who);
};

class ArriboPaciente: public eosim::core::BEvent {
public:
	ArriboPaciente(eosim::core::Model& model);
	void eventRoutine(eosim::core::Entity* who);
};

class SalidaEnfermera: public eosim::core::BEvent {
public:
	SalidaEnfermera(eosim::core::Model& model);
	void eventRoutine(eosim::core::Entity* who);
};

class SalidaConsulta: public eosim::core::BEvent {
public:
	SalidaConsulta(eosim::core::Model& model);
	void eventRoutine(eosim::core::Entity* who);
};

#endif
