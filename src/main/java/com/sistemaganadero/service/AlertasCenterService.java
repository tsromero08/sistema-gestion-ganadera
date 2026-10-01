package com.sistemaganadero.service;

import com.sistemaganadero.model.*;
import com.sistemaganadero.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centro de alertas unificado. Genera alertas segmentadas en 10 categorías
 * y mantiene 5 estados (NUEVA, VISTA, EN_PROCESO, RESUELTA, IGNORADA).
 * Las alertas son clickeables: cada una conoce su categoría y referencia.
 */
public final class AlertasCenterService {

    private static final ObservableList<AlertaCentro> ALERTAS = FXCollections.observableArrayList();
    private static final AtomicInteger SEQ = new AtomicInteger(1000);

    private AlertasCenterService() {}

    public static ObservableList<AlertaCentro> getTodas() { return ALERTAS; }

    public static ObservableList<AlertaCentro> filtrarPorCategoria(AlertaCentro.Categoria c) {
        ObservableList<AlertaCentro> r = FXCollections.observableArrayList();
        for (AlertaCentro a : ALERTAS) if (a.getCategoria() == c) r.add(a);
        return r;
    }

    public static ObservableList<AlertaCentro> filtrarPorEstado(AlertaCentro.Estado e) {
        ObservableList<AlertaCentro> r = FXCollections.observableArrayList();
        for (AlertaCentro a : ALERTAS) if (a.getEstado() == e) r.add(a);
        return r;
    }

    /** Limpia y regenera todas las alertas del sistema basadas en datos actuales. */
    public static synchronized void regenerar() {
        Set<String> previas = new HashSet<>();
        for (AlertaCentro a : ALERTAS) {
            // conservar estados manuales (VISTA/EN_PROCESO/RESUELTA/IGNORADA) por referencia
            if (a.getEstado() != AlertaCentro.Estado.NUEVA) {
                previas.add(claveDedup(a));
            }
        }
        ALERTAS.clear();

        int fincaId = Session.getFincaActualId();
        if (fincaId <= 0) return;

        agregarAlertasVacunas(fincaId, previas);
        agregarAlertasSanidad(fincaId, previas);
        agregarAlertasStock(fincaId, previas);
        agregarAlertasPotreros(fincaId, previas);
        agregarAlertasAlimentacion(fincaId, previas);
        agregarAlertasMarketplace(previas);
        agregarAlertasVentas(previas);
        agregarAlertasUsuarios(previas);
        agregarAlertaSistema(previas);
    }

    private static String claveDedup(AlertaCentro a) {
        return a.getCategoria().name() + "|" + a.getReferenciaTipo() + "|" + a.getReferenciaId() + "|" + a.getTitulo();
    }

    private static AlertaCentro crear(AlertaCentro.Categoria cat, AlertaCentro.Severidad sev,
                                      String titulo, String descripcion, String refTipo, Integer refId) {
        return new AlertaCentro(SEQ.incrementAndGet(), cat, sev, titulo, descripcion, refTipo, refId);
    }

    private static void agregarAlertasVacunas(int fincaId, Set<String> previas) {
        ObservableList<Vacuna> vacunas = GanaderiaDataStore.getVacunasPorFinca(fincaId);
        for (Vacuna v : vacunas) {
            if (v.estaVencida()) {
                Animal a = GanaderiaDataStore.getAnimalById(v.getAnimalId());
                AlertaCentro al = crear(AlertaCentro.Categoria.VACUNAS, AlertaCentro.Severidad.CRITICA,
                        "Vacuna VENCIDA: " + v.getNombreVacuna() + " — " + v.getArete(),
                        "Animal " + (a != null ? a.getNombre() : v.getArete())
                                + " — próxima dosis era el " + v.getProximaDosis(),
                        "vacuna", v.getId());
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.EN_PROCESO);
                ALERTAS.add(al);
            } else if (v.venceProximamente()) {
                AlertaCentro al = crear(AlertaCentro.Categoria.VACUNAS, AlertaCentro.Severidad.ALTA,
                        "Vacuna próxima: " + v.getNombreVacuna() + " — " + v.getArete(),
                        "Aplicar en " + v.getDiasRestantes() + " días (" + v.getProximaDosis() + ")",
                        "vacuna", v.getId());
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.VISTA);
                ALERTAS.add(al);
            }
        }
        // Animales sin registro de vacuna
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        for (Animal a : animales) {
            boolean tiene = vacunas.stream().anyMatch(v -> v.getAnimalId() == a.getId());
            if (!tiene) {
                AlertaCentro al = crear(AlertaCentro.Categoria.VACUNAS, AlertaCentro.Severidad.ALTA,
                        "Sin registro de vacunación: " + a.getArete(),
                        a.getNombre() + " (" + a.getRaza() + ") no tiene vacunas registradas",
                        "animal", a.getId());
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.VISTA);
                ALERTAS.add(al);
            }
        }
    }

    private static void agregarAlertasSanidad(int fincaId, Set<String> previas) {
        List<SanidadService.AnimalAlerta> purgas = SanidadService.animalesRequierenPurga(fincaId);
        for (SanidadService.AnimalAlerta al : purgas) {
            AlertaCentro a = crear(AlertaCentro.Categoria.SANIDAD, AlertaCentro.Severidad.ALTA,
                    "Purga pendiente: " + al.animal().getArete(),
                    al.motivo(), "animal", al.animal().getId());
            if (previas.contains(claveDedup(a))) a.setEstado(AlertaCentro.Estado.VISTA);
            ALERTAS.add(a);
        }
        List<SanidadService.AnimalAlerta> vit = SanidadService.animalesRequierenVitaminas(fincaId);
        for (SanidadService.AnimalAlerta al : vit) {
            AlertaCentro a = crear(AlertaCentro.Categoria.SANIDAD, AlertaCentro.Severidad.MEDIA,
                    "Vitaminas pendientes: " + al.animal().getArete(),
                    al.motivo(), "animal", al.animal().getId());
            if (previas.contains(claveDedup(a))) a.setEstado(AlertaCentro.Estado.VISTA);
            ALERTAS.add(a);
        }
        // Animales con bajo rendimiento -> sanidad
        for (Animal a : GanaderiaDataStore.getAnimalesPorFinca(fincaId)) {
            if (a.getGdp() < 0.5) {
                AlertaCentro al = crear(AlertaCentro.Categoria.SANIDAD, AlertaCentro.Severidad.CRITICA,
                        "Bajo rendimiento: " + a.getArete() + " — " + a.getNombre(),
                        String.format("GDP %.2f kg/día. Requiere plan individual y revisión veterinaria.", a.getGdp()),
                        "animal", a.getId());
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.EN_PROCESO);
                ALERTAS.add(al);
            }
        }
    }

    private static void agregarAlertasStock(int fincaId, Set<String> previas) {
        ObservableList<InventarioItem> inv = GanaderiaDataStore.getInventarioPorFinca(fincaId);
        for (InventarioItem i : inv) {
            if (i.getCantidad() <= i.getMinimo()) {
                AlertaCentro al = crear(AlertaCentro.Categoria.STOCK, AlertaCentro.Severidad.ALTA,
                        "Stock bajo: " + i.getNombre(),
                        String.format("Quedan %.0f %s (mínimo %.0f). Reabastecer pronto.",
                                i.getCantidad(), i.getUnidad(), i.getMinimo()),
                        "inventario", null);
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.VISTA);
                ALERTAS.add(al);
            }
            if (i.estaProximoVencer()) {
                AlertaCentro al = crear(AlertaCentro.Categoria.STOCK, AlertaCentro.Severidad.MEDIA,
                        "Próximo a vencer: " + i.getNombre(),
                        "Fecha de vencimiento " + i.getFechaVencimiento(),
                        "inventario", null);
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.VISTA);
                ALERTAS.add(al);
            }
        }
    }

    private static void agregarAlertasPotreros(int fincaId, Set<String> previas) {
        ObservableList<Potrero> potreros = GanaderiaDataStore.getPotrerosPorFinca(fincaId);
        for (Potrero p : potreros) {
            String rec = p.recomendacionRotacion();
            if (rec != null && rec.toUpperCase().contains("MOVER")) {
                AlertaCentro al = crear(AlertaCentro.Categoria.POTREROS, AlertaCentro.Severidad.ALTA,
                        "Rotación pendiente: " + p.getCodigo(),
                        rec, "potrero", p.getId());
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.VISTA);
                ALERTAS.add(al);
            }
            if (p.requiereFertilizacion()) {
                AlertaCentro al = crear(AlertaCentro.Categoria.POTREROS, AlertaCentro.Severidad.MEDIA,
                        "Fertilización requerida: " + p.getNombre(),
                        "Requiere mejora del suelo o fertilización.", "potrero", p.getId());
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.VISTA);
                ALERTAS.add(al);
            }
        }
    }

    private static void agregarAlertasAlimentacion(int fincaId, Set<String> previas) {
        ObservableList<InventarioItem> inv = GanaderiaDataStore.getInventarioPorFinca(fincaId);
        for (InventarioItem i : inv) {
            if ("Alimentos".equalsIgnoreCase(i.getCategoria()) && i.getCantidad() <= i.getMinimo()) {
                AlertaCentro al = crear(AlertaCentro.Categoria.ALIMENTACION, AlertaCentro.Severidad.ALTA,
                        "Alimento bajo: " + i.getNombre(),
                        "Stock de alimento por debajo del mínimo. Revisar dieta.",
                        "inventario", null);
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.VISTA);
                ALERTAS.add(al);
            }
        }
    }

    private static void agregarAlertasMarketplace(Set<String> previas) {
        for (MarketplaceListing m : GanaderiaDataStore.getMarketplace()) {
            if (m.getEstado() == MarketplaceListing.Estado.OFERTA_RECIBIDA
                    || m.getEstado() == MarketplaceListing.Estado.OFERTA_ACEPTABLE) {
                AlertaCentro al = crear(AlertaCentro.Categoria.MARKETPLACE, AlertaCentro.Severidad.MEDIA,
                        "Oferta recibida: " + m.getCodigo(),
                        m.getTituloLote() + " — " + m.getTotalOfertas() + " oferta(s)",
                        "listing", m.getId());
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.VISTA);
                ALERTAS.add(al);
            } else if (m.getEstado() == MarketplaceListing.Estado.NEGOCIANDO) {
                AlertaCentro al = crear(AlertaCentro.Categoria.MARKETPLACE, AlertaCentro.Severidad.MEDIA,
                        "Negociación en curso: " + m.getCodigo(),
                        m.getTituloLote(), "listing", m.getId());
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.EN_PROCESO);
                ALERTAS.add(al);
            } else if (m.getFechaPublicacion() != null
                    && m.getFechaPublicacion().isBefore(LocalDate.now().minusDays(45))
                    && m.getEstado() == MarketplaceListing.Estado.PUBLICADO) {
                AlertaCentro al = crear(AlertaCentro.Categoria.MARKETPLACE, AlertaCentro.Severidad.INFO,
                        "Publicación sin movimiento: " + m.getCodigo(),
                        "Publicada hace más de 45 días sin ofertas. Considerar bajar precio.",
                        "listing", m.getId());
                if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.VISTA);
                ALERTAS.add(al);
            }
        }
    }

    private static void agregarAlertasVentas(Set<String> previas) {
        long pendientes = GanaderiaDataStore.getMarketplace().stream()
                .filter(m -> m.getEstado() == MarketplaceListing.Estado.VENDIDO).count();
        if (pendientes > 0) {
            AlertaCentro al = crear(AlertaCentro.Categoria.VENTAS, AlertaCentro.Severidad.INFO,
                    pendientes + " venta(s) cerradas con comisión por cobrar",
                    "Revise el panel administrativo para gestionar comisiones.",
                    "ventas", null);
            if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.VISTA);
            ALERTAS.add(al);
        }
    }

    private static void agregarAlertasUsuarios(Set<String> previas) {
        long activos = AuthService.getUsuarios().stream().filter(u -> u.isActivo()).count();
        AlertaCentro al = crear(AlertaCentro.Categoria.USUARIOS, AlertaCentro.Severidad.INFO,
                activos + " usuarios activos en el sistema",
                "Gestione usuarios y permisos desde Configuración → Usuarios.",
                "usuarios", null);
        if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.IGNORADA);
        ALERTAS.add(al);
    }

    private static void agregarAlertaSistema(Set<String> previas) {
        AlertaCentro al = crear(AlertaCentro.Categoria.SISTEMA, AlertaCentro.Severidad.INFO,
                "Sistema operativo — versión 2.0",
                "Última sincronización: ahora · Soporte: +57 301 389 6861",
                "sistema", null);
        if (previas.contains(claveDedup(al))) al.setEstado(AlertaCentro.Estado.IGNORADA);
        ALERTAS.add(al);
    }

    // ---- Conteos por categoría ----
    public static long contar(AlertaCentro.Categoria c) {
        return ALERTAS.stream().filter(a -> a.getCategoria() == c
                && a.getEstado() != AlertaCentro.Estado.RESUELTA
                && a.getEstado() != AlertaCentro.Estado.IGNORADA).count();
    }

    public static long contarCriticas() {
        return ALERTAS.stream().filter(a -> a.getSeveridad() == AlertaCentro.Severidad.CRITICA
                && a.getEstado() != AlertaCentro.Estado.RESUELTA
                && a.getEstado() != AlertaCentro.Estado.IGNORADA).count();
    }

    public static long contarTotalActivas() {
        return ALERTAS.stream().filter(a -> a.getEstado() != AlertaCentro.Estado.RESUELTA
                && a.getEstado() != AlertaCentro.Estado.IGNORADA).count();
    }

    public static long contarPorEstado(AlertaCentro.Estado e) {
        return ALERTAS.stream().filter(a -> a.getEstado() == e).count();
    }
}
