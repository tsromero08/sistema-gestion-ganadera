package com.sistemaganadero.model;

public class Ternero {
    private String arete;
    private String nombre;
    private String raza;
    private String sexo;
    private int edadMeses;
    private double peso;
    private String estado;

    public Ternero() {
    }

    public Ternero(String arete, String nombre, String raza, String sexo, int edadMeses, double peso, String estado) {
        this.arete = arete;
        this.nombre = nombre;
        this.raza = raza;
        this.sexo = sexo;
        this.edadMeses = edadMeses;
        this.peso = peso;
        this.estado = estado;
    }

    public String getArete() { return arete; }
    public void setArete(String arete) { this.arete = arete; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public int getEdadMeses() { return edadMeses; }
    public void setEdadMeses(int edadMeses) { this.edadMeses = edadMeses; }

    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
