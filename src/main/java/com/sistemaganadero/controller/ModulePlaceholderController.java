package com.sistemaganadero.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ModulePlaceholderController {

    @FXML private Label lblTitulo;
    @FXML private Label lblDescripcion;

    public void configurar(String titulo, String descripcion) {
        lblTitulo.setText(titulo);
        lblDescripcion.setText(descripcion);
    }
}
