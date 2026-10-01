package com.sistemaganadero.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notificacion {

    public enum Tipo { CAMPANA, TOAST, MODAL, PUSH, EMAIL, SMS, WHATSAPP }

    private int id;
    private Tipo tipo;
    private String categoria;     // misma taxonomía que AlertaCentro
    private String asunto;
    private String mensaje;
    private Integer alertaId;
    private boolean leida;
    private LocalDateTime fecha;

    public Notificacion() {
        this.fecha = LocalDateTime.now();
        this.tipo = Tipo.CAMPANA;
    }

    public Notificacion(int id, Tipo tipo, String categoria, String asunto, String mensaje, Integer alertaId) {
        this();
        this.id = id;
        this.tipo = tipo;
        this.categoria = categoria;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.alertaId = alertaId;
    }

    public String getFechaFormateada() {
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public Integer getAlertaId() { return alertaId; }
    public void setAlertaId(Integer alertaId) { this.alertaId = alertaId; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
