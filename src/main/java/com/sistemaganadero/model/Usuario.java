package com.sistemaganadero.model;

import java.time.LocalDateTime;

public class Usuario {

    public enum Rol {
        ADMINISTRADOR("Administrador"),
        VENDEDOR_GANADERO("Vendedor Ganadero"),
        COMPRADOR("Comprador"),
        OPERARIO("Operario"),
        VETERINARIO("Veterinario");

        private final String etiqueta;
        Rol(String etiqueta) { this.etiqueta = etiqueta; }
        public String getEtiqueta() { return etiqueta; }

        public static Rol desdeEtiqueta(String etiqueta) {
            if (etiqueta == null) return OPERARIO;
            for (Rol r : values()) {
                if (r.etiqueta.equalsIgnoreCase(etiqueta) || r.name().equalsIgnoreCase(etiqueta)) return r;
            }
            return OPERARIO;
        }
    }

    private String username;
    private String passwordHash;
    private String nombre;
    private Rol rol;
    private String documento;
    private String telefono;
    private String email;
    private String departamento;
    private String ciudad;
    private boolean activo;
    private LocalDateTime fechaRegistro;

    public Usuario() {
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
        this.rol = Rol.OPERARIO;
    }

    public Usuario(String username, String passwordHash, String nombre, String rolEtiqueta) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
        this.rol = Rol.desdeEtiqueta(rolEtiqueta);
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return passwordHash; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Rol getRolEnum() { return rol; }
    public void setRolEnum(Rol rol) { this.rol = rol; }
    public String getRol() { return rol != null ? rol.getEtiqueta() : "Operario"; }
    public void setRol(String rolEtiqueta) { this.rol = Rol.desdeEtiqueta(rolEtiqueta); }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public boolean puedeVerPrecioMinimo() {
        return rol == Rol.ADMINISTRADOR || rol == Rol.VENDEDOR_GANADERO;
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }
}
