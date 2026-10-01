package com.sistemaganadero.util;

import com.sistemaganadero.model.Finca;
import com.sistemaganadero.model.Usuario;

import java.time.Duration;
import java.time.LocalDateTime;

public class Session {
    private static Usuario usuarioActual;
    private static Finca fincaActual;
    private static int fincaActualId = 1;
    private static LocalDateTime ultimaActividad;
    private static final Duration TIMEOUT_INACTIVIDAD = Duration.ofMinutes(30);

    public static Usuario getUsuarioActual() { return usuarioActual; }
    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
        registrarActividad();
    }

    public static Finca getFincaActual() { return fincaActual; }
    public static void setFincaActual(Finca finca) {
        fincaActual = finca;
        fincaActualId = finca != null ? finca.getId() : 1;
    }

    public static int getFincaActualId() { return fincaActualId; }

    public static String getNombreFincaActual() {
        return fincaActual != null ? fincaActual.getNombre() : "La Esperanza";
    }

    /** Registra una interacción del usuario para extender la sesión. */
    public static void registrarActividad() {
        ultimaActividad = LocalDateTime.now();
    }

    /** True si la sesión está activa y no ha expirado por inactividad. */
    public static boolean sesionVigente() {
        if (usuarioActual == null || ultimaActividad == null) return false;
        return Duration.between(ultimaActividad, LocalDateTime.now()).compareTo(TIMEOUT_INACTIVIDAD) < 0;
    }

    public static Duration tiempoRestanteSesion() {
        if (ultimaActividad == null) return Duration.ZERO;
        Duration trans = Duration.between(ultimaActividad, LocalDateTime.now());
        return trans.compareTo(TIMEOUT_INACTIVIDAD) >= 0 ? Duration.ZERO : TIMEOUT_INACTIVIDAD.minus(trans);
    }
}
