package com.sistemaganadero.controller;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.Pesaje;
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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PesajesController {

    @FXML private Label lblFinca;
    @FXML private Label lblTotalPesajes;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Pesaje> tablaPesajes;
    @FXML private TableColumn<Pesaje, String> colArete;
    @FXML private TableColumn<Pesaje, String> colFecha;
    @FXML private TableColumn<Pesaje, String> colPeso;
    @FXML private TableColumn<Pesaje, String> colObservaciones;
    @FXML private TableColumn<Pesaje, Void> colAccion;

    private ObservableList<Pesaje> pesajesData;
    private FilteredList<Pesaje> pesajesFiltrados;

    @FXML
    public void initialize() {
        int fincaId = Session.getFincaActualId();
        lblFinca.setText(Session.getNombreFincaActual());

        pesajesData = GanaderiaDataStore.getPesajesPorFinca(fincaId);
        pesajesFiltrados = new FilteredList<>(pesajesData, p -> true);

        configurarColumnas();
        tablaPesajes.setItems(pesajesFiltrados);
        actualizarConteo();

        txtBuscar.textProperty().addListener((obs, old, val) -> {
            String texto = val == null ? "" : val.toLowerCase().trim();
            pesajesFiltrados.setPredicate(p -> texto.isEmpty()
                    || p.getArete().toLowerCase().contains(texto)
                    || (p.getObservaciones() != null && p.getObservaciones().toLowerCase().contains(texto)));
            actualizarConteo();
        });
    }

    private void configurarColumnas() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colArete.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArete()));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFecha() != null ? c.getValue().getFecha().format(fmt) : ""));
        colPeso.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.1f kg", c.getValue().getPeso())));
        colObservaciones.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getObservaciones() != null ? c.getValue().getObservaciones() : "—"));

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnElim = new Button("Eliminar");
            private final HBox box = new HBox(6, btnEditar, btnElim);
            {
                btnEditar.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
                btnEditar.setOnAction(e -> editarPesaje(getTableView().getItems().get(getIndex())));
                btnElim.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                btnElim.setOnAction(e -> {
                    Pesaje p = getTableView().getItems().get(getIndex());
                    // Respaldo antes de eliminar también (preserva info — prompt v2)
                    BackupService.guardar("pesajes", String.valueOf(p.getId()), p);
                    pesajesData.remove(p);
                    actualizarConteo();
                    NotificationService.warning("Pesaje eliminado · respaldo creado");
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void editarPesaje(Pesaje p) {
        Dialog<Pesaje> dialog = new Dialog<>();
        dialog.setTitle("Editar pesaje");
        dialog.setHeaderText("Modificando pesaje de " + p.getArete() + " — copia automática en MongoDB");
        ButtonType ok = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(18));

        DatePicker fFecha = new DatePicker(p.getFecha());     fFecha.setPrefWidth(240);
        TextField fPeso = new TextField(String.format("%.1f", p.getPeso()));
        fPeso.textProperty().addListener((o, a, b) -> {
            if (b != null && !b.matches("[0-9]*\\.?[0-9]*")) fPeso.setText(a);
        });
        TextField fObs = new TextField(p.getObservaciones() != null ? p.getObservaciones() : "");
        MotorSelector motorSelector = new MotorSelector();

        addRow(g, "Fecha *", fFecha, 0);
        addRow(g, "Peso (kg) *", fPeso, 1);
        addRow(g, "Observaciones", fObs, 2);
        g.add(motorSelector.getNode(), 0, 3, 2, 1);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            if (fFecha.getValue() == null) { NotificationService.warning("Fecha obligatoria"); return null; }
            try {
                double peso = Double.parseDouble(fPeso.getText().trim());
                if (peso <= 0) { NotificationService.warning("Peso debe ser positivo"); return null; }
            } catch (Exception e) { NotificationService.warning("Peso inválido"); return null; }
            return p;
        });

        String snapshotAnterior = JsonUtil.toJson(JsonUtil.toMap(p));

        dialog.showAndWait().ifPresent(seleccionado -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            BackupService.guardarJson("pesajes", String.valueOf(p.getId()), snapshotAnterior);
            p.setFecha(fFecha.getValue());
            p.setPeso(Double.parseDouble(fPeso.getText().trim()));
            p.setObservaciones(fObs.getText().trim());
            PersistenceService.Resultado res = PersistenceService.editar("pesajes",
                    String.valueOf(p.getId()), seleccionado, p, motor);
            tablaPesajes.refresh();
            NotificationService.success("Pesaje actualizado en " + motor.getEtiqueta()
                    + " · respaldo en MongoDB · " + res.getMensaje());
        });
    }

    private void addRow(GridPane g, String label, javafx.scene.Node n, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        g.add(l, 0, row); g.add(n, 1, row);
    }

    private void actualizarConteo() {
        lblTotalPesajes.setText("Total: " + pesajesFiltrados.size() + " registros");
    }

    @FXML
    private void registrarPesaje() {
        int fincaId = Session.getFincaActualId();
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);

        if (animales.isEmpty()) {
            NotificationService.warning("No hay animales registrados en esta finca");
            return;
        }

        Dialog<Pesaje> dialog = new Dialog<>();
        dialog.setTitle("Registrar Pesaje");
        dialog.setHeaderText("Ingrese el peso del animal");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(22, 26, 10, 26));

        ComboBox<Animal> cmbAnimal = new ComboBox<>(animales);
        cmbAnimal.setConverter(new StringConverter<>() {
            public String toString(Animal a) { return a == null ? "" : a.getArete() + "  —  " + a.getNombre(); }
            public Animal fromString(String s) { return null; }
        });
        cmbAnimal.setPrefWidth(310);
        cmbAnimal.setPromptText("Seleccione un animal");

        // Peso field — only numbers, kg assumed automatically
        TextField txtPeso = new TextField();
        txtPeso.setPromptText("Ej: 385");
        txtPeso.setPrefWidth(200);
        txtPeso.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.matches("[0-9]*\\.?[0-9]*")) {
                txtPeso.setText(old);
            }
        });

        Label lblKgSuffix = new Label("kg");
        lblKgSuffix.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f7a63;");

        HBox pesoBox = new HBox(10, txtPeso, lblKgSuffix);
        pesoBox.setAlignment(Pos.CENTER_LEFT);

        DatePicker dpFecha = new DatePicker(LocalDate.now());
        dpFecha.setPrefWidth(310);

        TextField txtObs = new TextField();
        txtObs.setPromptText("Opcional — condición, lote, observación");
        txtObs.setPrefWidth(310);

        Label lblHint = new Label("El sistema interpreta el valor como kilogramos automáticamente.");
        lblHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");

        MotorSelector motorSelector = new MotorSelector();

        int row = 0;
        addFormRow(grid, "Animal *", cmbAnimal, row++);
        addFormRow(grid, "Peso *", pesoBox, row++);
        addFormRow(grid, "Fecha *", dpFecha, row++);
        addFormRow(grid, "Observaciones", txtObs, row++);
        grid.add(lblHint, 1, row++);
        grid.add(motorSelector.getNode(), 0, row, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(540);
        dialog.getDialogPane().getStylesheets().addAll(
                getClass().getResource("/css/app.css") != null
                        ? getClass().getResource("/css/app.css").toExternalForm()
                        : "");

        dialog.setResultConverter(btn -> {
            if (btn != guardarBtn) return null;

            Animal animal = cmbAnimal.getValue();
            if (animal == null) {
                NotificationService.warning("Seleccione un animal para registrar el pesaje");
                return null;
            }

            String pesoStr = txtPeso.getText().trim();
            if (pesoStr.isEmpty()) {
                NotificationService.warning("Ingrese el peso del animal");
                return null;
            }

            double peso;
            try {
                peso = Double.parseDouble(pesoStr);
            } catch (NumberFormatException ex) {
                NotificationService.warning("Ingrese solo el número del peso (ej: 385)");
                return null;
            }

            if (peso <= 0) {
                NotificationService.warning("El peso debe ser un valor positivo");
                return null;
            }
            if (peso > 2000) {
                NotificationService.warning("Verifique el peso — parece demasiado alto (máx. 2000 kg)");
                // Don't block — allow continuing
            }

            int newId = GanaderiaDataStore.getPesajesPorFinca(fincaId).size() + 1000 + (int)(Math.random() * 500);
            return new Pesaje(newId, animal.getId(), fincaId, animal.getArete(),
                    dpFecha.getValue(), peso, txtObs.getText().trim());
        });

        Optional<Pesaje> result = dialog.showAndWait();
        result.ifPresent(pesaje -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            // Update animal's current weight
            animales.stream()
                    .filter(a -> a.getArete().equals(pesaje.getArete()))
                    .findFirst()
                    .ifPresent(a -> a.setPesoActual(pesaje.getPeso()));

            GanaderiaDataStore.agregarPesaje(fincaId, pesaje);
            PersistenceService.Resultado res = PersistenceService.crear("pesajes",
                    String.valueOf(pesaje.getId()), pesaje, motor);
            actualizarConteo();
            NotificationService.success("Pesaje guardado: " + pesaje.getArete() + " — " + pesaje.getPeso()
                    + " kg en " + motor.getEtiqueta() + " · copia en MongoDB · " + res.getMensaje());
        });
    }

    private void addFormRow(GridPane grid, String label, javafx.scene.Node control, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        grid.add(lbl, 0, row);
        grid.add(control, 1, row);
    }
}
