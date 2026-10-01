package com.sistemaganadero.model;

import java.time.LocalDateTime;

public class Auditoria {
    private int id;
    private int usuarioId;
    private String accion;
    private String modulo;
    private String entidad;
    private int entidadId;
    private String descripcion;
    private LocalDateTime fecha;

    public Auditoria() {}

    public Auditoria(int usuarioId, String accion, String modulo, String entidad, int entidadId, String descripcion) {
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.modulo = modulo;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.descripcion = descripcion;
        this.fecha = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public int getEntidadId() { return entidadId; }
    public void setEntidadId(int entidadId) { this.entidadId = entidadId; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
