package com.sistemaganadero.service;

import com.sistemaganadero.model.Alerta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AlertasService {

    public ObservableList<Alerta> cargarAlertasDemo() {
        return FXCollections.observableArrayList(
                new Alerta("Sanidad", "Vacuna pendiente para el lote A"),
                new Alerta("Peso", "Ternero 1022 con peso inferior al esperado"),
                new Alerta("Inventario", "Stock bajo de concentrado"),
                new Alerta("Control", "Revisar ternero en observación")
        );
    }
}
