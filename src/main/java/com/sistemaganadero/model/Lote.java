package com.sistemaganadero.model;

import java.time.LocalDate;

public class Lote {
    private int id;
    private int fincaId;
    private String nombre;
    private int capacidad;
    private String descripcion;
    private String estado;
    private LocalDate fechaCreacion;

    public Lote() {}

    public Lote(int id, int fincaId, String nombre, int capacidad, String descripcion) {
        this.id = id;
        this.fincaId = fincaId;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.descripcion = descripcion;
        this.estado = "Activo";
        this.fechaCreacion = LocalDate.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFincaId() { return fincaId; }
    public void setFincaId(int fincaId) { this.fincaId = fincaId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() { return nombre; }
}
