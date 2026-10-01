package com.sistemaganadero.model;

public class Alerta {
    private final String tipo;
    private final String mensaje;

    public Alerta(String tipo, String mensaje) {
        this.tipo = tipo;
        this.mensaje = mensaje;
    }

    public String getTipo() { return tipo; }
    public String getMensaje() { return mensaje; }

    @Override
    public String toString() {
        return tipo + " - " + mensaje;
    }
}
