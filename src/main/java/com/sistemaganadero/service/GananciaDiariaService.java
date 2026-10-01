package com.sistemaganadero.service;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.Pesaje;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

public class GananciaDiariaService {

    private GananciaDiariaService() {}

    /**
     * Calcula la ganancia diaria de peso a partir del historial de pesajes.
     * Usa el primer y último pesaje. Si solo hay uno, usa peso inicial del animal.
     */
    public static double calcularGdp(Animal animal, List<Pesaje> historialAnimal) {
        if (animal == null) return 0.0;
        if (historialAnimal == null || historialAnimal.size() < 2) {
            // Fallback: usar peso inicial vs actual con días en sistema
            int dias = animal.getDiasEnSistema();
            if (dias <= 0) return 0.0;
            return Math.max(0, (animal.getPesoActual() - animal.getPesoInicial()) / dias);
        }
        List<Pesaje> ordenados = historialAnimal.stream()
                .sorted(Comparator.comparing(Pesaje::getFecha))
                .toList();
        Pesaje primero = ordenados.get(0);
        Pesaje ultimo = ordenados.get(ordenados.size() - 1);
        long dias = ChronoUnit.DAYS.between(primero.getFecha(), ultimo.getFecha());
        if (dias <= 0) return 0.0;
        return Math.max(0, (ultimo.getPeso() - primero.getPeso()) / (double) dias);
    }

    public static double calcularGdpRecientes(List<Pesaje> historialAnimal, int diasAtras) {
        if (historialAnimal == null || historialAnimal.size() < 2) return 0.0;
        LocalDate corte = LocalDate.now().minusDays(diasAtras);
        List<Pesaje> recientes = historialAnimal.stream()
                .filter(p -> p.getFecha() != null && !p.getFecha().isBefore(corte))
                .sorted(Comparator.comparing(Pesaje::getFecha))
                .toList();
        if (recientes.size() < 2) return 0.0;
        Pesaje primero = recientes.get(0);
        Pesaje ultimo = recientes.get(recientes.size() - 1);
        long dias = ChronoUnit.DAYS.between(primero.getFecha(), ultimo.getFecha());
        if (dias <= 0) return 0.0;
        return Math.max(0, (ultimo.getPeso() - primero.getPeso()) / (double) dias);
    }

    /** Recalcula y persiste el GDP para todos los animales de una finca. */
    public static void recalcularTodos(int fincaId) {
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        ObservableList<Pesaje> pesajes = GanaderiaDataStore.getPesajesPorFinca(fincaId);
        for (Animal a : animales) {
            List<Pesaje> hist = pesajes.stream().filter(p -> p.getAnimalId() == a.getId()).toList();
            double gdp = calcularGdp(a, hist);
            a.setGdp(gdp);
        }
    }

    /** Detecta si un animal lleva mucho tiempo sin pesarse. */
    public static boolean requierePesaje(int animalId, List<Pesaje> historialAnimal) {
        if (historialAnimal == null || historialAnimal.isEmpty()) return true;
        LocalDate ultima = historialAnimal.stream()
                .filter(p -> p.getAnimalId() == animalId)
                .map(Pesaje::getFecha)
                .filter(f -> f != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
        if (ultima == null) return true;
        return ultima.isBefore(LocalDate.now().minusDays(60));
    }
}
