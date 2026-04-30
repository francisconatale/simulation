#ifndef HOSPITAL_SIMPLE_HPP_
#define HOSPITAL_SIMPLE_HPP_

#include <eosim/core/model.hpp>
#include <eosim/utils/entityqueuefifo.hpp>
#include <eosim/core/renewable.hpp>
#include <eosim/dist/negexpdist.hpp>
#include <eosim/statics/timeweighted.hpp>
#include <eosim/dist/normaldist.hpp>
#include <eosim/statics/observation.hpp>
#include "paciente.hpp"

class AutoServicioSimple: public eosim::core::Model {
private:
	// tasa de arribos de los pacientes
	double tasaArribos;

	LlegadaCliente llegadaCliente;
	SalidaCliente salidaCliente;

public:
	eosim::dist::NegexpDist arribos;
	eosim::dist::NormalDist preparacionCafe;
	// cola de espera por camas
	eosim::utils::EntityQueueFifo cola;
	// camas del hospital
	eosim::core::Renewable cafeteras;
	// acumulador de datos sobre los tiempos de espera en las colas
	eosim::statics::Observation tEspera;
	// acumulador de datos sobre el largo medio de la cola
	eosim::statics::TimeWeighted lCola;
	// acumulador de datos para tiempo de uso
	eosim::statics::Observation tUso;
	// constructor del modelo
	AutoServicioSimple(unsigned int cantCafeteras, double tasaArribos);
	// destructor del modelo
	~AutoServicioSimple();
	// inicializa y registra los atributos del modelo, operacion abstracta de eosim::core::Model
	void init();
	// lleva al modelo a su estado inicial, operacion abstracta de eosim::core::Model
	void doInitialSchedules();
};

#endif

