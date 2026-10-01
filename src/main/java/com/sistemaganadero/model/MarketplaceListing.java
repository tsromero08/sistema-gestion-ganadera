package com.sistemaganadero.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MarketplaceListing {
    public enum Tipo { INDIVIDUAL, LOTE }
    public enum Estado {
        PUBLICADO,
        RECIBIENDO_OFERTAS,
        OFERTA_RECIBIDA,
        OFERTA_BAJA,
        OFERTA_ACEPTABLE,
        NEGOCIANDO,
        RESERVADO,
        VENDIDO,
        CANCELADO,
        RETIRADO
    }

    private int id;
    private String codigo;
    private Tipo tipo;
    private Estado estado;
    private int fincaId;
    private String fincaNombre;
    private String ubicacion;
    private String departamento;
    private String ciudad;
    private double distanciaKm;
    private List<Integer> animalIds = new ArrayList<>();
    private List<String> aretes = new ArrayList<>();
    private String tituloLote;
    private String resumen;

    // Precio público (deseado) — visible para todos
    private double precio;
    // Precio mínimo aceptable — privado, sólo admin / vendedor / sistema
    private double precioMinimo;

    private double comisionPorcentaje;
    private String vendedor;
    private String contacto;
    private LocalDate fechaPublicacion;
    private String observaciones;

    // Datos productivos resumidos
    private String raza;
    private String sexo;
    private int edadMeses;
    private double pesoTotal;
    private String tipoProduccion;
    private String estadoSanitario;
    private boolean vacunasAlDia;
    private boolean conRegistro;
    private int totalVistas;
    private int totalOfertas;

    public MarketplaceListing() {
        this.estado = Estado.PUBLICADO;
        this.fechaPublicacion = LocalDate.now();
        this.comisionPorcentaje = 3.0;
        this.vacunasAlDia = true;
    }

    public double getComisionValor() {
        return precio * (comisionPorcentaje / 100.0);
    }

    public double getNetoVendedor() {
        return precio - getComisionValor();
    }

    /** Evalúa una oferta contra los precios privados. */
    public Estado evaluarOferta(double oferta) {
        if (oferta <= 0) return Estado.OFERTA_BAJA;
        if (oferta >= precio) return Estado.OFERTA_ACEPTABLE;
        if (precioMinimo > 0 && oferta >= precioMinimo) return Estado.OFERTA_ACEPTABLE;
        return Estado.OFERTA_BAJA;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public int getFincaId() { return fincaId; }
    public void setFincaId(int fincaId) { this.fincaId = fincaId; }
    public String getFincaNombre() { return fincaNombre; }
    public void setFincaNombre(String fincaNombre) { this.fincaNombre = fincaNombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(double distanciaKm) { this.distanciaKm = distanciaKm; }
    public List<Integer> getAnimalIds() { return animalIds; }
    public void setAnimalIds(List<Integer> animalIds) { this.animalIds = animalIds; }
    public List<String> getAretes() { return aretes; }
    public void setAretes(List<String> aretes) { this.aretes = aretes; }
    public String getTituloLote() { return tituloLote; }
    public void setTituloLote(String tituloLote) { this.tituloLote = tituloLote; }
    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public double getPrecioMinimo() { return precioMinimo; }
    public void setPrecioMinimo(double precioMinimo) { this.precioMinimo = precioMinimo; }
    public double getComisionPorcentaje() { return comisionPorcentaje; }
    public void setComisionPorcentaje(double comisionPorcentaje) { this.comisionPorcentaje = comisionPorcentaje; }
    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public int getEdadMeses() { return edadMeses; }
    public void setEdadMeses(int edadMeses) { this.edadMeses = edadMeses; }
    public double getPesoTotal() { return pesoTotal; }
    public void setPesoTotal(double pesoTotal) { this.pesoTotal = pesoTotal; }
    public String getTipoProduccion() { return tipoProduccion; }
    public void setTipoProduccion(String tipoProduccion) { this.tipoProduccion = tipoProduccion; }
    public String getEstadoSanitario() { return estadoSanitario; }
    public void setEstadoSanitario(String estadoSanitario) { this.estadoSanitario = estadoSanitario; }
    public boolean isVacunasAlDia() { return vacunasAlDia; }
    public void setVacunasAlDia(boolean vacunasAlDia) { this.vacunasAlDia = vacunasAlDia; }
    public boolean isConRegistro() { return conRegistro; }
    public void setConRegistro(boolean conRegistro) { this.conRegistro = conRegistro; }
    public int getTotalVistas() { return totalVistas; }
    public void setTotalVistas(int totalVistas) { this.totalVistas = totalVistas; }
    public int getTotalOfertas() { return totalOfertas; }
    public void setTotalOfertas(int totalOfertas) { this.totalOfertas = totalOfertas; }
}
