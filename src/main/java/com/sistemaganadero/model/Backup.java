package com.sistemaganadero.model;

import java.time.LocalDateTime;

/**
 * Registro de respaldo de un objeto antes de ser editado.
 * Se guarda en la colección "backups" de MongoDB (o archivo local si MongoDB no está disponible).
 */
public class Backup {
    private String id;
    private String tablaModificada;       // ej: "animales", "vacunas", "marketplace"
    private String registroId;            // id del registro original
    private String datosOriginalesJson;   // snapshot completo en JSON
    private LocalDateTime fechaModificacion;
    private String usuario;               // username del que realizó el cambio

    public Backup() {
        this.fechaModificacion = LocalDateTime.now();
    }

    public Backup(String tabla, String registroId, String datosJson, String usuario) {
        this();
        this.tablaModificada = tabla;
        this.registroId = registroId;
        this.datosOriginalesJson = datosJson;
        this.usuario = usuario;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTablaModificada() { return tablaModificada; }
    public void setTablaModificada(String tablaModificada) { this.tablaModificada = tablaModificada; }
    public String getRegistroId() { return registroId; }
    public void setRegistroId(String registroId) { this.registroId = registroId; }
    public String getDatosOriginalesJson() { return datosOriginalesJson; }
    public void setDatosOriginalesJson(String datosOriginalesJson) { this.datosOriginalesJson = datosOriginalesJson; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
