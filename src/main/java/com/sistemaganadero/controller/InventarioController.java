package com.sistemaganadero.controller;

import com.sistemaganadero.model.InventarioItem;
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
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class InventarioController {

    private static final ObservableList<String> CATEGORIAS = FXCollections.observableArrayList(
            "Bovinos", "Lotes", "Suplementos", "Veterinaria", "Alimentos",
            "Insumos", "Equipos", "Servicios"
    );

    @FXML private Label lblFinca;
    @FXML private Label lblTotalItems;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private TableView<InventarioItem> tablaInventario;
    @FXML private TableColumn<InventarioItem, String> colCodigo;
    @FXML private TableColumn<InventarioItem, String> colNombre;
    @FXML private TableColumn<InventarioItem, String> colCategoria;
    @FXML private TableColumn<InventarioItem, String> colCantidad;
    @FXML private TableColumn<InventarioItem, String> colUnidad;
    @FXML private TableColumn<InventarioItem, String> colVencim;
    @FXML private TableColumn<InventarioItem, String> colProveedor;
    @FXML private TableColumn<InventarioItem, String> colCosto;
    @FXML private TableColumn<InventarioItem, String> colValor;
    @FXML private TableColumn<InventarioItem, String> colEstado;

    private ObservableList<InventarioItem> items;
    private FilteredList<InventarioItem> filtrados;

    @FXML
    public void initialize() {
        int fincaId = Session.getFincaActualId();
        lblFinca.setText(Session.getNombreFincaActual());

        items = GanaderiaDataStore.getInventarioPorFinca(fincaId);
        filtrados = new FilteredList<>(items, i -> true);

        configurarColumnas();
        tablaInventario.setItems(filtrados);
        actualizarConteo();

        ObservableList<String> filtros = FXCollections.observableArrayList("Todas las categorías");
        filtros.addAll(CATEGORIAS);
        cmbFiltroCategoria.setItems(filtros);
        cmbFiltroCategoria.getSelectionModel().selectFirst();

        txtBuscar.textProperty().addListener((obs, old, val) -> aplicarFiltro());
        cmbFiltroCategoria.valueProperty().addListener((obs, old, val) -> aplicarFiltro());
    }

    private void configurarColumnas() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colCategoria.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoria()));
        colCantidad.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.0f", c.getValue().getCantidad())));
        colUnidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnidad()));
        colVencim.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFechaVencimiento() != null
                        ? c.getValue().getFechaVencimiento().format(fmt) : "—"));
        colProveedor.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getProveedor() != null && !c.getValue().getProveedor().isBlank()
                        ? c.getValue().getProveedor() : "—"));
        colCosto.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%,.0f", c.getValue().getCostoUnitario())));
        colValor.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%,.0f", c.getValue().getValorTotal())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoStock()));

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
                            case "Bajo mínimo" -> "-fx-background-color: #fed7aa; -fx-text-fill: #92400e;";
                            case "Agotado", "Vencido" -> "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
                            default -> "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;";
                        });
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase().trim();
        String cat = cmbFiltroCategoria.getValue();
        filtrados.setPredicate(i -> {
            boolean coincideTexto = texto.isEmpty()
                    || i.getCodigo().toLowerCase().contains(texto)
                    || i.getNombre().toLowerCase().contains(texto)
                    || (i.getProveedor() != null && i.getProveedor().toLowerCase().contains(texto));
            boolean coincideCat = cat == null || cat.equals("Todas las categorías") || i.getCategoria().equals(cat);
            return coincideTexto && coincideCat;
        });
        actualizarConteo();
    }

    private void actualizarConteo() {
        lblTotalItems.setText("Total: " + filtrados.size() + " insumos");
    }

    @FXML
    private void registrarItem() {
        abrirFormulario(null);
    }

    @FXML
    private void editarItem() {
        InventarioItem sel = tablaInventario.getSelectionModel().getSelectedItem();
        if (sel == null) {
            NotificationService.info("Seleccione un insumo de la tabla para editar");
            return;
        }
        abrirFormulario(sel);
    }

    @FXML
    private void eliminarItem() {
        InventarioItem sel = tablaInventario.getSelectionModel().getSelectedItem();
        if (sel == null) {
            NotificationService.info("Seleccione un insumo para eliminar");
            return;
        }
        Dialog<PersistenceService.Motor> dialog = new Dialog<>();
        dialog.setTitle("Eliminar insumo");
        dialog.setHeaderText("¿Eliminar " + sel.getCodigo() + " — " + sel.getNombre() + "?");
        ButtonType ok = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        MotorSelector selector = new MotorSelector();
        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10);
        box.setPadding(new Insets(18));
        Label info = new Label("Se eliminará del motor seleccionado. Una copia íntegra se conservará en MongoDB (backups).");
        info.setWrapText(true); info.setStyle("-fx-text-fill: #92400e; -fx-font-size: 12px;");
        box.getChildren().addAll(info, selector.getNode());
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(b -> b == ok ? selector.getMotorSeleccionado() : null);
        dialog.showAndWait().ifPresent(motor -> {
            PersistenceService.Resultado res = PersistenceService.eliminar("inventario", sel.getCodigo(), sel, motor);
            items.remove(sel);
            actualizarConteo();
            NotificationService.success("Insumo " + sel.getCodigo() + " eliminado de " + motor.getEtiqueta()
                    + " · respaldo en MongoDB · " + res.getMensaje());
        });
    }

    private void abrirFormulario(InventarioItem existente) {
        boolean esNuevo = existente == null;
        int fincaId = Session.getFincaActualId();

        Dialog<InventarioItem> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Nuevo insumo" : "Editar insumo");
        dialog.setHeaderText(esNuevo ? "Registre un nuevo insumo del inventario" : "Edite el insumo " + existente.getCodigo());
        ButtonType ok = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        String codigo = esNuevo ? GanaderiaDataStore.generateInventoryCode(fincaId) : existente.getCodigo();
        TextField fCodigo = new TextField(codigo);
        fCodigo.setEditable(false);
        fCodigo.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #64748b;");
        fCodigo.setPrefWidth(280);

        TextField fNombre = new TextField(esNuevo ? "" : existente.getNombre());
        fNombre.setPrefWidth(280);

        ComboBox<String> fCategoria = new ComboBox<>(CATEGORIAS);
        fCategoria.setEditable(true);
        fCategoria.setPrefWidth(280);
        fCategoria.setPromptText("Seleccione o escriba una categoría");
        if (!esNuevo) fCategoria.setValue(existente.getCategoria());

        TextField fCantidad = new TextField(esNuevo ? "" : String.valueOf(existente.getCantidad()));
        fCantidad.setPrefWidth(280);
        forzarNumerico(fCantidad);

        ComboBox<String> fUnidad = new ComboBox<>(FXCollections.observableArrayList(
                "kg", "bultos", "frascos", "litros", "unidades", "dosis", "cajas", "metros"));
        fUnidad.setEditable(true);
        fUnidad.setPrefWidth(280);
        if (!esNuevo) fUnidad.setValue(existente.getUnidad());

        TextField fMin = new TextField(esNuevo ? "" : String.valueOf(existente.getMinimo()));
        fMin.setPrefWidth(280);
        forzarNumerico(fMin);

        TextField fCosto = new TextField(esNuevo ? "" : String.valueOf(existente.getCostoUnitario()));
        fCosto.setPrefWidth(280);
        forzarNumerico(fCosto);

        DatePicker fIngreso = new DatePicker(esNuevo || existente.getFechaIngreso() == null
                ? LocalDate.now() : existente.getFechaIngreso());
        fIngreso.setPrefWidth(280);

        DatePicker fVenc = new DatePicker(esNuevo ? null : existente.getFechaVencimiento());
        fVenc.setPromptText("Opcional");
        fVenc.setPrefWidth(280);

        TextField fProveedor = new TextField(esNuevo || existente.getProveedor() == null ? "" : existente.getProveedor());
        fProveedor.setPrefWidth(280);

        TextField fObs = new TextField(esNuevo || existente.getObservaciones() == null ? "" : existente.getObservaciones());
        fObs.setPrefWidth(280);

        MotorSelector motorSelector = new MotorSelector();

        int row = 0;
        addFormRow(grid, "Código (auto)", fCodigo, row++);
        addFormRow(grid, "Nombre *", fNombre, row++);
        addFormRow(grid, "Categoría *", fCategoria, row++);
        addFormRow(grid, "Cantidad *", fCantidad, row++);
        addFormRow(grid, "Unidad *", fUnidad, row++);
        addFormRow(grid, "Stock mínimo", fMin, row++);
        addFormRow(grid, "Costo unitario", fCosto, row++);
        addFormRow(grid, "Fecha ingreso", fIngreso, row++);
        addFormRow(grid, "Fecha vencimiento", fVenc, row++);
        addFormRow(grid, "Proveedor", fProveedor, row++);
        addFormRow(grid, "Observaciones", fObs, row++);
        grid.add(motorSelector.getNode(), 0, row, 2, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(560);
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefWidth(560);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            String nombre = fNombre.getText().trim();
            if (nombre.isEmpty()) { NotificationService.warning("Ingrese el nombre del insumo"); return null; }
            String cat = fCategoria.getValue();
            if (cat == null || cat.isBlank()) { NotificationService.warning("Seleccione una categoría"); return null; }
            String unidad = fUnidad.getValue();
            if (unidad == null || unidad.isBlank()) { NotificationService.warning("Seleccione una unidad"); return null; }
            double cant = parseDouble(fCantidad.getText());
            double minimo = parseDouble(fMin.getText());
            double costo = parseDouble(fCosto.getText());
            if (cant < 0) { NotificationService.warning("La cantidad debe ser válida"); return null; }

            InventarioItem item = new InventarioItem(codigo, nombre, cat, cant, unidad, minimo, costo,
                    fIngreso.getValue(), fVenc.getValue(),
                    fProveedor.getText().trim(), fObs.getText().trim());
            return item;
        });

        final String snapshotAnterior = esNuevo ? null : JsonUtil.toJson(JsonUtil.toMap(existente));

        Optional<InventarioItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            PersistenceService.Resultado res;
            if (esNuevo) {
                items.add(item);
                res = PersistenceService.crear("inventario", item.getCodigo(), item, motor);
            } else {
                BackupService.guardarJson("inventario", existente.getCodigo(), snapshotAnterior);
                res = PersistenceService.editar("inventario", existente.getCodigo(), existente, item, motor);
                int idx = items.indexOf(existente);
                if (idx >= 0) items.set(idx, item);
            }
            actualizarConteo();
            tablaInventario.refresh();
            String accion = esNuevo ? "agregado" : "actualizado";
            NotificationService.success("Insumo " + item.getCodigo() + " " + accion + " en "
                    + motor.getEtiqueta() + " · copia en MongoDB · " + res.getMensaje());
        });
    }

    private void addFormRow(GridPane grid, String label, javafx.scene.Node control, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        grid.add(l, 0, row);
        grid.add(control, 1, row);
    }

    private void forzarNumerico(TextField f) {
        f.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.matches("[0-9]*\\.?[0-9]*")) f.setText(old);
        });
    }

    private double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0; }
    }
}
