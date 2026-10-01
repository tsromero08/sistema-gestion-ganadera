package com.sistemaganadero.model;

import java.time.LocalDateTime;

public class Puja {

    public enum Estado {
        PENDIENTE,        // recién creada
        OFERTA_BAJA,      // por debajo del precio mínimo
        ACEPTABLE,        // dentro de rango aceptable
        EN_NEGOCIACION,
        ACEPTADA,
        RECHAZADA,
        CONTRA_OFERTA,
        EXPIRADA
    }

    private int id;
    private int listingId;
    private String listingCodigo;
    private String comprador;
    private String compradorContacto;
    private double valorOferta;
    private double contraOferta;
    private Estado estado;
    private LocalDateTime fecha;
    private String observaciones;

    public Puja() {
        this.estado = Estado.PENDIENTE;
        this.fecha = LocalDateTime.now();
    }

    public Puja(int id, int listingId, String listingCodigo, String comprador,
                String compradorContacto, double valorOferta) {
        this();
        this.id = id;
        this.listingId = listingId;
        this.listingCodigo = listingCodigo;
        this.comprador = comprador;
        this.compradorContacto = compradorContacto;
        this.valorOferta = valorOferta;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getListingId() { return listingId; }
    public void setListingId(int listingId) { this.listingId = listingId; }
    public String getListingCodigo() { return listingCodigo; }
    public void setListingCodigo(String listingCodigo) { this.listingCodigo = listingCodigo; }
    public String getComprador() { return comprador; }
    public void setComprador(String comprador) { this.comprador = comprador; }
    public String getCompradorContacto() { return compradorContacto; }
    public void setCompradorContacto(String compradorContacto) { this.compradorContacto = compradorContacto; }
    public double getValorOferta() { return valorOferta; }
    public void setValorOferta(double valorOferta) { this.valorOferta = valorOferta; }
    public double getContraOferta() { return contraOferta; }
    public void setContraOferta(double contraOferta) { this.contraOferta = contraOferta; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
