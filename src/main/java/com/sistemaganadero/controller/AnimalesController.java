package com.sistemaganadero.controller;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.Lote;
import com.sistemaganadero.service.BackupService;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.service.GananciaDiariaService;
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
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.Optional;

public class AnimalesController {

    @FXML private Label lblFinca;
    @FXML private Label lblTotalAnimales;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroLote;
    @FXML private TableView<Animal> tablaAnimales;
    @FXML private TableColumn<Animal, String> colArete;
    @FXML private TableColumn<Animal, String> colNombre;
    @FXML private TableColumn<Animal, String> colRaza;
    @FXML private TableColumn<Animal, String> colSexo;
    @FXML private TableColumn<Animal, String> colEdad;
    @FXML private TableColumn<Animal, String> colPesoActual;
    @FXML private TableColumn<Animal, String> colGdp;
    @FXML private TableColumn<Animal, String> colLote;
    @FXML private TableColumn<Animal, String> colEstado;
    @FXML private TableColumn<Animal, Void> colAccion;

    private ObservableList<Animal> animalesData;
    private FilteredList<Animal> animalesFiltrados;

    @FXML
    public void initialize() {
        int fincaId = Session.getFincaActualId();
        lblFinca.setText(Session.getNombreFincaActual());

        // Recalcular GDPs automáticamente con base en historial de pesajes
        GananciaDiariaService.recalcularTodos(fincaId);

        animalesData = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        animalesFiltrados = new FilteredList<>(animalesData, a -> true);

        configurarColumnas();
        tablaAnimales.setItems(animalesFiltrados);
        tablaAnimales.setRowFactory(tv -> {
            TableRow<Animal> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirDashboardAnimal(row.getItem());
                }
            });
            return row;
        });
        actualizarConteo();

        cargarFiltroLotes(fincaId);
        configurarFiltros();
    }

    private void configurarColumnas() {
        colArete.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArete()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNombre() != null && !c.getValue().getNombre().isBlank()
                        ? c.getValue().getNombre() : "—"));
        colRaza.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRaza()));
        colSexo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSexo()));
        colEdad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEdadFormateada()));
        colPesoActual.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.0f kg", c.getValue().getPesoActual())));
        colGdp.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f kg/día", c.getValue().getGdp())));
        colLote.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLoteNombre()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().calcularEstado()));

        // Negrita el nombre
        colNombre.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String nombre, boolean empty) {
                super.updateItem(nombre, empty);
                if (empty || nombre == null) { setText(null); setStyle(""); return; }
                setText(nombre);
                setStyle("-fx-font-weight: bold; -fx-text-fill: #1f7a63;");
            }
        });

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setText(null); setGraphic(null); setStyle(""); return; }
                Label badge = new Label(estado);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; " +
                    switch (estado) {
                        case "Rentable"    -> "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
                        case "Mantener"    -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
                        case "Bajo"        -> "-fx-background-color: #fed7aa; -fx-text-fill: #92400e;";
                        case "No Rentable" -> "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
                        default            -> "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;";
                    });
                setGraphic(badge);
                setText(null);
            }
        });

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnDashboard = new Button("Dashboard");
            private final Button btnEditar = new Button("Editar");
            private final HBox box = new HBox(6, btnDashboard, btnEditar);

            {
                btnDashboard.setStyle("-fx-background-color: #1f7a63; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                btnEditar.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                box.setAlignment(Pos.CENTER_LEFT);
                btnDashboard.setOnAction(e -> abrirDashboardAnimal(getTableView().getItems().get(getIndex())));
                btnEditar.setOnAction(e -> editarAnimal(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void cargarFiltroLotes(int fincaId) {
        ObservableList<Lote> lotes = GanaderiaDataStore.getLotesPorFinca(fincaId);
        ObservableList<String> opciones = FXCollections.observableArrayList("Todos los lotes");
        lotes.forEach(l -> opciones.add(l.getNombre()));
        cmbFiltroLote.setItems(opciones);
        cmbFiltroLote.getSelectionModel().selectFirst();
    }

    private void configurarFiltros() {
        txtBuscar.textProperty().addListener((obs, old, val) -> aplicarFiltro());
        cmbFiltroLote.valueProperty().addListener((obs, old, val) -> aplicarFiltro());
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase().trim();
        String lote = cmbFiltroLote.getValue();
        animalesFiltrados.setPredicate(a -> {
            boolean coincideTexto = texto.isEmpty()
                    || (a.getArete() != null && a.getArete().toLowerCase().contains(texto))
                    || (a.getNombre() != null && a.getNombre().toLowerCase().contains(texto))
                    || (a.getRaza() != null && a.getRaza().toLowerCase().contains(texto));
            boolean coincideLote = lote == null || lote.equals("Todos los lotes")
                    || (a.getLoteNombre() != null && a.getLoteNombre().equals(lote));
            return coincideTexto && coincideLote;
        });
        actualizarConteo();
    }

    private void actualizarConteo() {
        lblTotalAnimales.setText("Total: " + animalesFiltrados.size() + " animales");
    }

    @FXML
    private void registrarAnimal() {
        abrirFormularioAnimal(null);
    }

    private void abrirDashboardAnimal(Animal a) {
        if (a == null) return;
        AnimalDashboardController.mostrar(a);
    }

    private void editarAnimal(Animal a) {
        abrirFormularioAnimal(a);
    }

    private void abrirFormularioAnimal(Animal animalExistente) {
        boolean esNuevo = animalExistente == null;
        int fincaId = Session.getFincaActualId();

        Dialog<Animal> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Registrar Animal" : "Editar Animal");
        dialog.setHeaderText(esNuevo
                ? "Complete los datos del nuevo animal"
                : "Edite los datos del animal " + animalExistente.getArete());

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 24, 10, 24));

        String areteGenerado = esNuevo ? GanaderiaDataStore.generateUniqueArete(fincaId) : animalExistente.getArete();
        TextField fArete = new TextField(areteGenerado);
        fArete.setEditable(false);
        fArete.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0;");
        fArete.setPrefWidth(280);

        Label lblAreteHint = new Label(esNuevo ? "Generado automáticamente — único" : "");
        lblAreteHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");

        TextField fNombre = new TextField(esNuevo ? "" : animalExistente.getNombre());
        fNombre.setPromptText("Nombre del animal *");
        fNombre.setPrefWidth(280);

        ObservableList<String> razas = FXCollections.observableArrayList(
                "Normando", "Holstein", "Brauvión", "Simmental", "Brahman",
                "Angus", "Santa Gertrudis", "Otro");
        ComboBox<String> fRaza = new ComboBox<>(razas);
        fRaza.setPrefWidth(280);

        TextField fRazaCustom = new TextField();
        fRazaCustom.setPromptText("Escriba la raza personalizada");
        fRazaCustom.setPrefWidth(280);
        fRazaCustom.setVisible(false);
        fRazaCustom.setManaged(false);

        if (!esNuevo) {
            String razaActual = animalExistente.getRaza();
            if (razas.contains(razaActual)) {
                fRaza.setValue(razaActual);
            } else {
                fRaza.setValue("Otro");
                fRazaCustom.setText(razaActual);
                fRazaCustom.setVisible(true);
                fRazaCustom.setManaged(true);
            }
        }

        fRaza.valueProperty().addListener((obs, old, val) -> {
            boolean esOtro = "Otro".equals(val);
            fRazaCustom.setVisible(esOtro);
            fRazaCustom.setManaged(esOtro);
            if (!esOtro) fRazaCustom.clear();
        });

        ComboBox<String> fSexo = new ComboBox<>(FXCollections.observableArrayList("Macho", "Hembra"));
        fSexo.setPrefWidth(280);
        if (!esNuevo) fSexo.setValue(animalExistente.getSexo());

        DatePicker fFechaNac = new DatePicker(esNuevo ? LocalDate.now().minusYears(2) : animalExistente.getFechaNacimiento());
        fFechaNac.setPrefWidth(280);

        DatePicker fFechaIng = new DatePicker(esNuevo ? LocalDate.now()
                : (animalExistente.getFechaIngreso() != null ? animalExistente.getFechaIngreso() : LocalDate.now()));
        fFechaIng.setPrefWidth(280);

        TextField fPesoInicial = new TextField(esNuevo ? "" : String.format("%.0f", animalExistente.getPesoInicial()));
        fPesoInicial.setPromptText("Ej: 280");
        fPesoInicial.setPrefWidth(200);
        forzarNumerico(fPesoInicial);

        Label lblKg1 = new Label("kg");
        lblKg1.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1f7a63;");
        HBox pesoIniBox = new HBox(8, fPesoInicial, lblKg1);
        pesoIniBox.setAlignment(Pos.CENTER_LEFT);

        TextField fPesoActual = new TextField(esNuevo ? "" : String.format("%.0f", animalExistente.getPesoActual()));
        fPesoActual.setPromptText("Ej: 385");
        fPesoActual.setPrefWidth(200);
        forzarNumerico(fPesoActual);

        Label lblKg2 = new Label("kg");
        lblKg2.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1f7a63;");
        HBox pesoActBox = new HBox(8, fPesoActual, lblKg2);
        pesoActBox.setAlignment(Pos.CENTER_LEFT);

        Label lblGdpAuto = new Label("Calculado automáticamente desde el historial de pesajes");
        lblGdpAuto.setStyle("-fx-font-size: 11px; -fx-text-fill: #1f7a63; -fx-font-style: italic;");

        ObservableList<Lote> lotes = GanaderiaDataStore.getLotesPorFinca(fincaId);
        ObservableList<String> nombresLotes = FXCollections.observableArrayList();
        lotes.forEach(l -> nombresLotes.add(l.getNombre()));
        ComboBox<String> fLote = new ComboBox<>(nombresLotes);
        fLote.setPrefWidth(280);
        if (!esNuevo) fLote.setValue(animalExistente.getLoteNombre());

        ObservableList<String> estados = FXCollections.observableArrayList("Activo", "Vendido", "Enfermo", "Muerto", "Otro");
        ComboBox<String> fEstadoVida = new ComboBox<>(estados);
        fEstadoVida.setPrefWidth(280);
        fEstadoVida.setValue(esNuevo ? "Activo" : animalExistente.getEstado());

        TextField fObservaciones = new TextField(esNuevo ? "" : animalExistente.getObservaciones());
        fObservaciones.setPrefWidth(280);

        TextField fFotoPath = new TextField(esNuevo ? "" : (animalExistente.getFotoPath() != null ? animalExistente.getFotoPath() : ""));
        fFotoPath.setEditable(false);
        fFotoPath.setPromptText("Sin imagen seleccionada");
        Button btnFoto = new Button("Buscar...");
        btnFoto.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand;");
        btnFoto.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Seleccionar imagen del animal");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.jpeg", "*.png"));
            File f = chooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (f != null) fFotoPath.setText(f.getAbsolutePath());
        });
        HBox fotoBox = new HBox(8, fFotoPath, btnFoto);
        fFotoPath.setPrefWidth(200);
        fotoBox.setAlignment(Pos.CENTER_LEFT);

        // Selector de motor de BD (prompt v4 — usuario elige dónde guardar)
        MotorSelector motorSelector = new MotorSelector();

        int row = 0;
        addFormRow(grid, "Código (arete)", fArete, row++);
        grid.add(lblAreteHint, 1, row++);
        addFormRow(grid, "Nombre *", fNombre, row++);
        addFormRow(grid, "Raza *", fRaza, row++);
        addFormRow(grid, "Raza personalizada", fRazaCustom, row++);
        addFormRow(grid, "Sexo *", fSexo, row++);
        addFormRow(grid, "Fecha nacimiento *", fFechaNac, row++);
        addFormRow(grid, "Fecha ingreso *", fFechaIng, row++);
        addFormRow(grid, "Peso ingreso *", pesoIniBox, row++);
        addFormRow(grid, "Peso actual *", pesoActBox, row++);
        addFormRow(grid, "GDP", lblGdpAuto, row++);
        addFormRow(grid, "Lote *", fLote, row++);
        addFormRow(grid, "Estado", fEstadoVida, row++);
        addFormRow(grid, "Imagen", fotoBox, row++);
        addFormRow(grid, "Observaciones", fObservaciones, row++);
        grid.add(motorSelector.getNode(), 0, row, 2, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(540);
        scroll.setStyle("-fx-background: white; -fx-background-color: white;");

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefWidth(560);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != guardarBtn) return null;

            String nombre = fNombre.getText().trim();
            if (nombre.isEmpty()) {
                NotificationService.warning("Ingrese el nombre del animal");
                return null;
            }

            String raza = fRaza.getValue();
            if (raza == null) {
                NotificationService.warning("Seleccione una raza");
                return null;
            }
            if ("Otro".equals(raza)) {
                raza = fRazaCustom.getText().trim();
                if (raza.isEmpty()) {
                    NotificationService.warning("Ingrese el nombre de la raza personalizada");
                    return null;
                }
            }

            String sexo = fSexo.getValue();
            if (sexo == null) {
                NotificationService.warning("Seleccione el sexo del animal");
                return null;
            }

            String loteNombre = fLote.getValue();
            if (loteNombre == null) {
                NotificationService.warning("Seleccione un lote");
                return null;
            }

            double pesoIni = parsePeso(fPesoInicial.getText());
            double pesoAct = parsePeso(fPesoActual.getText());

            if (pesoIni <= 0) {
                NotificationService.warning("Ingrese el peso de ingreso del animal");
                return null;
            }
            if (pesoAct <= 0) {
                NotificationService.warning("Ingrese el peso actual del animal");
                return null;
            }

            Lote loteObj = lotes.stream().filter(l -> l.getNombre().equals(loteNombre)).findFirst().orElse(null);
            int loteId = loteObj != null ? loteObj.getId() : 0;
            int animalId = esNuevo
                    ? GanaderiaDataStore.getTodosLosAnimales().size() + 100 + (int)(Math.random() * 50)
                    : animalExistente.getId();

            // GDP automático desde fecha ingreso
            LocalDate fechaIng = fFechaIng.getValue() != null ? fFechaIng.getValue() : LocalDate.now();
            long dias = java.time.temporal.ChronoUnit.DAYS.between(fechaIng, LocalDate.now());
            double gdp = (dias > 0 && pesoAct > pesoIni) ? (pesoAct - pesoIni) / (double) dias : 0;

            Animal a = new Animal(animalId, fincaId, loteId, areteGenerado,
                    nombre, raza, sexo,
                    fFechaNac.getValue() != null ? fFechaNac.getValue() : LocalDate.now().minusYears(2),
                    pesoIni, pesoAct, gdp,
                    gdp >= 0.9 ? "Rentable" : gdp >= 0.7 ? "Mantener" : gdp >= 0.5 ? "Bajo" : "No Rentable",
                    fObservaciones.getText().trim(), loteNombre);
            a.setFechaIngreso(fechaIng);
            a.setEstado(fEstadoVida.getValue());
            if (!fFotoPath.getText().isBlank()) a.setFotoPath(fFotoPath.getText());
            a.setFincaNombre(Session.getNombreFincaActual());
            return a;
        });

        // Snapshot del estado anterior (antes de mostrar el dialog) para edición
        final String snapshotAnterior = esNuevo ? null
                : JsonUtil.toJson(JsonUtil.toMap(animalExistente));

        Optional<Animal> result = dialog.showAndWait();
        result.ifPresent(animal -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            String idStr = String.valueOf(animal.getId());
            PersistenceService.Resultado res;
            if (!esNuevo) {
                // Usamos snapshot anterior — el original ya pudo haberse modificado
                BackupService.guardarJson("animales", idStr, snapshotAnterior);
                res = PersistenceService.editar("animales", idStr, animalExistente, animal, motor);
                int idx = animalesData.indexOf(animalExistente);
                if (idx >= 0) animalesData.set(idx, animal);
            } else {
                GanaderiaDataStore.agregarAnimal(fincaId, animal);
                res = PersistenceService.crear("animales", idStr, animal, motor);
            }
            actualizarConteo();
            tablaAnimales.refresh();
            String accion = esNuevo ? "registrado" : "actualizado";
            NotificationService.success("Animal " + animal.getArete() + " " + accion
                    + " en " + motor.getEtiqueta() + " · " + res.getMensaje() + " · copia en MongoDB");
        });
    }

    @FXML
    private void eliminarAnimal() {
        Animal seleccionado = tablaAnimales.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            NotificationService.info("Seleccione un animal de la tabla para eliminar");
            return;
        }
        // Dialog con confirmación + selector de motor de origen
        Dialog<PersistenceService.Motor> dialog = new Dialog<>();
        dialog.setTitle("Eliminar animal");
        dialog.setHeaderText("¿Eliminar " + seleccionado.getArete() + " — " + seleccionado.getNombre() + "?");
        ButtonType ok = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        MotorSelector selector = new MotorSelector();
        VBox box = new VBox(10);
        box.setPadding(new Insets(18));
        Label info = new Label("El registro se eliminará del motor seleccionado.\nUna copia íntegra se conservará SIEMPRE en MongoDB (colección backups).");
        info.setWrapText(true);
        info.setStyle("-fx-text-fill: #92400e; -fx-font-size: 12px;");
        box.getChildren().addAll(info, selector.getNode());
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(b -> b == ok ? selector.getMotorSeleccionado() : null);

        dialog.showAndWait().ifPresent(motor -> {
            PersistenceService.Resultado res = PersistenceService.eliminar(
                    "animales", String.valueOf(seleccionado.getId()), seleccionado, motor);
            animalesData.remove(seleccionado);
            actualizarConteo();
            NotificationService.success("Animal " + seleccionado.getArete() + " eliminado de "
                    + motor.getEtiqueta() + " · respaldo conservado en MongoDB · " + res.getMensaje());
        });
    }

    private void forzarNumerico(TextField field) {
        field.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.matches("[0-9]*\\.?[0-9]*")) {
                field.setText(old);
            }
        });
    }

    private double parsePeso(String text) {
        if (text == null || text.trim().isEmpty()) return -1;
        try { return Double.parseDouble(text.trim()); } catch (NumberFormatException e) { return -1; }
    }

    private void addFormRow(GridPane grid, String label, javafx.scene.Node control, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        grid.add(lbl, 0, row);
        grid.add(control, 1, row);
    }
}
