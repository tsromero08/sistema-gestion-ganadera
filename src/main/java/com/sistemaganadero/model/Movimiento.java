package com.sistemaganadero.model;

import java.time.LocalDate;

public class Movimiento {
    private int id;
    private int animalId;
    private int fincaOrigenId;
    private int fincaDestinoId;
    private int loteOrigenId;
    private int loteDestinoId;
    private LocalDate fecha;
    private String tipoMovimiento;
    private String observaciones;
    private int usuarioId;

    public Movimiento() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAnimalId() { return animalId; }
    public void setAnimalId(int animalId) { this.animalId = animalId; }
    public int getFincaOrigenId() { return fincaOrigenId; }
    public void setFincaOrigenId(int fincaOrigenId) { this.fincaOrigenId = fincaOrigenId; }
    public int getFincaDestinoId() { return fincaDestinoId; }
    public void setFincaDestinoId(int fincaDestinoId) { this.fincaDestinoId = fincaDestinoId; }
    public int getLoteOrigenId() { return loteOrigenId; }
    public void setLoteOrigenId(int loteOrigenId) { this.loteOrigenId = loteOrigenId; }
    public int getLoteDestinoId() { return loteDestinoId; }
    public void setLoteDestinoId(int loteDestinoId) { this.loteDestinoId = loteDestinoId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
}
