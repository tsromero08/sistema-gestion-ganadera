package com.sistemaganadero.controller;

import com.sistemaganadero.model.Alerta;
import com.sistemaganadero.service.AlertasService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class DashboardController {

    @FXML private Label lblCantidadTerneros;
    @FXML private Label lblEnObservacion;
    @FXML private Label lblAlertas;
    @FXML private ListView<Alerta> listAlertas;

    private final AlertasService alertasService = new AlertasService();

    @FXML
    public void initialize() {
        ObservableList<Alerta> alertas = alertasService.cargarAlertasDemo();
        listAlertas.setItems(alertas);

        lblCantidadTerneros.setText("18");
        lblEnObservacion.setText("3");
        lblAlertas.setText(String.valueOf(alertas.size()));
    }
}
