package com.sistemaganadero.controller;

import com.sistemaganadero.model.Lote;
import com.sistemaganadero.model.Potrero;
import com.sistemaganadero.service.BackupService;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.service.PersistenceService;
import com.sistemaganadero.util.JsonUtil;
import com.sistemaganadero.util.MotorSelector;
import com.sistemaganadero.util.NotificationService;
import com.sistemaganadero.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;

public class PotrerosController {

    @FXML private Label lblFinca;
    @FXML private Label lblTotalPotreros;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Potrero> tablaPotreros;
    @FXML private TableColumn<Potrero, String> colCodigo;
    @FXML private TableColumn<Potrero, String> colNombre;
    @FXML private TableColumn<Potrero, String> colArea;
    @FXML private TableColumn<Potrero, String> colPasto;
    @FXML private TableColumn<Potrero, String> colAltura;
    @FXML private TableColumn<Potrero, String> colPh;
    @FXML private TableColumn<Potrero, String> colClima;
    @FXML private TableColumn<Potrero, String> colDesgaste;
    @FXML private TableColumn<Potrero, String> colCapacidad;
    @FXML private TableColumn<Potrero, String> colLote;
    @FXML private TableColumn<Potrero, String> colEstado;
    @FXML private TableColumn<Potrero, Void> colAccion;

    private ObservableList<Potrero> potreros;
    private FilteredList<Potrero> filtrados;

    @FXML
    public void initialize() {
        int fincaId = Session.getFincaActualId();
        lblFinca.setText(Session.getNombreFincaActual());

        potreros = GanaderiaDataStore.getPotrerosPorFinca(fincaId);
        filtrados = new FilteredList<>(potreros, p -> true);

        configurarColumnas();
        tablaPotreros.setItems(filtrados);
        actualizarConteo();

        txtBuscar.textProperty().addListener((obs, old, val) -> {
            String t = val == null ? "" : val.toLowerCase().trim();
            filtrados.setPredicate(p -> t.isEmpty()
                    || p.getCodigo().toLowerCase().contains(t)
                    || p.getNombre().toLowerCase().contains(t)
                    || (p.getTipoPasto() != null && p.getTipoPasto().toLowerCase().contains(t)));
            actualizarConteo();
        });
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colArea.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f", c.getValue().getAreaHa())));
        colPasto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoPasto()));
        colAltura.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.0f cm", c.getValue().getAlturaPasto())));
        colPh.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f", c.getValue().getPhSuelo())));
        colClima.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClima()));
        colDesgaste.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNivelDesgaste() + "%"));
        colCapacidad.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCapacidadAnimal())));
        colLote.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLoteAsignadoNombre() != null ? c.getValue().getLoteAsignadoNombre() : "Libre"));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().estadoOcupacion()));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(estado);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; " +
                        switch (estado) {
                            case "Disponible" -> "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
                            case "Ocupado"    -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
                            case "Mover ganado"-> "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
                            default            -> "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;";
                        });
                setGraphic(badge);
                setText(null);
            }
        });

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Detalle");
            {
                btn.setStyle("-fx-background-color: #1f7a63; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                btn.setOnAction(e -> verDetalle(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void actualizarConteo() {
        lblTotalPotreros.setText("Total: " + filtrados.size() + " potreros");
    }

    @FXML
    private void registrarPotrero() {
        abrirFormulario(null);
    }

    @FXML
    private void mostrarRecomendaciones() {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Recomendaciones automáticas — Rotación y Fertilización");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f4f8;");

        Label title = new Label("Recomendaciones automáticas para los potreros");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f1f3d;");
        root.getChildren().add(title);

        for (Potrero p : potreros) {
            VBox card = new VBox(6);
            card.setPadding(new Insets(14));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");

            Label header = new Label(p.getNombre() + " (" + p.getCodigo() + ")");
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");

            Label rotacion = new Label("ROTACIÓN: " + p.recomendacionRotacion());
            rotacion.setStyle("-fx-text-fill: #1f7a63; -fx-font-weight: bold;");
            rotacion.setWrapText(true);

            Label fertil = new Label("FERTILIZACIÓN: " + p.recomendacionFertilizacion());
            fertil.setStyle("-fx-text-fill: #b45309;");
            fertil.setWrapText(true);

            String mejoraSuelo = "MEJORA DE SUELO: ";
            if (p.getPhSuelo() < 5.5) mejoraSuelo += "Aplicar cal y materia orgánica para subir pH. ";
            if (p.getNivelDesgaste() > 50) mejoraSuelo += "Sobresembrado de leguminosas (kudzu/cratylia). ";
            if (mejoraSuelo.equals("MEJORA DE SUELO: ")) mejoraSuelo += "Mantener manejo actual.";
            Label mejora = new Label(mejoraSuelo);
            mejora.setStyle("-fx-text-fill: #4338ca;");
            mejora.setWrapText(true);

            card.getChildren().addAll(header, rotacion, fertil, mejora);
            root.getChildren().add(card);
        }

        if (potreros.isEmpty()) {
            Label vacio = new Label("No hay potreros registrados.");
            vacio.setStyle("-fx-text-fill: #94a3b8;");
            root.getChildren().add(vacio);
        }

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #f0f4f8; -fx-background-color: #f0f4f8;");
        Scene scene = new Scene(sp, 720, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void verDetalle(Potrero p) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Potrero — " + p.getNombre());
        info.setHeaderText(p.getCodigo() + " · " + p.getNombre());
        info.setContentText(String.format(
                "Área: %.1f ha%nPasto: %s%nCantidad: %.0f kg/ha%nAltura: %.0f cm%nAltitud: %.0f m%n" +
                "Clima: %s%npH suelo: %.2f%nEstado suelo: %s%nDesgaste: %d%%%n" +
                "Manejo: %s%nCapacidad: %d animales%nLote: %s%n%n%s%n%s",
                p.getAreaHa(), p.getTipoPasto(), p.getCantidadPasto(), p.getAlturaPasto(), p.getAltitud(),
                p.getClima(), p.getPhSuelo(), p.getEstadoSuelo(), p.getNivelDesgaste(),
                p.getTipoManejo(), p.getCapacidadAnimal(),
                p.getLoteAsignadoNombre() != null ? p.getLoteAsignadoNombre() : "Sin asignar",
                "ROTACIÓN: " + p.recomendacionRotacion(),
                "FERTILIZACIÓN: " + p.recomendacionFertilizacion()));
        info.getDialogPane().setPrefWidth(540);
        info.showAndWait();
    }

    private void abrirFormulario(Potrero existente) {
        boolean esNuevo = existente == null;
        int fincaId = Session.getFincaActualId();

        Dialog<Potrero> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Nuevo potrero" : "Editar potrero");
        dialog.setHeaderText(esNuevo ? "Registre un nuevo potrero" : "Edite el potrero " + existente.getCodigo());

        ButtonType ok = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        String codigo = esNuevo ? GanaderiaDataStore.generatePotreroCode(fincaId) : existente.getCodigo();
        TextField fCodigo = new TextField(codigo);
        fCodigo.setEditable(false);
        fCodigo.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #64748b;");

        TextField fNombre = new TextField(esNuevo ? "" : existente.getNombre());
        TextField fArea = new TextField(esNuevo ? "" : String.valueOf(existente.getAreaHa()));
        forzarDecimal(fArea);

        ComboBox<String> fPasto = new ComboBox<>(FXCollections.observableArrayList(
                "Kikuyo", "Brachiaria mulato", "Brachiaria humidicola", "Estrella africana",
                "Pangola", "Ray grass", "Pasto guinea", "Otro"));
        fPasto.setEditable(true);
        fPasto.setPrefWidth(220);
        if (!esNuevo) fPasto.setValue(existente.getTipoPasto());

        TextField fCantidad = new TextField(esNuevo ? "" : String.valueOf(existente.getCantidadPasto()));
        forzarDecimal(fCantidad);
        TextField fAltura = new TextField(esNuevo ? "" : String.valueOf(existente.getAlturaPasto()));
        forzarDecimal(fAltura);
        TextField fAltitud = new TextField(esNuevo ? "" : String.valueOf(existente.getAltitud()));
        forzarDecimal(fAltitud);

        ComboBox<String> fClima = new ComboBox<>(FXCollections.observableArrayList("Frío", "Templado", "Caliente"));
        if (!esNuevo) fClima.setValue(existente.getClima());

        TextField fPh = new TextField(esNuevo ? "" : String.valueOf(existente.getPhSuelo()));
        forzarDecimal(fPh);

        ComboBox<String> fSuelo = new ComboBox<>(FXCollections.observableArrayList(
                "Excelente", "Bueno", "Regular", "Pobre"));
        if (!esNuevo) fSuelo.setValue(existente.getEstadoSuelo());

        TextField fDesgaste = new TextField(esNuevo ? "0" : String.valueOf(existente.getNivelDesgaste()));
        forzarNumerico(fDesgaste);

        ComboBox<String> fManejo = new ComboBox<>(FXCollections.observableArrayList("Libre", "Estabulado", "Semi"));
        if (!esNuevo) fManejo.setValue(existente.getTipoManejo());

        TextField fCapacidad = new TextField(esNuevo ? "" : String.valueOf(existente.getCapacidadAnimal()));
        forzarNumerico(fCapacidad);

        ObservableList<Lote> lotes = GanaderiaDataStore.getLotesPorFinca(fincaId);
        ObservableList<String> nombresLotes = FXCollections.observableArrayList("(Sin asignar)");
        lotes.forEach(l -> nombresLotes.add(l.getNombre()));
        ComboBox<String> fLote = new ComboBox<>(nombresLotes);
        fLote.setValue(esNuevo || existente.getLoteAsignadoNombre() == null
                ? "(Sin asignar)" : existente.getLoteAsignadoNombre());

        DatePicker fIngreso = new DatePicker(esNuevo ? null : existente.getFechaIngresoGanado());
        fIngreso.setPromptText("Opcional — si tiene ganado");

        TextField fObs = new TextField(esNuevo ? "" : (existente.getObservaciones() != null ? existente.getObservaciones() : ""));

        int row = 0;
        addRow(grid, "Código (auto)", fCodigo, row++);
        addRow(grid, "Nombre *", fNombre, row++);
        addRow(grid, "Área (ha) *", fArea, row++);
        addRow(grid, "Tipo de pasto *", fPasto, row++);
        addRow(grid, "Cantidad pasto (kg/ha)", fCantidad, row++);
        addRow(grid, "Altura pasto (cm)", fAltura, row++);
        addRow(grid, "Altitud (msnm)", fAltitud, row++);
        addRow(grid, "Clima *", fClima, row++);
        addRow(grid, "pH suelo (4.0-8.0)", fPh, row++);
        addRow(grid, "Estado suelo", fSuelo, row++);
        addRow(grid, "Nivel desgaste (0-100)", fDesgaste, row++);
        addRow(grid, "Tipo manejo *", fManejo, row++);
        addRow(grid, "Capacidad animales", fCapacidad, row++);
        addRow(grid, "Lote asignado", fLote, row++);
        addRow(grid, "Fecha ingreso ganado", fIngreso, row++);
        addRow(grid, "Observaciones", fObs, row++);
        MotorSelector motorSelector = new MotorSelector();
        grid.add(motorSelector.getNode(), 0, row, 2, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(560);
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefWidth(540);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            String nombre = fNombre.getText().trim();
            if (nombre.isEmpty()) { NotificationService.warning("Ingrese el nombre del potrero"); return null; }
            String pasto = fPasto.getValue();
            if (pasto == null || pasto.isBlank()) { NotificationService.warning("Seleccione tipo de pasto"); return null; }
            String clima = fClima.getValue();
            if (clima == null) { NotificationService.warning("Seleccione el clima"); return null; }
            String manejo = fManejo.getValue();
            if (manejo == null) { NotificationService.warning("Seleccione el tipo de manejo"); return null; }

            String loteNom = fLote.getValue();
            if ("(Sin asignar)".equals(loteNom)) loteNom = null;

            int id = esNuevo ? potreros.size() + fincaId * 300 + (int)(Math.random() * 100) : existente.getId();
            Potrero p = new Potrero(id, codigo, nombre, fincaId,
                    parseDouble(fArea.getText()), pasto,
                    parseDouble(fCantidad.getText()), parseDouble(fAltura.getText()),
                    parseDouble(fAltitud.getText()), clima,
                    parseDouble(fPh.getText()), fSuelo.getValue() != null ? fSuelo.getValue() : "Bueno",
                    (int) parseDouble(fDesgaste.getText()), manejo,
                    (int) parseDouble(fCapacidad.getText()), loteNom,
                    fIngreso.getValue(), fObs.getText().trim());
            p.setFincaNombre(Session.getNombreFincaActual());
            return p;
        });

        final String snapshotAnterior = esNuevo ? null : JsonUtil.toJson(JsonUtil.toMap(existente));

        Optional<Potrero> result = dialog.showAndWait();
        result.ifPresent(p -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            PersistenceService.Resultado res;
            if (esNuevo) {
                potreros.add(p);
                res = PersistenceService.crear("potreros", String.valueOf(p.getId()), p, motor);
            } else {
                BackupService.guardarJson("potreros", String.valueOf(existente.getId()), snapshotAnterior);
                res = PersistenceService.editar("potreros", String.valueOf(existente.getId()), existente, p, motor);
                int idx = potreros.indexOf(existente);
                if (idx >= 0) potreros.set(idx, p);
            }
            actualizarConteo();
            tablaPotreros.refresh();
            String accion = esNuevo ? "registrado" : "actualizado";
            NotificationService.success("Potrero " + p.getCodigo() + " " + accion + " en "
                    + motor.getEtiqueta() + " · copia en MongoDB · " + res.getMensaje());
        });
    }

    private void addRow(GridPane grid, String label, javafx.scene.Node control, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        if (control instanceof javafx.scene.control.Control c) c.setPrefWidth(220);
        grid.add(l, 0, row);
        grid.add(control, 1, row);
    }

    private void forzarNumerico(TextField f) {
        f.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.matches("[0-9]*")) f.setText(old);
        });
    }

    private void forzarDecimal(TextField f) {
        f.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.matches("[0-9]*\\.?[0-9]*")) f.setText(old);
        });
    }

    private double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0; }
    }
}
