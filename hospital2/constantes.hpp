#ifndef CONSTANTES_HPP_
#define CONSTANTES_HPP_

// Tiempos en DIAS
const double DIA = 1.0;
const double SEMANA = 7.0;

// Tasas de arribo de nuevas pacientes (cada 0.1 dias = 10 pacientes por dia)
const double ARRIBO_NUEVAS = 0.1; 

// Tiempos de Servicio (Horas convertidas a dias: Horas / 24)
// Enfermera: 5-10 mins (0.0034 - 0.0069 dias)
const double T_ENF_MIN = 0.0034;
const double T_ENF_MAX = 0.0069;

// Medico/Partera: 15-30 mins (0.0104 - 0.0208 dias)
const double T_MED_MIN = 0.0104;
const double T_MED_MAX = 0.0208;

// Conjunta: 25-45 mins (0.0173 - 0.0312 dias)
const double T_CONJ_MIN = 0.0173;
const double T_CONJ_MAX = 0.0312;

#endif
