package com.sistemaganadero.controller;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.Vacuna;
import com.sistemaganadero.service.BackupService;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.service.PersistenceService;
import com.sistemaganadero.util.JsonUtil;
import com.sistemaganadero.util.MotorSelector;
import com.sistemaganadero.util.NotificationService;
import com.sistemaganadero.util.Session;
import com.sistemaganadero.util.Validators;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Optional;

public class VacunasController {

    @FXML private Label lblTotal;
    @FXML private Label lblKpiTotal;
    @FXML private Label lblKpiProximas;
    @FXML private Label lblKpiVencidas;
    @FXML private Label lblKpiSinRegistro;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<Vacuna> tabla;
    @FXML private TableColumn<Vacuna, String> colArete;
    @FXML private TableColumn<Vacuna, String> colAnimal;
    @FXML private TableColumn<Vacuna, String> colVacuna;
    @FXML private TableColumn<Vacuna, String> colDosis;
    @FXML private TableColumn<Vacuna, String> colLote;
    @FXML private TableColumn<Vacuna, String> colFechaApl;
    @FXML private TableColumn<Vacuna, String> colProxima;
    @FXML private TableColumn<Vacuna, String> colResponsable;
    @FXML private TableColumn<Vacuna, String> colEstado;
    @FXML private TableColumn<Vacuna, Void> colAccion;
    @FXML private ListView<String> lstAlertas;

    private ObservableList<Vacuna> vacunas;
    private FilteredList<Vacuna> filtradas;

    @FXML
    public void initialize() {
        cargar();
        configurarColumnas();
        configurarFiltros();
        actualizarKpis();
        recalcularAlertas();
    }

    private void cargar() {
        int fincaId = Session.getFincaActualId();
        vacunas = GanaderiaDataStore.getVacunasPorFinca(fincaId);
        filtradas = new FilteredList<>(vacunas, v -> true);
        tabla.setItems(filtradas);
    }

    private void configurarColumnas() {
        colArete.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArete()));
        colAnimal.setCellValueFactory(c -> {
            Animal a = GanaderiaDataStore.getAnimalById(c.getValue().getAnimalId());
            return new SimpleStringProperty(a != null ? a.getNombre() + " (" + a.getRaza() + ")" : "—");
        });
        colVacuna.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreVacuna()));
        colDosis.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDosis() != null ? c.getValue().getDosis() : "—"));
        colLote.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLote() != null ? c.getValue().getLote() : "—"));
        colFechaApl.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaAplicacion() != null ? c.getValue().getFechaAplicacion().toString() : "—"));
        colProxima.setCellValueFactory(c -> {
            Vacuna v = c.getValue();
            String txt = v.getProximaDosis() != null ? v.getProximaDosis().toString() : "—";
            if (v.getProximaDosis() != null) {
                int dias = v.getDiasRestantes();
                if (dias < 0) txt += "  (vencida hace " + (-dias) + " días)";
                else if (dias <= 7) txt += "  (en " + dias + " días)";
            }
            return new SimpleStringProperty(txt);
        });
        colResponsable.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getResponsable() != null ? c.getValue().getResponsable() : "—"));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(estado);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; " + switch (estado) {
                    case "AL DÍA"   -> "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
                    case "PRÓXIMA"  -> "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
                    case "VENCIDA"  -> "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
                    default          -> "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;";
                });
                setGraphic(badge);
                setText(null);
            }
        });

        if (colAccion != null) {
            colAccion.setCellFactory(col -> new TableCell<>() {
                private final Button btnEditar = new Button("Editar");
                private final HBox box = new HBox(6, btnEditar);
                {
                    btnEditar.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; "
                            + "-fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                    btnEditar.setOnAction(e -> editarVacuna(getTableView().getItems().get(getIndex())));
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : box);
                }
            });
        }
    }

    private void editarVacuna(Vacuna v) {
        Dialog<Vacuna> dialog = new Dialog<>();
        dialog.setTitle("Editar vacuna");
        dialog.setHeaderText("Modificando registro de " + v.getArete() + " — se generará respaldo automático en MongoDB");
        ButtonType ok = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(18));
        TextField fNombre = new TextField(v.getNombreVacuna());      fNombre.setPrefWidth(260);
        TextField fDosis = new TextField(v.getDosis() != null ? v.getDosis() : "");
        TextField fLote = new TextField(v.getLote() != null ? v.getLote() : "");
        DatePicker fFecha = new DatePicker(v.getFechaAplicacion());
        DatePicker fProx = new DatePicker(v.getProximaDosis());
        TextField fResp = new TextField(v.getResponsable() != null ? v.getResponsable() : "");
        TextField fObs = new TextField(v.getObservaciones() != null ? v.getObservaciones() : "");
        MotorSelector motorSelector = new MotorSelector();

        int r = 0;
        addEditRow(g, "Vacuna *", fNombre, r++);
        addEditRow(g, "Dosis", fDosis, r++);
        addEditRow(g, "Lote", fLote, r++);
        addEditRow(g, "Fecha aplicación *", fFecha, r++);
        addEditRow(g, "Próxima dosis", fProx, r++);
        addEditRow(g, "Responsable", fResp, r++);
        addEditRow(g, "Observaciones", fObs, r++);
        g.add(motorSelector.getNode(), 0, r, 2, 1);

        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            if (Validators.isBlank(fNombre.getText())) { NotificationService.warning("Nombre de vacuna obligatorio"); return null; }
            if (fFecha.getValue() == null) { NotificationService.warning("Fecha de aplicación obligatoria"); return null; }
            return v;
        });

        // Snapshot ANTES de aplicar cambios
        String snapshotAnterior = JsonUtil.toJson(JsonUtil.toMap(v));

        dialog.showAndWait().ifPresent(seleccionado -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            BackupService.guardarJson("vacunas", String.valueOf(v.getId()), snapshotAnterior);
            v.setNombreVacuna(fNombre.getText().trim());
            v.setDosis(fDosis.getText().trim());
            v.setLote(fLote.getText().trim());
            v.setFechaAplicacion(fFecha.getValue());
            v.setProximaDosis(fProx.getValue());
            v.setResponsable(fResp.getText().trim());
            v.setObservaciones(fObs.getText().trim());
            PersistenceService.Resultado res = PersistenceService.editar("vacunas",
                    String.valueOf(v.getId()), seleccionado, v, motor);
            tabla.refresh();
            actualizarKpis();
            recalcularAlertas();
            NotificationService.success("Vacuna actualizada en " + motor.getEtiqueta()
                    + " · respaldo en MongoDB · " + res.getMensaje());
        });
    }

    private void addEditRow(GridPane g, String label, javafx.scene.Node n, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        g.add(l, 0, row); g.add(n, 1, row);
    }

    private void configurarFiltros() {
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "AL DÍA", "PRÓXIMA", "VENCIDA"));
        cmbFiltroEstado.getSelectionModel().selectFirst();

        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltro());
        cmbFiltroEstado.valueProperty().addListener((o, a, b) -> aplicarFiltro());
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase().trim();
        String estado = cmbFiltroEstado.getValue();
        filtradas.setPredicate(v -> {
            boolean cText = texto.isEmpty()
                    || (v.getArete() != null && v.getArete().toLowerCase().contains(texto))
                    || (v.getNombreVacuna() != null && v.getNombreVacuna().toLowerCase().contains(texto))
                    || (v.getResponsable() != null && v.getResponsable().toLowerCase().contains(texto));
            boolean cEst = estado == null || estado.equals("Todos") || v.getEstado().equals(estado);
            return cText && cEst;
        });
        lblTotal.setText("Total: " + filtradas.size() + " vacunas");
    }

    private void actualizarKpis() {
        long total = vacunas.size();
        long proximas = vacunas.stream().filter(Vacuna::venceProximamente).count();
        long vencidas = vacunas.stream().filter(Vacuna::estaVencida).count();
        long sinRegistro = animalesSinVacuna();
        if (lblKpiTotal != null) lblKpiTotal.setText(String.valueOf(total));
        if (lblKpiProximas != null) lblKpiProximas.setText(String.valueOf(proximas));
        if (lblKpiVencidas != null) lblKpiVencidas.setText(String.valueOf(vencidas));
        if (lblKpiSinRegistro != null) lblKpiSinRegistro.setText(String.valueOf(sinRegistro));
        lblTotal.setText("Total: " + total + " vacunas");
    }

    private long animalesSinVacuna() {
        int fincaId = Session.getFincaActualId();
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        return animales.stream().filter(a -> vacunas.stream().noneMatch(v -> v.getAnimalId() == a.getId())).count();
    }

    private void recalcularAlertas() {
        if (lstAlertas == null) return;
        ObservableList<String> items = FXCollections.observableArrayList();
        vacunas.stream().filter(Vacuna::estaVencida).forEach(v -> {
            Animal a = GanaderiaDataStore.getAnimalById(v.getAnimalId());
            items.add("⚠ VENCIDA: " + v.getNombreVacuna() + " — " + v.getArete()
                    + (a != null ? " (" + a.getNombre() + ")" : "") + " · vence " + v.getProximaDosis());
        });
        vacunas.stream().filter(Vacuna::venceProximamente).forEach(v -> {
            Animal a = GanaderiaDataStore.getAnimalById(v.getAnimalId());
            items.add("⏰ PRÓXIMA: " + v.getNombreVacuna() + " — " + v.getArete()
                    + (a != null ? " (" + a.getNombre() + ")" : "") + " · en " + v.getDiasRestantes() + " días");
        });
        int fincaId = Session.getFincaActualId();
        GanaderiaDataStore.getAnimalesPorFinca(fincaId).stream()
                .filter(a -> vacunas.stream().noneMatch(v -> v.getAnimalId() == a.getId()))
                .forEach(a -> items.add("✗ SIN REGISTRO: " + a.getArete() + " — " + a.getNombre() + " no tiene vacunas registradas"));
        if (items.isEmpty()) items.add("✓ Todas las vacunas están al día — excelente trabajo");
        lstAlertas.setItems(items);
    }

    @FXML
    private void registrarVacuna() {
        int fincaId = Session.getFincaActualId();
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        if (animales.isEmpty()) {
            NotificationService.warning("No hay animales registrados en la finca actual");
            return;
        }

        Dialog<Vacuna> dialog = new Dialog<>();
        dialog.setTitle("Registrar vacuna");
        dialog.setHeaderText("Aplicación de vacuna a un animal");
        ButtonType ok = new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12); grid.setPadding(new Insets(20));

        ComboBox<Animal> cmbAnimal = new ComboBox<>(animales);
        cmbAnimal.setPrefWidth(280);
        cmbAnimal.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Animal a, boolean empty) {
                super.updateItem(a, empty);
                setText(empty || a == null ? null : a.getArete() + " — " + a.getNombre() + " (" + a.getRaza() + ")");
            }
        });
        cmbAnimal.setButtonCell(cmbAnimal.getCellFactory().call(null));

        ComboBox<String> cmbVacuna = new ComboBox<>(FXCollections.observableArrayList(
                "Aftosa", "Brucela", "Clostridial", "Rabia bovina", "IBR/DVB", "Carbón sintomático", "Leptospira"));
        cmbVacuna.setEditable(true); cmbVacuna.setPrefWidth(280);

        DatePicker dpAplicacion = new DatePicker(LocalDate.now()); dpAplicacion.setPrefWidth(280);
        DatePicker dpProxima = new DatePicker(LocalDate.now().plusMonths(6)); dpProxima.setPrefWidth(280);
        TextField fResponsable = new TextField("Dr. Veterinario"); fResponsable.setPrefWidth(280);
        TextField fDosis = new TextField("1 dosis"); fDosis.setPrefWidth(280);
        TextField fLote = new TextField(); fLote.setPromptText("Lote del fabricante"); fLote.setPrefWidth(280);
        TextField fObs = new TextField(); fObs.setPrefWidth(280);

        MotorSelector motorSelector = new MotorSelector();

        int row = 0;
        addRow(grid, "Animal *", cmbAnimal, row++);
        addRow(grid, "Tipo de vacuna *", cmbVacuna, row++);
        addRow(grid, "Fecha aplicación *", dpAplicacion, row++);
        addRow(grid, "Próxima dosis *", dpProxima, row++);
        addRow(grid, "Responsable *", fResponsable, row++);
        addRow(grid, "Dosis", fDosis, row++);
        addRow(grid, "Lote del fabricante", fLote, row++);
        addRow(grid, "Observaciones", fObs, row++);
        grid.add(motorSelector.getNode(), 0, row, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(540);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            if (cmbAnimal.getValue() == null) { NotificationService.warning("Selecciona un animal"); return null; }
            if (Validators.isBlank(cmbVacuna.getValue())) { NotificationService.warning("Indica el tipo de vacuna"); return null; }
            if (dpAplicacion.getValue() == null) { NotificationService.warning("Fecha de aplicación inválida"); return null; }
            if (dpProxima.getValue() == null) { NotificationService.warning("Fecha próxima inválida"); return null; }
            if (Validators.isBlank(fResponsable.getText())) { NotificationService.warning("Indica el responsable"); return null; }

            Animal a = cmbAnimal.getValue();
            Vacuna v = new Vacuna();
            v.setId((int) (System.currentTimeMillis() % 100000));
            v.setAnimalId(a.getId());
            v.setFincaId(fincaId);
            v.setArete(a.getArete());
            v.setNombreVacuna(cmbVacuna.getValue().trim());
            v.setFechaAplicacion(dpAplicacion.getValue());
            v.setProximaDosis(dpProxima.getValue());
            v.setResponsable(fResponsable.getText().trim());
            v.setDosis(fDosis.getText().trim());
            v.setLote(fLote.getText().trim());
            v.setObservaciones(fObs.getText().trim());
            return v;
        });

        Optional<Vacuna> result = dialog.showAndWait();
        result.ifPresent(v -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            GanaderiaDataStore.agregarVacuna(fincaId, v);
            PersistenceService.Resultado res = PersistenceService.crear("vacunas", String.valueOf(v.getId()), v, motor);
            actualizarKpis();
            recalcularAlertas();
            NotificationService.success("Vacuna " + v.getNombreVacuna() + " registrada para " + v.getArete()
                    + " en " + motor.getEtiqueta() + " · copia en MongoDB · " + res.getMensaje());
        });
    }

    private void addRow(GridPane grid, String label, javafx.scene.Node control, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        grid.add(l, 0, row);
        grid.add(control, 1, row);
    }
}
