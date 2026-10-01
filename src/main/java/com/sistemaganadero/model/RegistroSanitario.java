package com.sistemaganadero.model;

import java.time.LocalDate;

public class RegistroSanitario {
    public enum Tipo { TRATAMIENTO, PURGA, VITAMINA, REVISION }

    private int id;
    private int animalId;
    private int fincaId;
    private String arete;
    private String animalNombre;
    private Tipo tipo;
    private String descripcion;
    private LocalDate fecha;
    private LocalDate proximaAplicacion;
    private String responsable;
    private double costo;
    private String observaciones;

    public RegistroSanitario() {}

    public RegistroSanitario(int id, int animalId, int fincaId, String arete, String animalNombre,
                              Tipo tipo, String descripcion, LocalDate fecha, LocalDate proximaAplicacion,
                              String responsable, double costo, String observaciones) {
        this.id = id;
        this.animalId = animalId;
        this.fincaId = fincaId;
        this.arete = arete;
        this.animalNombre = animalNombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.proximaAplicacion = proximaAplicacion;
        this.responsable = responsable;
        this.costo = costo;
        this.observaciones = observaciones;
    }

    public boolean esVencido() {
        return proximaAplicacion != null && proximaAplicacion.isBefore(LocalDate.now());
    }

    public boolean estaProximo() {
        if (proximaAplicacion == null) return false;
        return !proximaAplicacion.isAfter(LocalDate.now().plusDays(15));
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAnimalId() { return animalId; }
    public void setAnimalId(int animalId) { this.animalId = animalId; }
    public int getFincaId() { return fincaId; }
    public void setFincaId(int fincaId) { this.fincaId = fincaId; }
    public String getArete() { return arete; }
    public void setArete(String arete) { this.arete = arete; }
    public String getAnimalNombre() { return animalNombre; }
    public void setAnimalNombre(String animalNombre) { this.animalNombre = animalNombre; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalDate getProximaAplicacion() { return proximaAplicacion; }
    public void setProximaAplicacion(LocalDate proximaAplicacion) { this.proximaAplicacion = proximaAplicacion; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
