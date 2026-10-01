package com.sistemaganadero.model;

import java.time.LocalDate;

public class Vacuna {
    private int id;
    private int animalId;
    private int fincaId;
    private String arete;
    private String nombreVacuna;
    private LocalDate fechaAplicacion;
    private LocalDate proximaDosis;
    private String responsable;
    private String dosis;
    private String lote;
    private String observaciones;
    private String documento;

    public Vacuna() {}

    public Vacuna(int id, int animalId, int fincaId, String arete, String nombreVacuna,
                  LocalDate fechaAplicacion, LocalDate proximaDosis, String responsable, String observaciones) {
        this.id = id;
        this.animalId = animalId;
        this.fincaId = fincaId;
        this.arete = arete;
        this.nombreVacuna = nombreVacuna;
        this.fechaAplicacion = fechaAplicacion;
        this.proximaDosis = proximaDosis;
        this.responsable = responsable;
        this.observaciones = observaciones;
        this.dosis = "1 dosis";
        this.lote = "—";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAnimalId() { return animalId; }
    public void setAnimalId(int animalId) { this.animalId = animalId; }
    public int getFincaId() { return fincaId; }
    public void setFincaId(int fincaId) { this.fincaId = fincaId; }
    public String getArete() { return arete; }
    public void setArete(String arete) { this.arete = arete; }
    public String getNombreVacuna() { return nombreVacuna; }
    public void setNombreVacuna(String nombreVacuna) { this.nombreVacuna = nombreVacuna; }
    public LocalDate getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDate fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }
    public LocalDate getProximaDosis() { return proximaDosis; }
    public void setProximaDosis(LocalDate proximaDosis) { this.proximaDosis = proximaDosis; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }
    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public boolean venceProximamente() {
        if (proximaDosis == null) return false;
        LocalDate hoy = LocalDate.now();
        return !proximaDosis.isBefore(hoy) && !proximaDosis.isAfter(hoy.plusDays(7));
    }

    public boolean estaVencida() {
        return proximaDosis != null && proximaDosis.isBefore(LocalDate.now());
    }

    public String getEstado() {
        if (estaVencida()) return "VENCIDA";
        if (venceProximamente()) return "PRÓXIMA";
        return "AL DÍA";
    }

    public int getDiasRestantes() {
        if (proximaDosis == null) return Integer.MAX_VALUE;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), proximaDosis);
    }
}
