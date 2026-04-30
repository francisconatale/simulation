#include "paciente.hpp"
#include "hospitalsimple.hpp"
#include "constantes.hpp"
#include <iostream>

using namespace eosim::core;

GestorSemanal::GestorSemanal(Model& model) : BEvent(EV_GESTOR_SEMANAL, model) {}

void GestorSemanal::eventRoutine(Entity* who) {
    HospitalSimple& h = dynamic_cast<HospitalSimple&>(owner);

    Embarazada* e = new Embarazada(12);
    h.schedule(0.0, e, EV_ARRIBO_PACIENTE);

    h.schedule(ARRIBO_NUEVAS, who, EV_GESTOR_SEMANAL);
}

ArriboPaciente::ArriboPaciente(Model& model) : BEvent(EV_ARRIBO_PACIENTE, model) {}

void ArriboPaciente::eventRoutine(Entity* who) {
    HospitalSimple& h = dynamic_cast<HospitalSimple&>(owner);

    if (h.enfermera.isAvailable(1)) {
        h.enfermera.acquire(1);
        h.tEsperaEnfermera.log(0.0);
        h.schedule(h.distEnfermera.sample(), who, EV_SALIDA_ENFERMERA);
    } else {
        h.lColaEnfermera.log(h.colaEnfermera.size());
        who->setClock(h.getSimTime());
        h.colaEnfermera.push(who);
    }
}

SalidaEnfermera::SalidaEnfermera(Model& model) : BEvent(EV_SALIDA_ENFERMERA, model) {}

void SalidaEnfermera::eventRoutine(Entity* who){
    HospitalSimple& h = dynamic_cast<HospitalSimple&>(owner);
    h.enfermera.returnBin(1);

    if (!h.colaEnfermera.empty()) {
        Entity* sig = h.colaEnfermera.pop();
        h.enfermera.acquire(1);
        h.tEsperaEnfermera.log(h.getSimTime() - sig->getClock());
        h.schedule(h.distEnfermera.sample(), sig, EV_SALIDA_ENFERMERA);
    }

    Embarazada* e = dynamic_cast<Embarazada*>(who);

    if (e->tipoProximaCita == CITA_MEDICO) {
        if (h.medico.isAvailable(1)) {
            h.medico.acquire(1);
            h.tEsperaMedico.log(0.0);
            h.schedule(h.distMedico.sample(), who, EV_SALIDA_CONSULTA);
        } else {
            h.lColaMedico.log(h.colaMedico.size());
            who->setClock(h.getSimTime());
            h.colaMedico.push(who);
        }
    } else if (e->tipoProximaCita == CITA_PARTERA) {
        if (h.partera.isAvailable(1)) {
            h.partera.acquire(1);
            h.tEsperaPartera.log(0.0);
            h.schedule(h.distPartera.sample(), who, EV_SALIDA_CONSULTA);
        } else {
            h.lColaPartera.log(h.colaPartera.size());
            who->setClock(h.getSimTime());
            h.colaPartera.push(who);
        }
    } else {
        if (h.medico.isAvailable(1) && h.partera.isAvailable(1)) {
            h.medico.acquire(1);
            h.partera.acquire(1);
            h.tEsperaConjunta.log(0.0);
            h.schedule(h.distConjunta.sample(), who, EV_SALIDA_CONSULTA);
        } else {
            h.lColaConjunta.log(h.colaConjunta.size());
            who->setClock(h.getSimTime());
            h.colaConjunta.push(who);
        }
    }
}

SalidaConsulta::SalidaConsulta(Model& model) : BEvent(EV_SALIDA_CONSULTA, model) {}

void SalidaConsulta::eventRoutine(Entity* who) {
    HospitalSimple& h = dynamic_cast<HospitalSimple&>(owner);
    Embarazada* e = dynamic_cast<Embarazada*>(who);

    if (e->tipoProximaCita == CITA_MEDICO) {
        h.medico.returnBin(1);
        if (!h.colaMedico.empty()) {
            Entity* sig = h.colaMedico.pop();
            h.medico.acquire(1);
            h.tEsperaMedico.log(h.getSimTime() - sig->getClock());
            h.schedule(h.distMedico.sample(), sig, EV_SALIDA_CONSULTA);
        }
    } else if (e->tipoProximaCita == CITA_PARTERA) {
        h.partera.returnBin(1);
        if (!h.colaPartera.empty()) {
            Entity* sig = h.colaPartera.pop();
            h.partera.acquire(1);
            h.tEsperaPartera.log(h.getSimTime() - sig->getClock());
            h.schedule(h.distPartera.sample(), sig, EV_SALIDA_CONSULTA);
        }
    } else {
        h.medico.returnBin(1);
        h.partera.returnBin(1);
        if (!h.colaConjunta.empty()) {
            Entity* sig = h.colaConjunta.pop();
            h.medico.acquire(1);
            h.partera.acquire(1);
            h.tEsperaConjunta.log(h.getSimTime() - sig->getClock());
            h.schedule(h.distConjunta.sample(), sig, EV_SALIDA_CONSULTA);
        }
    }

    if (e->semanaEmbarazo < 35) {
        e->semanaEmbarazo += 4;
        e->tipoProximaCita = (e->tipoProximaCita == CITA_MEDICO) ? CITA_PARTERA : CITA_MEDICO;
    } else {
        e->semanaEmbarazo += 1;
        e->tipoProximaCita = CITA_CONJUNTA;
    }

    if (e->semanaEmbarazo >= 40) {
        delete who;
    } else {
        double espera = (e->semanaEmbarazo <= 35) ? (4.0 * SEMANA) : (1.0 * SEMANA);
        h.schedule(espera, who, EV_ARRIBO_PACIENTE);
    }
}
