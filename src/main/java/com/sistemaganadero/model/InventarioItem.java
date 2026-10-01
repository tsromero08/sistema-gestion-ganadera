package com.sistemaganadero.model;

import java.time.LocalDate;

public class InventarioItem {
    private String codigo;
    private String nombre;
    private String categoria;
    private double cantidad;
    private String unidad;
    private double minimo;
    private double costoUnitario;
    private LocalDate fechaIngreso;
    private LocalDate fechaVencimiento;
    private String proveedor;
    private String estado;
    private String observaciones;

    public InventarioItem(String codigo, String nombre, String categoria, double cantidad, String unidad, double minimo, double costoUnitario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.minimo = minimo;
        this.costoUnitario = costoUnitario;
        this.fechaIngreso = LocalDate.now();
        this.estado = "Disponible";
    }

    public InventarioItem(String codigo, String nombre, String categoria, double cantidad, String unidad,
                           double minimo, double costoUnitario, LocalDate fechaIngreso,
                           LocalDate fechaVencimiento, String proveedor, String observaciones) {
        this(codigo, nombre, categoria, cantidad, unidad, minimo, costoUnitario);
        this.fechaIngreso = fechaIngreso;
        this.fechaVencimiento = fechaVencimiento;
        this.proveedor = proveedor;
        this.observaciones = observaciones;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public double getMinimo() { return minimo; }
    public void setMinimo(double minimo) { this.minimo = minimo; }
    public double getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(double costoUnitario) { this.costoUnitario = costoUnitario; }
    public double getValorTotal() { return cantidad * costoUnitario; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public String getEstado() {
        if (estado != null && !estado.isBlank()) return estado;
        return getEstadoStock();
    }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getEstadoStock() {
        if (fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now())) return "Vencido";
        if (cantidad <= 0) return "Agotado";
        if (cantidad <= minimo) return "Bajo mínimo";
        return "Disponible";
    }

    public boolean estaProximoVencer() {
        return fechaVencimiento != null
                && !fechaVencimiento.isBefore(LocalDate.now())
                && fechaVencimiento.isBefore(LocalDate.now().plusDays(30));
    }
}
