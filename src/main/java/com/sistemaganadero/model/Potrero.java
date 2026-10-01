package com.sistemaganadero.model;

import java.time.LocalDate;

public class Potrero {
    private int id;
    private String codigo;
    private String nombre;
    private int fincaId;
    private String fincaNombre;
    private double areaHa;
    private String tipoPasto;
    private double cantidadPasto;
    private double alturaPasto;
    private double altitud;
    private String clima;
    private double phSuelo;
    private String estadoSuelo;
    private int nivelDesgaste;
    private String tipoManejo;
    private int capacidadAnimal;
    private Integer loteAsignadoId;
    private String loteAsignadoNombre;
    private LocalDate fechaIngresoGanado;
    private LocalDate fechaSalidaRecomendada;
    private String observaciones;
    private LocalDate ultimaFertilizacion;

    public Potrero() {}

    public Potrero(int id, String codigo, String nombre, int fincaId, double areaHa,
                   String tipoPasto, double cantidadPasto, double alturaPasto, double altitud,
                   String clima, double phSuelo, String estadoSuelo, int nivelDesgaste,
                   String tipoManejo, int capacidadAnimal, String loteAsignadoNombre,
                   LocalDate fechaIngresoGanado, String observaciones) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.fincaId = fincaId;
        this.areaHa = areaHa;
        this.tipoPasto = tipoPasto;
        this.cantidadPasto = cantidadPasto;
        this.alturaPasto = alturaPasto;
        this.altitud = altitud;
        this.clima = clima;
        this.phSuelo = phSuelo;
        this.estadoSuelo = estadoSuelo;
        this.nivelDesgaste = nivelDesgaste;
        this.tipoManejo = tipoManejo;
        this.capacidadAnimal = capacidadAnimal;
        this.loteAsignadoNombre = loteAsignadoNombre;
        this.fechaIngresoGanado = fechaIngresoGanado;
        this.observaciones = observaciones;
        if (fechaIngresoGanado != null) {
            this.fechaSalidaRecomendada = fechaIngresoGanado.plusDays(diasUsoRecomendado());
        }
    }

    public int diasUsoRecomendado() {
        int base = 30;
        if (nivelDesgaste >= 70) base = 12;
        else if (nivelDesgaste >= 50) base = 18;
        else if (nivelDesgaste >= 30) base = 24;
        if (alturaPasto < 15) base = Math.min(base, 15);
        if ("Estabulado".equalsIgnoreCase(tipoManejo)) base = 60;
        return Math.max(8, base);
    }

    public String estadoOcupacion() {
        if (loteAsignadoId == null && (loteAsignadoNombre == null || loteAsignadoNombre.isBlank()))
            return "Disponible";
        if (fechaSalidaRecomendada != null && fechaSalidaRecomendada.isBefore(LocalDate.now()))
            return "Mover ganado";
        return "Ocupado";
    }

    public boolean requiereFertilizacion() {
        if (phSuelo > 0 && phSuelo < 5.5) return true;
        if (nivelDesgaste >= 60) return true;
        if (ultimaFertilizacion != null && ultimaFertilizacion.isBefore(LocalDate.now().minusMonths(6)))
            return true;
        return false;
    }

    public String recomendacionFertilizacion() {
        if (!requiereFertilizacion()) return "No requiere — suelo en condiciones óptimas.";
        StringBuilder sb = new StringBuilder();
        if (phSuelo > 0 && phSuelo < 5.5)
            sb.append("Aplicar cal agrícola (1.5-2 ton/ha) para corregir acidez. ");
        if (nivelDesgaste >= 60)
            sb.append("Fertilizar con NPK 10-30-10 (200 kg/ha). ");
        if (ultimaFertilizacion == null || ultimaFertilizacion.isBefore(LocalDate.now().minusMonths(6)))
            sb.append("Aplicar urea (50 kg/ha) y descansar 30 días. ");
        return sb.toString().trim();
    }

    public String recomendacionRotacion() {
        if ("Estabulado".equalsIgnoreCase(tipoManejo)) return "Sistema estabulado — rotación no aplica.";
        if (fechaIngresoGanado == null) return "Sin ganado asignado — listo para rotación.";
        long dias = java.time.temporal.ChronoUnit.DAYS.between(fechaIngresoGanado, LocalDate.now());
        if (dias >= diasUsoRecomendado())
            return "Mover ganado YA. Llevan " + dias + " días — exceden el descanso.";
        long restantes = diasUsoRecomendado() - dias;
        return "Mantener " + restantes + " días más, luego rotar.";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getFincaId() { return fincaId; }
    public void setFincaId(int fincaId) { this.fincaId = fincaId; }
    public String getFincaNombre() { return fincaNombre; }
    public void setFincaNombre(String fincaNombre) { this.fincaNombre = fincaNombre; }
    public double getAreaHa() { return areaHa; }
    public void setAreaHa(double areaHa) { this.areaHa = areaHa; }
    public String getTipoPasto() { return tipoPasto; }
    public void setTipoPasto(String tipoPasto) { this.tipoPasto = tipoPasto; }
    public double getCantidadPasto() { return cantidadPasto; }
    public void setCantidadPasto(double cantidadPasto) { this.cantidadPasto = cantidadPasto; }
    public double getAlturaPasto() { return alturaPasto; }
    public void setAlturaPasto(double alturaPasto) { this.alturaPasto = alturaPasto; }
    public double getAltitud() { return altitud; }
    public void setAltitud(double altitud) { this.altitud = altitud; }
    public String getClima() { return clima; }
    public void setClima(String clima) { this.clima = clima; }
    public double getPhSuelo() { return phSuelo; }
    public void setPhSuelo(double phSuelo) { this.phSuelo = phSuelo; }
    public String getEstadoSuelo() { return estadoSuelo; }
    public void setEstadoSuelo(String estadoSuelo) { this.estadoSuelo = estadoSuelo; }
    public int getNivelDesgaste() { return nivelDesgaste; }
    public void setNivelDesgaste(int nivelDesgaste) { this.nivelDesgaste = nivelDesgaste; }
    public String getTipoManejo() { return tipoManejo; }
    public void setTipoManejo(String tipoManejo) { this.tipoManejo = tipoManejo; }
    public int getCapacidadAnimal() { return capacidadAnimal; }
    public void setCapacidadAnimal(int capacidadAnimal) { this.capacidadAnimal = capacidadAnimal; }
    public Integer getLoteAsignadoId() { return loteAsignadoId; }
    public void setLoteAsignadoId(Integer loteAsignadoId) { this.loteAsignadoId = loteAsignadoId; }
    public String getLoteAsignadoNombre() { return loteAsignadoNombre; }
    public void setLoteAsignadoNombre(String loteAsignadoNombre) { this.loteAsignadoNombre = loteAsignadoNombre; }
    public LocalDate getFechaIngresoGanado() { return fechaIngresoGanado; }
    public void setFechaIngresoGanado(LocalDate fechaIngresoGanado) { this.fechaIngresoGanado = fechaIngresoGanado; }
    public LocalDate getFechaSalidaRecomendada() { return fechaSalidaRecomendada; }
    public void setFechaSalidaRecomendada(LocalDate fechaSalidaRecomendada) { this.fechaSalidaRecomendada = fechaSalidaRecomendada; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDate getUltimaFertilizacion() { return ultimaFertilizacion; }
    public void setUltimaFertilizacion(LocalDate ultimaFertilizacion) { this.ultimaFertilizacion = ultimaFertilizacion; }

    @Override
    public String toString() { return nombre; }
}
