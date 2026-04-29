#ifndef HOSPITAL_SIMPLE_HPP_
#define HOSPITAL_SIMPLE_HPP_

#include <eosim/core/model.hpp>
#include <eosim/utils/entityqueuefifo.hpp>
#include <eosim/core/renewable.hpp>
#include <eosim/statics/observation.hpp>
#include <eosim/statics/timeweighted.hpp>
#include <eosim/dist/uniformdist.hpp>
#include "paciente.hpp"

class HospitalSimple: public eosim::core::Model {
private:
    GestorSemanal gestor;
    ArriboPaciente arribo;
    SalidaEnfermera sEnf;
    SalidaConsulta sCons;

public:
    // Distribuciones
    eosim::dist::UniformDist distEnfermera;
    eosim::dist::UniformDist distMedico;
    eosim::dist::UniformDist distPartera;
    eosim::dist::UniformDist distConjunta;

    // Recursos
    eosim::core::Renewable enfermera;
    eosim::core::Renewable medico;
    eosim::core::Renewable partera;

    // Colas
    eosim::utils::EntityQueueFifo colaEnfermera;
    eosim::utils::EntityQueueFifo colaMedico;
    eosim::utils::EntityQueueFifo colaPartera;
    eosim::utils::EntityQueueFifo colaConjunta;

    // Estadisticas de Largo de Cola
    eosim::statics::TimeWeighted lColaEnfermera;
    eosim::statics::TimeWeighted lColaMedico;
    eosim::statics::TimeWeighted lColaPartera;
    eosim::statics::TimeWeighted lColaConjunta;

    // Estadisticas de Tiempos de Espera
    eosim::statics::Observation tEsperaEnfermera;
    eosim::statics::Observation tEsperaMedico;
    eosim::statics::Observation tEsperaPartera;
    eosim::statics::Observation tEsperaConjunta;

    HospitalSimple();
    ~HospitalSimple();
    
    void init();
    void doInitialSchedules();
};

#endif
