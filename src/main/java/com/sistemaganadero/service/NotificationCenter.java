package com.sistemaganadero.service;

import com.sistemaganadero.model.AlertaCentro;
import com.sistemaganadero.model.Notificacion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centro de notificaciones tipo campana. Espeja las alertas activas
 * generando una notificación por cada una.
 */
public final class NotificationCenter {

    private static final ObservableList<Notificacion> NOTIS = FXCollections.observableArrayList();
    private static final AtomicInteger SEQ = new AtomicInteger(2000);

    private NotificationCenter() {}

    public static synchronized void regenerar() {
        // Conserva notis ya leídas en memoria, y agrega nuevas para alertas no representadas
        for (AlertaCentro a : AlertasCenterService.getTodas()) {
            if (a.getEstado() == AlertaCentro.Estado.RESUELTA
                    || a.getEstado() == AlertaCentro.Estado.IGNORADA) continue;
            int alertaId = a.getId();
            boolean yaExiste = NOTIS.stream()
                    .anyMatch(n -> n.getAlertaId() != null && n.getAlertaId() == alertaId);
            if (yaExiste) continue;
            Notificacion n = new Notificacion(SEQ.incrementAndGet(),
                    Notificacion.Tipo.CAMPANA,
                    a.getCategoria().name(),
                    a.getTitulo(),
                    a.getDescripcion(),
                    alertaId);
            NOTIS.add(0, n);
        }
        // Limitar a 200 notificaciones más recientes
        while (NOTIS.size() > 200) NOTIS.remove(NOTIS.size() - 1);
    }

    public static long contarNoLeidas() {
        return NOTIS.stream().filter(n -> !n.isLeida()).count();
    }

    public static ObservableList<Notificacion> getRecientes(int max) {
        ObservableList<Notificacion> result = FXCollections.observableArrayList();
        int limite = Math.min(max, NOTIS.size());
        for (int i = 0; i < limite; i++) result.add(NOTIS.get(i));
        return result;
    }

    public static void marcarTodasLeidas() {
        for (Notificacion n : NOTIS) n.setLeida(true);
    }

    public static void agregar(Notificacion n) {
        NOTIS.add(0, n);
    }
}
