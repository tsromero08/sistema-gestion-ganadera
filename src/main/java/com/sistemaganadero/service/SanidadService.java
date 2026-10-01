package com.sistemaganadero.service;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.RegistroSanitario;
import com.sistemaganadero.model.Vacuna;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SanidadService {

    private SanidadService() {}

    /** Animales que requieren purga (cada 4 meses). */
    public static List<AnimalAlerta> animalesRequierenPurga(int fincaId) {
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        ObservableList<RegistroSanitario> registros = GanaderiaDataStore.getRegistrosSanitariosPorFinca(fincaId);
        List<AnimalAlerta> resultado = new ArrayList<>();
        for (Animal a : animales) {
            LocalDate ultima = registros.stream()
                    .filter(r -> r.getAnimalId() == a.getId() && r.getTipo() == RegistroSanitario.Tipo.PURGA)
                    .map(RegistroSanitario::getFecha)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            if (ultima == null || ChronoUnit.MONTHS.between(ultima, LocalDate.now()) >= 4) {
                String motivo = ultima == null ? "Sin registro de purga"
                        : "Última purga hace " + ChronoUnit.MONTHS.between(ultima, LocalDate.now()) + " meses";
                resultado.add(new AnimalAlerta(a, "Purga", motivo));
            }
        }
        return resultado;
    }

    /** Animales que requieren vitaminas (cada 3 meses). */
    public static List<AnimalAlerta> animalesRequierenVitaminas(int fincaId) {
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        ObservableList<RegistroSanitario> registros = GanaderiaDataStore.getRegistrosSanitariosPorFinca(fincaId);
        List<AnimalAlerta> resultado = new ArrayList<>();
        for (Animal a : animales) {
            LocalDate ultima = registros.stream()
                    .filter(r -> r.getAnimalId() == a.getId() && r.getTipo() == RegistroSanitario.Tipo.VITAMINA)
                    .map(RegistroSanitario::getFecha)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            if (ultima == null || ChronoUnit.MONTHS.between(ultima, LocalDate.now()) >= 3) {
                String motivo = ultima == null ? "Sin registro de vitaminas"
                        : "Últimas vitaminas hace " + ChronoUnit.MONTHS.between(ultima, LocalDate.now()) + " meses";
                resultado.add(new AnimalAlerta(a, "Vitaminas", motivo));
            }
        }
        return resultado;
    }

    /** Animales con vacunas vencidas o próximas (lista detallada con animal específico). */
    public static List<AlertaVacuna> vacunasPendientes(int fincaId) {
        ObservableList<Vacuna> vacunas = GanaderiaDataStore.getVacunasPorFinca(fincaId);
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        Map<Integer, Animal> indexAnim = new HashMap<>();
        animales.forEach(a -> indexAnim.put(a.getId(), a));

        List<AlertaVacuna> alertas = new ArrayList<>();
        for (Vacuna v : vacunas) {
            if (v.getProximaDosis() == null) continue;
            if (v.getProximaDosis().isAfter(LocalDate.now().plusDays(30))) continue;
            Animal a = indexAnim.get(v.getAnimalId());
            if (a == null) continue;
            String estado = v.getProximaDosis().isBefore(LocalDate.now()) ? "VENCIDA" : "Próxima dosis";
            alertas.add(new AlertaVacuna(a, v, estado));
        }
        return alertas;
    }

    /** Plan individual cuando un animal tiene bajo rendimiento (GDP < 0.5). */
    public static String generarPlanIndividualBajoRendimiento(Animal a) {
        if (a == null) return "";
        if (a.getGdp() >= 0.5) return "El animal no requiere plan individual — GDP estable.";
        StringBuilder plan = new StringBuilder();
        plan.append("PLAN INDIVIDUAL — Bajo Rendimiento\n");
        plan.append("=================================\n");
        plan.append("Animal: ").append(a.getArete()).append(" — ").append(a.getNombre()).append("\n");
        plan.append("GDP actual: ").append(String.format("%.2f kg/día", a.getGdp())).append("\n\n");

        plan.append("MEDICINA:\n");
        plan.append(" • Desparasitación: Ivermectina 1% inyectable (1 ml/50 kg).\n");
        plan.append(" • Vitaminas: Complejo B + ADE (5 ml subcutáneo) — repetir cada 30 días.\n");
        plan.append(" • Examen coprológico — descartar parásitos resistentes.\n\n");

        plan.append("DIETA:\n");
        plan.append(" • Aumentar concentrado al 1.2% del peso vivo (").append(String.format("%.1f kg/día", a.getPesoActual() * 0.012)).append(").\n");
        plan.append(" • Suplementar con bloque multinutricional libre acceso.\n");
        plan.append(" • Sales mineralizadas con fósforo y zinc.\n\n");

        plan.append("SEGUIMIENTO:\n");
        plan.append(" • Pesar cada 15 días para monitorear recuperación.\n");
        plan.append(" • Revisar mucosas, pelo y heces semanalmente.\n");
        plan.append(" • Reevaluar plan a los 60 días — si persiste GDP < 0.5, considerar descarte.\n");
        return plan.toString();
    }

    public record AnimalAlerta(Animal animal, String tipo, String motivo) {}

    public record AlertaVacuna(Animal animal, Vacuna vacuna, String estado) {}
}
