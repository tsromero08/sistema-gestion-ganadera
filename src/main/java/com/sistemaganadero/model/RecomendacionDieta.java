package com.sistemaganadero.model;

public class RecomendacionDieta {
    private final String forrajeBase;
    private final double cantidadForrajeKg;
    private final String suplemento;
    private final double cantidadSuplementoKg;
    private final String sales;
    private final String agua;
    private final int frecuencia;
    private final String observacion;
    private final String objetivo;

    public RecomendacionDieta(String forrajeBase, double cantidadForrajeKg, String suplemento,
                              double cantidadSuplementoKg, String sales, String agua,
                              int frecuencia, String observacion, String objetivo) {
        this.forrajeBase = forrajeBase;
        this.cantidadForrajeKg = cantidadForrajeKg;
        this.suplemento = suplemento;
        this.cantidadSuplementoKg = cantidadSuplementoKg;
        this.sales = sales;
        this.agua = agua;
        this.frecuencia = frecuencia;
        this.observacion = observacion;
        this.objetivo = objetivo;
    }

    public String getForrajeBase() { return forrajeBase; }
    public double getCantidadForrajeKg() { return cantidadForrajeKg; }
    public String getSuplemento() { return suplemento; }
    public double getCantidadSuplementoKg() { return cantidadSuplementoKg; }
    public String getSales() { return sales; }
    public String getAgua() { return agua; }
    public int getFrecuencia() { return frecuencia; }
    public String getObservacion() { return observacion; }
    public String getObjetivo() { return objetivo; }

    public String resumen() {
        return String.format(
            "OBJETIVO: %s%n" +
            "Forraje base: %s — %.1f kg/animal/día%n" +
            "Suplemento: %s — %.2f kg/animal/día%n" +
            "Sales: %s%n" +
            "Agua: %s%n" +
            "Frecuencia: %d veces/día%n" +
            "Nota: %s",
            objetivo, forrajeBase, cantidadForrajeKg, suplemento, cantidadSuplementoKg,
            sales, agua, frecuencia, observacion);
    }
}
