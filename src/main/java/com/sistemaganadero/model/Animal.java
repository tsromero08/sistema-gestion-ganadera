package com.sistemaganadero.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class Animal {
    private int id;
    private int fincaId;
    private int loteId;
    private String arete;
    private String nombre;
    private String sexo;
    private String raza;
    private LocalDate fechaNacimiento;
    private LocalDate fechaIngreso;
    private double pesoInicial;
    private double pesoActual;
    private double gdp;
    private String estadoReproductivo;
    private String estadoSanitario;
    private String observaciones;
    private boolean activo;
    private LocalDate fechaRegistro;
    private String loteNombre;
    private String fincaNombre;
    private String fotoPath;
    private String estado;

    // --- Ficha técnica ampliada ---
    private String color;
    private int estadoCorporal;              // escala 1-5
    private double produccionLecheDiaria;    // litros/día (si aplica)
    private int numeroPartos;
    private String estadoGestacion;          // No gestante, 1er trim, 2do trim, 3er trim, Lactando
    private String calidadGenetica;          // A, B, C, premium, etc.
    private String registroGenealogico;
    private String lineaGenetica;
    private String usoRecomendado;           // Ceba, Cría, Leche, Reproducción, Levante
    private String tipoProduccion;           // Carne, Leche, Doble propósito
    private LocalDate ultimoParto;
    private LocalDate proximoServicio;
    private String veterinarioResponsable;

    // --- Documentos ---
    private String registroIcaNumero;
    private String guiaSanitariaPath;
    private String certificadoVacunacionPath;
    private String registroPropiedadPath;
    private String registroGenealogicoPath;
    private String facturaCompraPath;
    private String videoPath;
    private List<String> galeriaFotos = new ArrayList<>();
    private List<String> documentosAdjuntos = new ArrayList<>();

    public Animal() {}

    public Animal(int id, int fincaId, int loteId, String arete, String nombre, String raza,
                  String sexo, LocalDate fechaNacimiento, double pesoInicial, double pesoActual,
                  double gdp, String estadoSanitario, String observaciones, String loteNombre) {
        this.id = id;
        this.fincaId = fincaId;
        this.loteId = loteId;
        this.arete = arete;
        this.nombre = nombre;
        this.raza = raza;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.pesoInicial = pesoInicial;
        this.pesoActual = pesoActual;
        this.gdp = gdp;
        this.estadoSanitario = estadoSanitario;
        this.estadoReproductivo = "Normal";
        this.observaciones = observaciones;
        this.activo = true;
        this.fechaRegistro = LocalDate.now();
        this.fechaIngreso = LocalDate.now();
        this.loteNombre = loteNombre;
        this.estado = "Activo";
        this.estadoCorporal = 3;
        this.estadoGestacion = "No gestante";
        this.tipoProduccion = "Carne";
        this.usoRecomendado = "Ceba";
        this.calidadGenetica = "B";
    }

    public String calcularEstado() {
        if (gdp >= 0.90) return "Rentable";
        if (gdp >= 0.70) return "Mantener";
        if (gdp >= 0.50) return "Bajo";
        return "No Rentable";
    }

    public String getPesoActualFormateado() {
        return String.format("%.1f kg", pesoActual);
    }

    public String getGdpFormateado() {
        return String.format("%.2f kg/día", gdp);
    }

    public int getEdadMeses() {
        if (fechaNacimiento == null) return 0;
        Period p = Period.between(fechaNacimiento, LocalDate.now());
        return p.getYears() * 12 + p.getMonths();
    }

    public String getEdadFormateada() {
        int meses = getEdadMeses();
        int anios = meses / 12;
        int sobranteMeses = meses % 12;
        if (anios == 0) return meses + " meses";
        if (sobranteMeses == 0) return anios + (anios == 1 ? " año" : " años");
        return anios + (anios == 1 ? " año " : " años ") + sobranteMeses + "m";
    }

    public int getDiasEnSistema() {
        LocalDate ingreso = fechaIngreso != null ? fechaIngreso : fechaRegistro;
        if (ingreso == null) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(ingreso, LocalDate.now());
    }

    public double getGananciaTotal() {
        return pesoActual - pesoInicial;
    }

    public String getEstadoCorporalEtiqueta() {
        return switch (estadoCorporal) {
            case 1 -> "1 — Muy delgado";
            case 2 -> "2 — Delgado";
            case 3 -> "3 — Bueno";
            case 4 -> "4 — Buena condición";
            case 5 -> "5 — Excesivo";
            default -> estadoCorporal + " — N/D";
        };
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFincaId() { return fincaId; }
    public void setFincaId(int fincaId) { this.fincaId = fincaId; }
    public int getLoteId() { return loteId; }
    public void setLoteId(int loteId) { this.loteId = loteId; }
    public String getArete() { return arete; }
    public void setArete(String arete) { this.arete = arete; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public LocalDate getFechaIngreso() { return fechaIngreso != null ? fechaIngreso : fechaRegistro; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public double getPesoInicial() { return pesoInicial; }
    public void setPesoInicial(double pesoInicial) { this.pesoInicial = pesoInicial; }
    public double getPesoActual() { return pesoActual; }
    public void setPesoActual(double pesoActual) { this.pesoActual = pesoActual; }
    public double getGdp() { return gdp; }
    public void setGdp(double gdp) { this.gdp = gdp; }
    public String getEstadoReproductivo() { return estadoReproductivo; }
    public void setEstadoReproductivo(String estadoReproductivo) { this.estadoReproductivo = estadoReproductivo; }
    public String getEstadoSanitario() { return estadoSanitario; }
    public void setEstadoSanitario(String estadoSanitario) { this.estadoSanitario = estadoSanitario; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getLoteNombre() { return loteNombre; }
    public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }
    public String getFincaNombre() { return fincaNombre; }
    public void setFincaNombre(String fincaNombre) { this.fincaNombre = fincaNombre; }
    public String getFotoPath() { return fotoPath; }
    public void setFotoPath(String fotoPath) { this.fotoPath = fotoPath; }
    public String getEstado() { return estado != null ? estado : "Activo"; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getEstadoCorporal() { return estadoCorporal; }
    public void setEstadoCorporal(int estadoCorporal) { this.estadoCorporal = estadoCorporal; }
    public double getProduccionLecheDiaria() { return produccionLecheDiaria; }
    public void setProduccionLecheDiaria(double produccionLecheDiaria) { this.produccionLecheDiaria = produccionLecheDiaria; }
    public int getNumeroPartos() { return numeroPartos; }
    public void setNumeroPartos(int numeroPartos) { this.numeroPartos = numeroPartos; }
    public String getEstadoGestacion() { return estadoGestacion; }
    public void setEstadoGestacion(String estadoGestacion) { this.estadoGestacion = estadoGestacion; }
    public String getCalidadGenetica() { return calidadGenetica; }
    public void setCalidadGenetica(String calidadGenetica) { this.calidadGenetica = calidadGenetica; }
    public String getRegistroGenealogico() { return registroGenealogico; }
    public void setRegistroGenealogico(String registroGenealogico) { this.registroGenealogico = registroGenealogico; }
    public String getLineaGenetica() { return lineaGenetica; }
    public void setLineaGenetica(String lineaGenetica) { this.lineaGenetica = lineaGenetica; }
    public String getUsoRecomendado() { return usoRecomendado; }
    public void setUsoRecomendado(String usoRecomendado) { this.usoRecomendado = usoRecomendado; }
    public String getTipoProduccion() { return tipoProduccion; }
    public void setTipoProduccion(String tipoProduccion) { this.tipoProduccion = tipoProduccion; }
    public LocalDate getUltimoParto() { return ultimoParto; }
    public void setUltimoParto(LocalDate ultimoParto) { this.ultimoParto = ultimoParto; }
    public LocalDate getProximoServicio() { return proximoServicio; }
    public void setProximoServicio(LocalDate proximoServicio) { this.proximoServicio = proximoServicio; }
    public String getVeterinarioResponsable() { return veterinarioResponsable; }
    public void setVeterinarioResponsable(String veterinarioResponsable) { this.veterinarioResponsable = veterinarioResponsable; }
    public String getRegistroIcaNumero() { return registroIcaNumero; }
    public void setRegistroIcaNumero(String registroIcaNumero) { this.registroIcaNumero = registroIcaNumero; }
    public String getGuiaSanitariaPath() { return guiaSanitariaPath; }
    public void setGuiaSanitariaPath(String guiaSanitariaPath) { this.guiaSanitariaPath = guiaSanitariaPath; }
    public String getCertificadoVacunacionPath() { return certificadoVacunacionPath; }
    public void setCertificadoVacunacionPath(String certificadoVacunacionPath) { this.certificadoVacunacionPath = certificadoVacunacionPath; }
    public String getRegistroPropiedadPath() { return registroPropiedadPath; }
    public void setRegistroPropiedadPath(String registroPropiedadPath) { this.registroPropiedadPath = registroPropiedadPath; }
    public String getRegistroGenealogicoPath() { return registroGenealogicoPath; }
    public void setRegistroGenealogicoPath(String registroGenealogicoPath) { this.registroGenealogicoPath = registroGenealogicoPath; }
    public String getFacturaCompraPath() { return facturaCompraPath; }
    public void setFacturaCompraPath(String facturaCompraPath) { this.facturaCompraPath = facturaCompraPath; }
    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }
    public List<String> getGaleriaFotos() { return galeriaFotos; }
    public void setGaleriaFotos(List<String> galeriaFotos) { this.galeriaFotos = galeriaFotos; }
    public List<String> getDocumentosAdjuntos() { return documentosAdjuntos; }
    public void setDocumentosAdjuntos(List<String> documentosAdjuntos) { this.documentosAdjuntos = documentosAdjuntos; }
}
