package com.sistemaganadero.service;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.Potrero;
import com.sistemaganadero.model.RecomendacionDieta;

public class DietaService {

    private DietaService() {}

    public static RecomendacionDieta recomendarParaAnimal(Animal animal, Potrero potrero, String objetivo) {
        if (animal == null) {
            return new RecomendacionDieta(
                    "Pasto disponible", 25, "Sal mineralizada", 0.05,
                    "Sal mineralizada bovina", "Libre disposición — 60 L/día",
                    1, "Datos insuficientes — recomendación general.", objetivo);
        }

        double pesoActual = animal.getPesoActual();
        double consumoMs = pesoActual * 0.025;
        double cantForraje = consumoMs * 4.0;

        String forrajeBase;
        if (potrero != null && potrero.getTipoPasto() != null && !potrero.getTipoPasto().isBlank()) {
            forrajeBase = potrero.getTipoPasto();
        } else {
            forrajeBase = "Pasto kikuyo / brachiaria mejorado";
        }

        double cantSuplemento;
        String suplemento;
        String objetivoNorm = objetivo == null ? "engorde" : objetivo.toLowerCase();

        if (objetivoNorm.contains("ceb") || objetivoNorm.contains("engor")) {
            cantSuplemento = pesoActual * 0.012;
            suplemento = "Concentrado de engorde 16% PC + melaza";
        } else if (objetivoNorm.contains("lech")) {
            cantSuplemento = pesoActual * 0.015;
            suplemento = "Concentrado lechero 18% PC";
        } else if (objetivoNorm.contains("crec") || objetivoNorm.contains("level")) {
            cantSuplemento = pesoActual * 0.010;
            suplemento = "Concentrado de levante 14% PC";
        } else if (objetivoNorm.contains("manteni")) {
            cantSuplemento = pesoActual * 0.005;
            suplemento = "Bloque multinutricional";
        } else {
            cantSuplemento = pesoActual * 0.008;
            suplemento = "Concentrado balanceado 14% PC";
        }

        if (animal.getEdadMeses() < 12) {
            cantSuplemento *= 1.15;
            suplemento += " (etapa joven — más PC)";
        }

        StringBuilder obs = new StringBuilder();
        if ("Hembra".equalsIgnoreCase(animal.getSexo())) obs.append("Hembra: ajustar +10% si está en última gestación. ");
        if (animal.getGdp() < 0.5) obs.append("GDP bajo: revisar parásitos y subir PC del concentrado. ");
        if (potrero != null) {
            if (potrero.getAlturaPasto() > 0 && potrero.getAlturaPasto() < 15)
                obs.append("Pasto bajo: aumentar suplementación temporal. ");
            if (potrero.getPhSuelo() > 0 && potrero.getPhSuelo() < 5.5)
                obs.append("Suelo ácido: complementar con calcio mineral. ");
            if ("Caliente".equalsIgnoreCase(potrero.getClima()))
                obs.append("Clima caliente: priorizar agua fresca y sombra. ");
        }
        if (obs.length() == 0) obs.append("Dieta balanceada según peso, raza y estado.");

        String sales = "Sal mineralizada bovina con fósforo (50-80 g/día)";
        String agua = "Libre disposición — 50 a 80 L/día por animal";
        int frecuencia = objetivoNorm.contains("lech") ? 3 : 2;

        return new RecomendacionDieta(forrajeBase, cantForraje, suplemento, cantSuplemento,
                sales, agua, frecuencia, obs.toString().trim(),
                objetivo == null ? "Mantenimiento + crecimiento" : objetivo);
    }
}
