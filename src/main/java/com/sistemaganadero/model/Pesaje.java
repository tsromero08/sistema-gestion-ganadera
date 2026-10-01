package com.sistemaganadero.model;

import java.time.LocalDate;

public class Pesaje {
    private int id;
    private int animalId;
    private int fincaId;
    private String arete;
    private LocalDate fecha;
    private double peso;
    private String observaciones;
    private int usuarioId;

    public Pesaje() {}

    public Pesaje(int id, int animalId, int fincaId, String arete, LocalDate fecha, double peso, String observaciones) {
        this.id = id;
        this.animalId = animalId;
        this.fincaId = fincaId;
        this.arete = arete;
        this.fecha = fecha;
        this.peso = peso;
        this.observaciones = observaciones;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAnimalId() { return animalId; }
    public void setAnimalId(int animalId) { this.animalId = animalId; }
    public int getFincaId() { return fincaId; }
    public void setFincaId(int fincaId) { this.fincaId = fincaId; }
    public String getArete() { return arete; }
    public void setArete(String arete) { this.arete = arete; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
}
