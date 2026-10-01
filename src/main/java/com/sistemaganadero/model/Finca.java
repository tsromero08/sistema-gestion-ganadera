package com.sistemaganadero.model;

import java.time.LocalDate;

public class Finca {
    private int id;
    private String nombre;
    private String ubicacion;
    private String propietario;
    private double areHectareas;
    private String descripcion;
    private String estado;
    private LocalDate fechaCreacion;
    private String departamento;
    private String municipio;
    private String vereda;
    private double distanciaKm;

    public Finca() {}

    public Finca(int id, String nombre, String ubicacion, String propietario, double areHectareas, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.propietario = propietario;
        this.areHectareas = areHectareas;
        this.descripcion = descripcion;
        this.estado = "Activa";
        this.fechaCreacion = LocalDate.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getPropietario() { return propietario; }
    public void setPropietario(String propietario) { this.propietario = propietario; }
    public double getAreaHectareas() { return areHectareas; }
    public void setAreaHectareas(double areHectareas) { this.areHectareas = areHectareas; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getVereda() { return vereda; }
    public void setVereda(String vereda) { this.vereda = vereda; }
    public double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(double distanciaKm) { this.distanciaKm = distanciaKm; }

    @Override
    public String toString() { return nombre; }
}
