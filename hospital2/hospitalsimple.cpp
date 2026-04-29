#include "hospitalsimple.hpp"
#include "constantes.hpp"

using namespace eosim::core;
using namespace eosim::dist;

HospitalSimple::HospitalSimple() : 
    gestor(*this), arribo(*this), sEnf(*this), sCons(*this),
    distEnfermera(MT19937, T_ENF_MIN, T_ENF_MAX),
    distMedico(MT19937, T_MED_MIN, T_MED_MAX),
    distPartera(MT19937, T_MED_MIN, T_MED_MAX),
    distConjunta(MT19937, T_CONJ_MIN, T_CONJ_MAX),
    enfermera(1, 1),
    medico(1, 1),
    partera(1, 1),
    lColaEnfermera("Largo Cola Enfermera", *this),
    lColaMedico("Largo Cola Medico", *this),
    lColaPartera("Largo Cola Partera", *this),
    lColaConjunta("Largo Cola Conjunta", *this),
    tEsperaEnfermera("Espera Enfermera"),
    tEsperaMedico("Espera Medico"),
    tEsperaPartera("Espera Partera"),
    tEsperaConjunta("Espera Conjunta")
{}

HospitalSimple::~HospitalSimple() {}

void HospitalSimple::init() {
    registerBEvent(&gestor);
    registerBEvent(&arribo);
    registerBEvent(&sEnf);
    registerBEvent(&sCons);

    registerDist(&distEnfermera);
    registerDist(&distMedico);
    registerDist(&distPartera);
    registerDist(&distConjunta);
}

void HospitalSimple::doInitialSchedules() {
    schedule(0.0, new Entity(), EV_GESTOR_SEMANAL);
}
