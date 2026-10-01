package com.sistemaganadero.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlertaCentro {

    public enum Categoria {
        VACUNAS("Vacunas"),
        SANIDAD("Sanidad"),
        STOCK("Stock bajo"),
        POTREROS("Potreros"),
        ALIMENTACION("Alimentación"),
        MARKETPLACE("Marketplace"),
        VENTAS("Ventas"),
        USUARIOS("Usuarios"),
        SISTEMA("Sistema");

        private final String etiqueta;
        Categoria(String etiqueta) { this.etiqueta = etiqueta; }
        public String getEtiqueta() { return etiqueta; }
    }

    public enum Severidad {
        CRITICA("Crítica", "#dc2626"),
        ALTA   ("Alta",    "#f59e0b"),
        MEDIA  ("Media",   "#0369a1"),
        INFO   ("Info",    "#64748b");
        private final String etiqueta;
        private final String color;
        Severidad(String e, String c) { this.etiqueta = e; this.color = c; }
        public String getEtiqueta() { return etiqueta; }
        public String getColor() { return color; }
    }

    public enum Estado {
        NUEVA      ("Nueva",       "#dbeafe", "#1e40af"),
        VISTA      ("Vista",       "#e0f2fe", "#0369a1"),
        EN_PROCESO ("En proceso",  "#fef3c7", "#92400e"),
        RESUELTA   ("Resuelta",    "#d1fae5", "#065f46"),
        IGNORADA   ("Ignorada",    "#f3f4f6", "#374151");
        private final String etiqueta;
        private final String bg;
        private final String fg;
        Estado(String e, String b, String f) { etiqueta = e; bg = b; fg = f; }
        public String getEtiqueta() { return etiqueta; }
        public String getBg() { return bg; }
        public String getFg() { return fg; }
    }

    private int id;
    private Categoria categoria;
    private Severidad severidad;
    private Estado estado;
    private String titulo;
    private String descripcion;
    private String referenciaTipo;   // animal, listing, inventario, potrero, usuario, ...
    private Integer referenciaId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public AlertaCentro() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = Estado.NUEVA;
        this.severidad = Severidad.MEDIA;
    }

    public AlertaCentro(int id, Categoria categoria, Severidad sev, String titulo, String descripcion,
                        String refTipo, Integer refId) {
        this();
        this.id = id;
        this.categoria = categoria;
        this.severidad = sev;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.referenciaTipo = refTipo;
        this.referenciaId = refId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Severidad getSeveridad() { return severidad; }
    public void setSeveridad(Severidad severidad) { this.severidad = severidad; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) {
        this.estado = estado;
        this.fechaActualizacion = LocalDateTime.now();
    }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getReferenciaTipo() { return referenciaTipo; }
    public void setReferenciaTipo(String referenciaTipo) { this.referenciaTipo = referenciaTipo; }
    public Integer getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Integer referenciaId) { this.referenciaId = referenciaId; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }

    public String getFechaFormateada() {
        return fechaCreacion.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }
}
