package com.sistemaganadero.controller;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.RegistroSanitario;
import com.sistemaganadero.model.Vacuna;
import com.sistemaganadero.service.BackupService;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.service.PersistenceService;
import com.sistemaganadero.service.SanidadService;
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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SanidadController {

    @FXML private Label lblFinca;
    @FXML private Label lblTotalVacunas;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Vacuna> tablaVacunas;
    @FXML private TableColumn<Vacuna, String> colArete;
    @FXML private TableColumn<Vacuna, String> colNombreAnimal;
    @FXML private TableColumn<Vacuna, String> colLote;
    @FXML private TableColumn<Vacuna, String> colFinca;
    @FXML private TableColumn<Vacuna, String> colVacuna;
    @FXML private TableColumn<Vacuna, String> colFechaApp;
    @FXML private TableColumn<Vacuna, String> colProxDosis;
    @FXML private TableColumn<Vacuna, String> colResponsable;
    @FXML private TableColumn<Vacuna, String> colEstado;
    @FXML private TableColumn<Vacuna, Void> colAccion;

    private ObservableList<Vacuna> vacunasData;
    private FilteredList<Vacuna> vacunasFiltradas;
    private Map<Integer, Animal> indexAnimales;

    @FXML
    public void initialize() {
        int fincaId = Session.getFincaActualId();
        lblFinca.setText(Session.getNombreFincaActual());

        vacunasData = GanaderiaDataStore.getVacunasPorFinca(fincaId);
        vacunasFiltradas = new FilteredList<>(vacunasData, v -> true);

        indexAnimales = new HashMap<>();
        GanaderiaDataStore.getAnimalesPorFinca(fincaId).forEach(a -> indexAnimales.put(a.getId(), a));

        configurarColumnas();
        tablaVacunas.setItems(vacunasFiltradas);
        actualizarConteo();

        txtBuscar.textProperty().addListener((obs, old, val) -> {
            String texto = val == null ? "" : val.toLowerCase().trim();
            vacunasFiltradas.setPredicate(v -> {
                Animal a = indexAnimales.get(v.getAnimalId());
                if (texto.isEmpty()) return true;
                return v.getArete().toLowerCase().contains(texto)
                        || v.getNombreVacuna().toLowerCase().contains(texto)
                        || (a != null && a.getNombre() != null && a.getNombre().toLowerCase().contains(texto))
                        || (v.getResponsable() != null && v.getResponsable().toLowerCase().contains(texto));
            });
            actualizarConteo();
        });
    }

    private void configurarColumnas() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colArete.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArete()));
        colNombreAnimal.setCellValueFactory(c -> {
            Animal a = indexAnimales.get(c.getValue().getAnimalId());
            return new SimpleStringProperty(a != null && a.getNombre() != null ? a.getNombre() : "—");
        });
        colLote.setCellValueFactory(c -> {
            Animal a = indexAnimales.get(c.getValue().getAnimalId());
            return new SimpleStringProperty(a != null && a.getLoteNombre() != null ? a.getLoteNombre() : "—");
        });
        colFinca.setCellValueFactory(c -> new SimpleStringProperty(Session.getNombreFincaActual()));
        colVacuna.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreVacuna()));
        colFechaApp.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaAplicacion() != null ? c.getValue().getFechaAplicacion().format(fmt) : "—"));
        colProxDosis.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getProximaDosis() != null ? c.getValue().getProximaDosis().format(fmt) : "N/A"));
        colResponsable.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getResponsable() != null && !c.getValue().getResponsable().isEmpty()
                        ? c.getValue().getResponsable() : "—"));

        colNombreAnimal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) { setText(null); setStyle(""); return; }
                setText(n);
                setStyle("-fx-font-weight: bold; -fx-text-fill: #1f7a63;");
            }
        });

        colEstado.setCellValueFactory(c -> {
            Vacuna v = c.getValue();
            String estado;
            if (v.getProximaDosis() == null) estado = "Dosis única";
            else if (v.getProximaDosis().isBefore(LocalDate.now())) estado = "Vencida";
            else if (v.venceProximamente()) estado = "Próxima dosis";
            else estado = "Al día";
            return new SimpleStringProperty(estado);
        });

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(estado);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; " +
                        switch (estado) {
                            case "Al día"        -> "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
                            case "Próxima dosis" -> "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
                            case "Vencida"       -> "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
                            default              -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
                        });
                setGraphic(badge);
                setText(null);
            }
        });

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnElim = new Button("Eliminar");
            private final HBox box = new HBox(6, btnEditar, btnElim);
            {
                btnEditar.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
                btnEditar.setOnAction(e -> editarVacuna(getTableView().getItems().get(getIndex())));
                btnElim.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                btnElim.setOnAction(e -> {
                    Vacuna v = getTableView().getItems().get(getIndex());
                    // Dialog con selector de motor
                    Dialog<PersistenceService.Motor> dlg = new Dialog<>();
                    dlg.setTitle("Eliminar registro sanitario");
                    dlg.setHeaderText("¿Eliminar vacuna " + v.getNombreVacuna() + " de " + v.getArete() + "?");
                    ButtonType okElim = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
                    dlg.getDialogPane().getButtonTypes().addAll(okElim, ButtonType.CANCEL);
                    MotorSelector sel = new MotorSelector();
                    VBox bx = new VBox(10);
                    bx.setPadding(new Insets(18));
                    Label inf = new Label("Una copia íntegra se conservará en MongoDB.");
                    inf.setStyle("-fx-text-fill: #92400e; -fx-font-size: 12px;");
                    bx.getChildren().addAll(inf, sel.getNode());
                    dlg.getDialogPane().setContent(bx);
                    dlg.setResultConverter(b -> b == okElim ? sel.getMotorSeleccionado() : null);
                    dlg.showAndWait().ifPresent(motor -> {
                        PersistenceService.Resultado res = PersistenceService.eliminar("sanidad_vacunas",
                                String.valueOf(v.getId()), v, motor);
                        vacunasData.remove(v);
                        actualizarConteo();
                        NotificationService.warning("Registro eliminado de " + motor.getEtiqueta()
                                + " · respaldo en MongoDB · " + res.getMensaje());
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void editarVacuna(Vacuna v) {
        Dialog<Vacuna> dialog = new Dialog<>();
        dialog.setTitle("Editar registro sanitario");
        dialog.setHeaderText("Modificando vacuna de " + v.getArete() + " — se generará respaldo automático");
        ButtonType ok = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(18));
        TextField fNombre = new TextField(v.getNombreVacuna() != null ? v.getNombreVacuna() : "");
        fNombre.setPrefWidth(260);
        DatePicker fFecha = new DatePicker(v.getFechaAplicacion());
        DatePicker fProx = new DatePicker(v.getProximaDosis());
        TextField fResp = new TextField(v.getResponsable() != null ? v.getResponsable() : "");
        TextField fObs = new TextField(v.getObservaciones() != null ? v.getObservaciones() : "");

        MotorSelector motorSelector = new MotorSelector();

        int r = 0;
        addEditRow(g, "Vacuna *", fNombre, r++);
        addEditRow(g, "Fecha aplicación *", fFecha, r++);
        addEditRow(g, "Próxima dosis", fProx, r++);
        addEditRow(g, "Responsable", fResp, r++);
        addEditRow(g, "Observaciones", fObs, r++);
        g.add(motorSelector.getNode(), 0, r, 2, 1);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            if (fNombre.getText() == null || fNombre.getText().isBlank()) { NotificationService.warning("Vacuna obligatoria"); return null; }
            if (fFecha.getValue() == null) { NotificationService.warning("Fecha obligatoria"); return null; }
            return v;
        });

        String snapshotAnterior = JsonUtil.toJson(JsonUtil.toMap(v));

        dialog.showAndWait().ifPresent(seleccionado -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            BackupService.guardarJson("sanidad_vacunas", String.valueOf(v.getId()), snapshotAnterior);
            v.setNombreVacuna(fNombre.getText().trim());
            v.setFechaAplicacion(fFecha.getValue());
            v.setProximaDosis(fProx.getValue());
            v.setResponsable(fResp.getText().trim());
            v.setObservaciones(fObs.getText().trim());
            PersistenceService.Resultado res = PersistenceService.editar("sanidad_vacunas",
                    String.valueOf(v.getId()), seleccionado, v, motor);
            tablaVacunas.refresh();
            actualizarConteo();
            NotificationService.success("Vacuna actualizada en " + motor.getEtiqueta()
                    + " · respaldo en MongoDB · " + res.getMensaje());
        });
    }

    private void addEditRow(GridPane g, String label, javafx.scene.Node n, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        g.add(l, 0, row); g.add(n, 1, row);
    }

    private void actualizarConteo() {
        lblTotalVacunas.setText("Total: " + vacunasFiltradas.size() + " registros");
    }

    @FXML
    private void registrarVacuna() {
        int fincaId = Session.getFincaActualId();
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        if (animales.isEmpty()) {
            NotificationService.warning("No hay animales registrados en esta finca");
            return;
        }

        Dialog<Vacuna> dialog = new Dialog<>();
        dialog.setTitle("Registrar Vacuna");
        dialog.setHeaderText("Registro sanitario — vacunación");
        ButtonType ok = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14); grid.setPadding(new Insets(22, 26, 10, 26));

        ComboBox<Animal> cmbAnimal = new ComboBox<>(animales);
        cmbAnimal.setConverter(new StringConverter<>() {
            public String toString(Animal a) {
                return a == null ? "" : a.getArete() + " — " + a.getNombre()
                        + " (" + (a.getLoteNombre() != null ? a.getLoteNombre() : "sin lote") + ")";
            }
            public Animal fromString(String s) { return null; }
        });
        cmbAnimal.setPrefWidth(310);
        cmbAnimal.setPromptText("Seleccione un animal");

        ComboBox<String> cmbVacuna = new ComboBox<>(FXCollections.observableArrayList(
                "Aftosa", "Brucela", "Clostridial", "Rabia bovina", "IBR/DVB",
                "Carbón sintomático", "Leptospirosis", "Otro"
        ));
        cmbVacuna.setPrefWidth(310);
        cmbVacuna.setPromptText("Seleccione el tipo de vacuna");

        TextField txtVacunaCustom = new TextField();
        txtVacunaCustom.setPromptText("Escriba el nombre de la vacuna");
        txtVacunaCustom.setPrefWidth(310);
        txtVacunaCustom.setVisible(false);
        txtVacunaCustom.setManaged(false);
        cmbVacuna.valueProperty().addListener((obs, old, val) -> {
            boolean otro = "Otro".equals(val);
            txtVacunaCustom.setVisible(otro);
            txtVacunaCustom.setManaged(otro);
            if (!otro) txtVacunaCustom.clear();
        });

        DatePicker dpFecha = new DatePicker(LocalDate.now());
        dpFecha.setPrefWidth(310);
        DatePicker dpProx = new DatePicker();
        dpProx.setPromptText("Opcional — dejar vacío si es dosis única");
        dpProx.setPrefWidth(310);

        TextField txtResp = new TextField();
        txtResp.setPromptText("Nombre del veterinario o responsable");
        txtResp.setPrefWidth(310);
        TextField txtObs = new TextField();
        txtObs.setPromptText("Opcional");
        txtObs.setPrefWidth(310);

        int row = 0;
        addRow(grid, "Animal *", cmbAnimal, row++);
        addRow(grid, "Vacuna *", cmbVacuna, row++);
        addRow(grid, "Nombre personalizado", txtVacunaCustom, row++);
        addRow(grid, "Fecha aplicación *", dpFecha, row++);
        addRow(grid, "Próxima dosis", dpProx, row++);
        addRow(grid, "Responsable", txtResp, row++);
        addRow(grid, "Observaciones", txtObs, row);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(540);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            Animal a = cmbAnimal.getValue();
            if (a == null) { NotificationService.warning("Seleccione un animal"); return null; }
            String n = cmbVacuna.getValue();
            if (n == null || n.isEmpty()) { NotificationService.warning("Seleccione el tipo de vacuna"); return null; }
            if ("Otro".equals(n)) {
                n = txtVacunaCustom.getText().trim();
                if (n.isEmpty()) { NotificationService.warning("Ingrese nombre personalizado"); return null; }
            }
            int newId = vacunasData.size() + 2000 + (int)(Math.random() * 500);
            return new Vacuna(newId, a.getId(), Session.getFincaActualId(), a.getArete(),
                    n, dpFecha.getValue(), dpProx.getValue(),
                    txtResp.getText().trim(), txtObs.getText().trim());
        });

        Optional<Vacuna> result = dialog.showAndWait();
        result.ifPresent(v -> {
            GanaderiaDataStore.agregarVacuna(Session.getFincaActualId(), v);
            actualizarConteo();
            Animal a = indexAnimales.get(v.getAnimalId());
            String nom = a != null ? a.getNombre() : v.getArete();
            NotificationService.success("Vacuna " + v.getNombreVacuna() + " registrada para " + nom);
        });
    }

    @FXML
    private void registrarTratamiento() {
        int fincaId = Session.getFincaActualId();
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        if (animales.isEmpty()) {
            NotificationService.warning("No hay animales en esta finca");
            return;
        }

        Dialog<RegistroSanitario> dialog = new Dialog<>();
        dialog.setTitle("Registrar tratamiento sanitario");
        dialog.setHeaderText("Purga, vitamina, tratamiento o revisión");
        ButtonType ok = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12); grid.setPadding(new Insets(20));

        ComboBox<Animal> cmbAnimal = new ComboBox<>(animales);
        cmbAnimal.setConverter(new StringConverter<>() {
            public String toString(Animal a) { return a == null ? "" : a.getArete() + " — " + a.getNombre(); }
            public Animal fromString(String s) { return null; }
        });
        cmbAnimal.setPrefWidth(310);

        ComboBox<RegistroSanitario.Tipo> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(RegistroSanitario.Tipo.values()));
        cmbTipo.setPrefWidth(310);

        TextField fDesc = new TextField();
        fDesc.setPromptText("Ej: Ivermectina 1%, ADE...");
        fDesc.setPrefWidth(310);

        DatePicker dpFecha = new DatePicker(LocalDate.now());
        dpFecha.setPrefWidth(310);
        DatePicker dpProx = new DatePicker();
        dpProx.setPromptText("Opcional");
        dpProx.setPrefWidth(310);
        TextField fResp = new TextField();
        fResp.setPrefWidth(310);
        TextField fCosto = new TextField();
        fCosto.setPromptText("0");
        fCosto.setPrefWidth(310);
        fCosto.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.matches("[0-9]*\\.?[0-9]*")) fCosto.setText(old);
        });
        TextField fObs = new TextField();
        fObs.setPrefWidth(310);

        int row = 0;
        addRow(grid, "Animal *", cmbAnimal, row++);
        addRow(grid, "Tipo *", cmbTipo, row++);
        addRow(grid, "Descripción *", fDesc, row++);
        addRow(grid, "Fecha *", dpFecha, row++);
        addRow(grid, "Próxima aplicación", dpProx, row++);
        addRow(grid, "Responsable", fResp, row++);
        addRow(grid, "Costo ($)", fCosto, row++);
        addRow(grid, "Observaciones", fObs, row);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(540);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            Animal a = cmbAnimal.getValue();
            if (a == null) { NotificationService.warning("Seleccione un animal"); return null; }
            RegistroSanitario.Tipo tipo = cmbTipo.getValue();
            if (tipo == null) { NotificationService.warning("Seleccione el tipo"); return null; }
            String desc = fDesc.getText().trim();
            if (desc.isEmpty()) { NotificationService.warning("Ingrese la descripción"); return null; }
            double costo = 0;
            try { costo = Double.parseDouble(fCosto.getText().trim().isEmpty() ? "0" : fCosto.getText().trim()); }
            catch (Exception ignored) {}
            int id = GanaderiaDataStore.getRegistrosSanitariosPorFinca(fincaId).size()
                    + fincaId * 400 + (int)(Math.random() * 100);
            return new RegistroSanitario(id, a.getId(), fincaId, a.getArete(), a.getNombre(),
                    tipo, desc, dpFecha.getValue(), dpProx.getValue(),
                    fResp.getText().trim(), costo, fObs.getText().trim());
        });

        Optional<RegistroSanitario> result = dialog.showAndWait();
        result.ifPresent(r -> {
            GanaderiaDataStore.agregarRegistroSanitario(fincaId, r);
            NotificationService.success(r.getTipo() + " registrado para " + r.getAnimalNombre());
        });
    }

    @FXML
    private void mostrarPendientes() {
        int fincaId = Session.getFincaActualId();
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Animales que requieren atención sanitaria");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f4f8;");

        root.getChildren().add(seccion("VACUNAS pendientes / vencidas",
                SanidadService.vacunasPendientes(fincaId).stream()
                        .map(av -> "• " + av.animal().getArete() + " — " + av.animal().getNombre()
                                + " | Lote: " + av.animal().getLoteNombre()
                                + " | Vacuna: " + av.vacuna().getNombreVacuna()
                                + " | " + av.estado())
                        .toList()));

        root.getChildren().add(seccion("PURGA requerida (cada 4 meses)",
                SanidadService.animalesRequierenPurga(fincaId).stream()
                        .map(al -> "• " + al.animal().getArete() + " — " + al.animal().getNombre()
                                + " | Lote: " + al.animal().getLoteNombre()
                                + " | " + al.motivo())
                        .toList()));

        root.getChildren().add(seccion("VITAMINAS requeridas (cada 3 meses)",
                SanidadService.animalesRequierenVitaminas(fincaId).stream()
                        .map(al -> "• " + al.animal().getArete() + " — " + al.animal().getNombre()
                                + " | Lote: " + al.animal().getLoteNombre()
                                + " | " + al.motivo())
                        .toList()));

        // Animales con bajo rendimiento
        List<Animal> bajoRend = GanaderiaDataStore.getAnimalesPorFinca(fincaId).stream()
                .filter(a -> a.getGdp() < 0.5).toList();
        VBox planBox = new VBox(8);
        planBox.setPadding(new Insets(14));
        planBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #fee2e2; -fx-border-radius: 10; -fx-border-width: 2;");
        Label hdr = new Label("PLANES INDIVIDUALES — bajo rendimiento (GDP < 0.5)");
        hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #991b1b;");
        planBox.getChildren().add(hdr);
        if (bajoRend.isEmpty()) {
            Label l = new Label("Sin animales con bajo rendimiento.");
            l.setStyle("-fx-text-fill: #94a3b8;");
            planBox.getChildren().add(l);
        } else {
            for (Animal a : bajoRend) {
                Button btn = new Button("Ver plan para " + a.getArete() + " — " + a.getNombre()
                        + " (GDP " + String.format("%.2f", a.getGdp()) + ")");
                btn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #991b1b; -fx-cursor: hand; -fx-background-radius: 6; -fx-font-weight: bold;");
                btn.setOnAction(e -> mostrarPlanIndividual(a));
                planBox.getChildren().add(btn);
            }
        }
        root.getChildren().add(planBox);

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #f0f4f8; -fx-background-color: #f0f4f8;");
        Scene scene = new Scene(sp, 880, 700);
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarPlanIndividual(Animal a) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Plan individual — " + a.getArete() + " " + a.getNombre());

        VBox v = new VBox(12);
        v.setPadding(new Insets(20));
        v.setStyle("-fx-background-color: #f0f4f8;");

        TextArea ta = new TextArea(SanidadService.generarPlanIndividualBajoRendimiento(a));
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefHeight(440);
        ta.setStyle("-fx-control-inner-background: white; -fx-font-family: 'Consolas', monospace; -fx-font-size: 13px;");

        v.getChildren().add(ta);
        stage.setScene(new Scene(v, 720, 520));
        stage.show();
    }

    private VBox seccion(String titulo, java.util.List<String> items) {
        VBox v = new VBox(6);
        v.setPadding(new Insets(14));
        v.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");
        Label l = new Label(titulo);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        v.getChildren().add(l);
        if (items.isEmpty()) {
            Label vacio = new Label("✓ Sin pendientes en esta categoría.");
            vacio.setStyle("-fx-text-fill: #1f7a63;");
            v.getChildren().add(vacio);
        } else {
            for (String it : items) {
                Label lbl = new Label(it);
                lbl.setWrapText(true);
                lbl.setStyle("-fx-text-fill: #475569;");
                v.getChildren().add(lbl);
            }
        }
        return v;
    }

    private void addRow(GridPane grid, String label, javafx.scene.Node control, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        grid.add(l, 0, row);
        grid.add(control, 1, row);
    }
}
