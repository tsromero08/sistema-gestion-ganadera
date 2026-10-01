package com.sistemaganadero.controller;

import com.sistemaganadero.model.Ternero;
import com.sistemaganadero.service.BackupService;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.service.PersistenceService;
import com.sistemaganadero.util.Alerts;
import com.sistemaganadero.util.JsonUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class TernerosController {

    @FXML private TextField txtArete;
    @FXML private TextField txtNombre;
    @FXML private TextField txtRaza;
    @FXML private ComboBox<String> cmbSexo;
    @FXML private TextField txtEdadMeses;
    @FXML private TextField txtPeso;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<PersistenceService.Motor> cmbMotor;

    @FXML private TableView<Ternero> tablaTerneros;
    @FXML private TableColumn<Ternero, String> colArete;
    @FXML private TableColumn<Ternero, String> colNombre;
    @FXML private TableColumn<Ternero, String> colRaza;
    @FXML private TableColumn<Ternero, String> colSexo;
    @FXML private TableColumn<Ternero, Integer> colEdad;
    @FXML private TableColumn<Ternero, Double> colPeso;
    @FXML private TableColumn<Ternero, String> colEstado;

    private final ObservableList<Ternero> listaTerneros = GanaderiaDataStore.getTerneros();
    private Ternero terneroSeleccionado;

    @FXML
    public void initialize() {
        cmbSexo.setItems(FXCollections.observableArrayList("Macho", "Hembra"));
        cmbEstado.setItems(FXCollections.observableArrayList("Activo", "Observación", "Riesgo"));
        if (cmbMotor != null) {
            cmbMotor.setItems(FXCollections.observableArrayList(PersistenceService.Motor.values()));
            cmbMotor.setValue(PersistenceService.Motor.MONGODB);
        }

        colArete.setCellValueFactory(new PropertyValueFactory<>("arete"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRaza.setCellValueFactory(new PropertyValueFactory<>("raza"));
        colSexo.setCellValueFactory(new PropertyValueFactory<>("sexo"));
        colEdad.setCellValueFactory(new PropertyValueFactory<>("edadMeses"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaTerneros.setItems(listaTerneros);

        tablaTerneros.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                terneroSeleccionado = actual;
                txtArete.setText(actual.getArete());
                txtNombre.setText(actual.getNombre());
                txtRaza.setText(actual.getRaza());
                cmbSexo.setValue(actual.getSexo());
                txtEdadMeses.setText(String.valueOf(actual.getEdadMeses()));
                txtPeso.setText(String.valueOf(actual.getPeso()));
                cmbEstado.setValue(actual.getEstado());
            }
        });

    }

    @FXML
    private void agregarTernero() {
        try {
            Ternero ternero = leerFormulario();
            if (existeArete(ternero.getArete())) {
                Alerts.warning("Sistema Ganadero", "Arete duplicado", "Ya existe un ternero con ese arete.");
                return;
            }
            listaTerneros.add(ternero);
            PersistenceService.Motor motor = cmbMotor != null && cmbMotor.getValue() != null
                    ? cmbMotor.getValue() : PersistenceService.Motor.MONGODB;
            PersistenceService.Resultado res = PersistenceService.crear("terneros", ternero.getArete(), ternero, motor);
            limpiarFormulario();
            Alerts.info("Sistema Ganadero", "Registro exitoso",
                    "Ternero agregado en " + motor.getEtiqueta() + " · copia en MongoDB · " + res.getMensaje());
        } catch (Exception e) {
            Alerts.error("Sistema Ganadero", "Error al agregar", e.getMessage());
        }
    }

    @FXML
    private void editarTernero() {
        if (terneroSeleccionado == null) {
            Alerts.warning("Sistema Ganadero", "Sin selección", "Selecciona un ternero de la tabla para editar.");
            return;
        }

        try {
            Ternero nuevosDatos = leerFormulario();

            boolean duplicado = listaTerneros.stream()
                    .filter(t -> t != terneroSeleccionado)
                    .anyMatch(t -> t.getArete().equalsIgnoreCase(nuevosDatos.getArete()));

            if (duplicado) {
                Alerts.warning("Sistema Ganadero", "Arete duplicado", "Ya existe otro ternero con ese arete.");
                return;
            }

            PersistenceService.Motor motor = cmbMotor != null && cmbMotor.getValue() != null
                    ? cmbMotor.getValue() : PersistenceService.Motor.MONGODB;
            String snapshotAnterior = JsonUtil.toJson(JsonUtil.toMap(terneroSeleccionado));
            BackupService.guardarJson("terneros", terneroSeleccionado.getArete(), snapshotAnterior);

            terneroSeleccionado.setArete(nuevosDatos.getArete());
            terneroSeleccionado.setNombre(nuevosDatos.getNombre());
            terneroSeleccionado.setRaza(nuevosDatos.getRaza());
            terneroSeleccionado.setSexo(nuevosDatos.getSexo());
            terneroSeleccionado.setEdadMeses(nuevosDatos.getEdadMeses());
            terneroSeleccionado.setPeso(nuevosDatos.getPeso());
            terneroSeleccionado.setEstado(nuevosDatos.getEstado());

            PersistenceService.Resultado res = PersistenceService.editar("terneros",
                    terneroSeleccionado.getArete(), nuevosDatos, terneroSeleccionado, motor);

            tablaTerneros.refresh();
            limpiarFormulario();
            tablaTerneros.getSelectionModel().clearSelection();
            terneroSeleccionado = null;
            Alerts.info("Sistema Ganadero", "Edición exitosa",
                    "Ternero actualizado en " + motor.getEtiqueta() + " · respaldo en MongoDB · " + res.getMensaje());
        } catch (Exception e) {
            Alerts.error("Sistema Ganadero", "Error al editar", e.getMessage());
        }
    }

    @FXML
    private void eliminarTernero() {
        Ternero seleccionado = tablaTerneros.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alerts.warning("Sistema Ganadero", "Sin selección", "Selecciona un ternero de la tabla para eliminar.");
            return;
        }
        // Respaldo + eliminar del motor elegido
        PersistenceService.Motor motor = cmbMotor != null && cmbMotor.getValue() != null
                ? cmbMotor.getValue() : PersistenceService.Motor.MONGODB;
        PersistenceService.Resultado res = PersistenceService.eliminar("terneros",
                seleccionado.getArete(), seleccionado, motor);
        listaTerneros.remove(seleccionado);
        limpiarFormulario();
        tablaTerneros.getSelectionModel().clearSelection();
        terneroSeleccionado = null;
        Alerts.info("Sistema Ganadero", "Eliminación exitosa",
                "Ternero eliminado de " + motor.getEtiqueta() + " · respaldo en MongoDB · " + res.getMensaje());
    }

    @FXML
    private void limpiarFormulario() {
        txtArete.clear();
        txtNombre.clear();
        txtRaza.clear();
        txtEdadMeses.clear();
        txtPeso.clear();
        cmbSexo.setValue(null);
        cmbEstado.setValue(null);
    }

    private Ternero leerFormulario() {
        String arete = safe(txtArete.getText());
        String nombre = safe(txtNombre.getText());
        String raza = safe(txtRaza.getText());
        String sexo = cmbSexo.getValue();
        String edadTexto = safe(txtEdadMeses.getText());
        String pesoTexto = safe(txtPeso.getText());
        String estado = cmbEstado.getValue();

        if (arete.isBlank() || nombre.isBlank() || raza.isBlank() || sexo == null || edadTexto.isBlank() || pesoTexto.isBlank() || estado == null) {
            throw new IllegalArgumentException("Todos los campos son obligatorios.");
        }

        int edad;
        double peso;

        try {
            edad = Integer.parseInt(edadTexto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La edad debe ser numérica.");
        }

        try {
            peso = Double.parseDouble(pesoTexto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El peso debe ser numérico.");
        }

        if (edad < 0 || peso <= 0) {
            throw new IllegalArgumentException("Edad y peso deben ser valores válidos.");
        }

        return new Ternero(arete, nombre, raza, sexo, edad, peso, estado);
    }

    private boolean existeArete(String arete) {
        return listaTerneros.stream().anyMatch(t -> t.getArete().equalsIgnoreCase(arete));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
