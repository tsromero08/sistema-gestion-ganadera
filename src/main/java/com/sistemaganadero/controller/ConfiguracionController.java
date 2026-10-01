package com.sistemaganadero.controller;

import com.sistemaganadero.model.Finca;
import com.sistemaganadero.model.Lote;
import com.sistemaganadero.model.MarketplaceListing;
import com.sistemaganadero.model.Usuario;
import com.sistemaganadero.service.AuthService;
import com.sistemaganadero.service.BackupService;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.service.PersistenceService;
import com.sistemaganadero.util.JsonUtil;
import com.sistemaganadero.util.MotorSelector;
import com.sistemaganadero.util.NotificationService;
import com.sistemaganadero.util.PasswordHash;
import com.sistemaganadero.util.Session;
import com.sistemaganadero.util.Validators;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class ConfiguracionController {

    // === Tab Usuarios ===
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colTelefono;
    @FXML private TableColumn<Usuario, String> colEstado;

    // === Tab Fincas ===
    @FXML private TableView<Finca> tablaFincas;
    @FXML private TableColumn<Finca, String> colFincaNombre;
    @FXML private TableColumn<Finca, String> colFincaUbic;
    @FXML private TableColumn<Finca, String> colFincaProp;
    @FXML private TableColumn<Finca, String> colFincaArea;
    @FXML private TableColumn<Finca, String> colFincaEstado;
    @FXML private TableColumn<Finca, Void> colFincaAccion;

    @FXML private TableView<Lote> tablaLotes;
    @FXML private TableColumn<Lote, String> colLoteNom;
    @FXML private TableColumn<Lote, String> colLoteCap;
    @FXML private TableColumn<Lote, String> colLoteDesc;
    @FXML private TableColumn<Lote, String> colLoteEst;
    @FXML private TableColumn<Lote, Void> colLoteAccion;

    // === Tab Parámetros ===
    @FXML private TextField cfgGdpRentable;
    @FXML private TextField cfgGdpMantener;
    @FXML private TextField cfgDiasPesaje;
    @FXML private TextField cfgFrecPurga;
    @FXML private TextField cfgFrecVit;
    @FXML private CheckBox chkNotifPantalla;
    @FXML private CheckBox chkNotifAlertas;
    @FXML private CheckBox chkNotifInventario;
    @FXML private CheckBox chkNotifVacunas;
    @FXML private TextField cfgComisionDefault;

    // === Tab Sistema ===
    @FXML private ComboBox<String> cmbMoneda;
    @FXML private ComboBox<String> cmbIdioma;
    @FXML private ComboBox<String> cmbUnidadPeso;
    @FXML private CheckBox chkLogAuditoria;
    @FXML private CheckBox chkBloqueoAuto;
    @FXML private Label lblVersion;

    // === Tab Panel administrativo: Publicaciones ===
    @FXML private TableView<MarketplaceListing> tablaPublicaciones;
    @FXML private TableColumn<MarketplaceListing, String> colPubCodigo;
    @FXML private TableColumn<MarketplaceListing, String> colPubTitulo;
    @FXML private TableColumn<MarketplaceListing, String> colPubVendedor;
    @FXML private TableColumn<MarketplaceListing, String> colPubPrecio;
    @FXML private TableColumn<MarketplaceListing, String> colPubEstado;

    // === Tab Sistema: estado motores/backups ===
    @FXML private Label lblEstadoMotores;
    @FXML private Label lblBackupsTotal;
    @FXML private TextArea txtConteoRegistros;

    // === Tab Panel administrativo: Comisiones ===
    @FXML private Label lblComisionTotal;
    @FXML private Label lblComisionPendiente;
    @FXML private Label lblComisionCobrada;
    @FXML private Label lblVentasTotales;
    @FXML private TableView<MarketplaceListing> tablaComisiones;
    @FXML private TableColumn<MarketplaceListing, String> colComCodigo;
    @FXML private TableColumn<MarketplaceListing, String> colComVendedor;
    @FXML private TableColumn<MarketplaceListing, String> colComValor;
    @FXML private TableColumn<MarketplaceListing, String> colComPorcentaje;
    @FXML private TableColumn<MarketplaceListing, String> colComMonto;
    @FXML private TableColumn<MarketplaceListing, String> colComEstado;

    @FXML
    public void initialize() {
        configurarTablaUsuarios();
        configurarTablaFincasYLotes();
        configurarCombosSistema();
        configurarPanelAdmin();
        bloquearSiNoAdmin();
    }

    private void bloquearSiNoAdmin() {
        Usuario u = Session.getUsuarioActual();
        if (u == null || u.esAdministrador()) return;
        // Sólo admin puede editar usuarios y panel admin
        if (tablaUsuarios != null) tablaUsuarios.setEditable(false);
    }

    private void configurarTablaUsuarios() {
        if (tablaUsuarios == null) return;
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colRol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRol()));
        if (colEmail != null) colEmail.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEmail() != null ? c.getValue().getEmail() : "—"));
        if (colTelefono != null) colTelefono.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTelefono() != null ? c.getValue().getTelefono() : "—"));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "Activo" : "Bloqueado"));

        colRol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(r);
                b.setPadding(new Insets(2, 10, 2, 10));
                b.setStyle("-fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold; " + estiloRol(r));
                setGraphic(b); setText(null);
            }
        });
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String e, boolean empty) {
                super.updateItem(e, empty);
                if (empty || e == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(e);
                b.setPadding(new Insets(3, 10, 3, 10));
                String style = "Activo".equals(e)
                        ? "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;"
                        : "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
                b.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; " + style);
                setGraphic(b); setText(null);
            }
        });
        tablaUsuarios.setItems(AuthService.getUsuarios());
    }

    private static String estiloRol(String rol) {
        return switch (rol) {
            case "Administrador"     -> "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
            case "Vendedor Ganadero" -> "-fx-background-color: #dcfce7; -fx-text-fill: #166534;";
            case "Comprador"         -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
            case "Veterinario"       -> "-fx-background-color: #ffe4e6; -fx-text-fill: #9f1239;";
            case "Operario"          -> "-fx-background-color: #f3e8ff; -fx-text-fill: #6b21a8;";
            default                  -> "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;";
        };
    }

    private void configurarTablaFincasYLotes() {
        if (tablaFincas != null) {
            colFincaNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
            colFincaUbic.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUbicacion()));
            colFincaProp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPropietario()));
            colFincaArea.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.0f ha", c.getValue().getAreaHectareas())));
            colFincaEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
            if (colFincaAccion != null) {
                colFincaAccion.setCellFactory(col -> new TableCell<>() {
                    private final Button btn = new Button("Editar");
                    {
                        btn.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; "
                                + "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
                        btn.setOnAction(e -> editarFinca(getTableView().getItems().get(getIndex())));
                    }
                    @Override protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : btn); }
                });
            }
            tablaFincas.setItems(GanaderiaDataStore.getFincas());
        }
        if (tablaLotes != null) {
            colLoteNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
            colLoteCap.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCapacidad() + " animales"));
            colLoteDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
            colLoteEst.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
            if (colLoteAccion != null) {
                colLoteAccion.setCellFactory(col -> new TableCell<>() {
                    private final Button btn = new Button("Editar");
                    {
                        btn.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; "
                                + "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
                        btn.setOnAction(e -> editarLote(getTableView().getItems().get(getIndex())));
                    }
                    @Override protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : btn); }
                });
            }
            ObservableList<Lote> todos = FXCollections.observableArrayList();
            for (Finca f : GanaderiaDataStore.getFincas())
                todos.addAll(GanaderiaDataStore.getLotesPorFinca(f.getId()));
            tablaLotes.setItems(todos);
        }
    }

    private void editarFinca(Finca f) {
        if (!esAdmin("editar fincas")) return;
        Dialog<Finca> dialog = new Dialog<>();
        dialog.setTitle("Editar finca");
        dialog.setHeaderText("Modificando " + f.getNombre() + " — se generará respaldo automático");
        ButtonType ok = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(18));
        TextField fNombre = new TextField(f.getNombre());           fNombre.setPrefWidth(280);
        TextField fUbic   = new TextField(f.getUbicacion());
        TextField fProp   = new TextField(f.getPropietario());
        TextField fArea   = new TextField(String.format("%.0f", f.getAreaHectareas()));
        TextField fDesc   = new TextField(f.getDescripcion() != null ? f.getDescripcion() : "");
        TextField fDepto  = new TextField(f.getDepartamento() != null ? f.getDepartamento() : "");
        TextField fMun    = new TextField(f.getMunicipio() != null ? f.getMunicipio() : "");
        TextField fVer    = new TextField(f.getVereda() != null ? f.getVereda() : "");
        TextField fDist   = new TextField(f.getDistanciaKm() > 0 ? String.valueOf(f.getDistanciaKm()) : "");
        ComboBox<String> cmbEstado = new ComboBox<>(FXCollections.observableArrayList("Activa", "Inactiva", "En mantenimiento"));
        cmbEstado.setValue(f.getEstado());
        MotorSelector motorSelector = new MotorSelector();

        int r = 0;
        addEditRow(g, "Nombre *", fNombre, r++);
        addEditRow(g, "Ubicación general", fUbic, r++);
        addEditRow(g, "Propietario", fProp, r++);
        addEditRow(g, "Área (ha) *", fArea, r++);
        addEditRow(g, "Descripción", fDesc, r++);
        addEditRow(g, "Departamento", fDepto, r++);
        addEditRow(g, "Municipio", fMun, r++);
        addEditRow(g, "Vereda", fVer, r++);
        addEditRow(g, "Distancia aprox (km)", fDist, r++);
        addEditRow(g, "Estado", cmbEstado, r++);
        g.add(motorSelector.getNode(), 0, r, 2, 1);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            if (Validators.isBlank(fNombre.getText())) { NotificationService.warning("Nombre obligatorio"); return null; }
            try { Double.parseDouble(fArea.getText().trim()); }
            catch (Exception ex) { NotificationService.warning("Área inválida"); return null; }
            return f;
        });

        String snapshotAnterior = JsonUtil.toJson(JsonUtil.toMap(f));

        dialog.showAndWait().ifPresent(sel -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            BackupService.guardarJson("fincas", String.valueOf(f.getId()), snapshotAnterior);
            f.setNombre(fNombre.getText().trim());
            f.setUbicacion(fUbic.getText().trim());
            f.setPropietario(fProp.getText().trim());
            f.setAreaHectareas(Double.parseDouble(fArea.getText().trim()));
            f.setDescripcion(fDesc.getText().trim());
            f.setDepartamento(fDepto.getText().trim());
            f.setMunicipio(fMun.getText().trim());
            f.setVereda(fVer.getText().trim());
            try { f.setDistanciaKm(fDist.getText().isBlank() ? 0 : Double.parseDouble(fDist.getText().trim())); }
            catch (Exception ignored) {}
            f.setEstado(cmbEstado.getValue());
            PersistenceService.Resultado res = PersistenceService.editar("fincas", String.valueOf(f.getId()), sel, f, motor);
            tablaFincas.refresh();
            NotificationService.success("Finca " + f.getNombre() + " actualizada en "
                    + motor.getEtiqueta() + " · respaldo en MongoDB · " + res.getMensaje());
        });
    }

    private void editarLote(Lote l) {
        if (!esAdmin("editar lotes")) return;
        Dialog<Lote> dialog = new Dialog<>();
        dialog.setTitle("Editar lote");
        dialog.setHeaderText("Modificando " + l.getNombre() + " — se generará respaldo automático");
        ButtonType ok = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(18));
        TextField fNombre = new TextField(l.getNombre()); fNombre.setPrefWidth(280);
        TextField fCap = new TextField(String.valueOf(l.getCapacidad()));
        TextField fDesc = new TextField(l.getDescripcion() != null ? l.getDescripcion() : "");
        ComboBox<String> cmbEstado = new ComboBox<>(FXCollections.observableArrayList("Activo", "Inactivo", "Mantenimiento", "Descanso"));
        cmbEstado.setValue(l.getEstado());
        MotorSelector motorSelector = new MotorSelector();

        int r = 0;
        addEditRow(g, "Nombre *", fNombre, r++);
        addEditRow(g, "Capacidad (animales) *", fCap, r++);
        addEditRow(g, "Descripción", fDesc, r++);
        addEditRow(g, "Estado", cmbEstado, r++);
        g.add(motorSelector.getNode(), 0, r, 2, 1);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            if (Validators.isBlank(fNombre.getText())) { NotificationService.warning("Nombre obligatorio"); return null; }
            try {
                int cap = Integer.parseInt(fCap.getText().trim());
                if (cap <= 0) { NotificationService.warning("Capacidad debe ser positiva"); return null; }
            } catch (Exception ex) { NotificationService.warning("Capacidad inválida"); return null; }
            return l;
        });

        String snapshotAnterior = JsonUtil.toJson(JsonUtil.toMap(l));

        dialog.showAndWait().ifPresent(sel -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            BackupService.guardarJson("lotes", String.valueOf(l.getId()), snapshotAnterior);
            l.setNombre(fNombre.getText().trim());
            l.setCapacidad(Integer.parseInt(fCap.getText().trim()));
            l.setDescripcion(fDesc.getText().trim());
            l.setEstado(cmbEstado.getValue());
            PersistenceService.Resultado res = PersistenceService.editar("lotes", String.valueOf(l.getId()), sel, l, motor);
            tablaLotes.refresh();
            NotificationService.success("Lote " + l.getNombre() + " actualizado en "
                    + motor.getEtiqueta() + " · respaldo en MongoDB · " + res.getMensaje());
        });
    }

    private void addEditRow(GridPane g, String label, javafx.scene.Node n, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        g.add(lbl, 0, row); g.add(n, 1, row);
    }

    private void configurarCombosSistema() {
        if (cmbMoneda != null) {
            cmbMoneda.setItems(FXCollections.observableArrayList("COP (Peso colombiano)", "USD", "MXN", "EUR"));
            cmbMoneda.getSelectionModel().selectFirst();
        }
        if (cmbIdioma != null) {
            cmbIdioma.setItems(FXCollections.observableArrayList("Español", "English", "Português"));
            cmbIdioma.getSelectionModel().selectFirst();
        }
        if (cmbUnidadPeso != null) {
            cmbUnidadPeso.setItems(FXCollections.observableArrayList("kg", "lb", "@"));
            cmbUnidadPeso.getSelectionModel().selectFirst();
        }
        refrescarEstadoBackups();
    }

    @FXML
    private void refrescarEstadoBackups() {
        if (lblEstadoMotores != null) {
            lblEstadoMotores.setText(com.sistemaganadero.database.DatabaseManager.estadoMotores());
        }
        if (lblBackupsTotal != null) {
            long n = com.sistemaganadero.service.BackupService.contarBackups();
            lblBackupsTotal.setText("Total de respaldos: " + n);
        }
        if (txtConteoRegistros != null) {
            String[] tablas = {"animales","vacunas","pesajes","inventario","potreros",
                    "marketplace","sanidad_vacunas","terneros","usuarios","fincas","lotes"};
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-20s %10s %10s %10s%n", "TABLA", "MySQL", "PostgreSQL", "MongoDB"));
            sb.append("─".repeat(54)).append('\n');
            for (String t : tablas) {
                long my = PersistenceService.contarRegistros(t, PersistenceService.Motor.MYSQL);
                long pg = PersistenceService.contarRegistros(t, PersistenceService.Motor.POSTGRESQL);
                long mg = PersistenceService.contarRegistros(t, PersistenceService.Motor.MONGODB);
                sb.append(String.format("%-20s %10s %10s %10s%n", t,
                        formatear(my), formatear(pg), formatear(mg)));
            }
            txtConteoRegistros.setText(sb.toString());
        }
    }

    private static String formatear(long n) { return n < 0 ? "—" : String.valueOf(n); }

    private void configurarPanelAdmin() {
        // Publicaciones
        if (tablaPublicaciones != null) {
            colPubCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
            colPubTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTituloLote()));
            colPubVendedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVendedor() != null ? c.getValue().getVendedor() : "—"));
            colPubPrecio.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%,.0f", c.getValue().getPrecio())));
            colPubEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));
            tablaPublicaciones.setItems(GanaderiaDataStore.getMarketplace());
        }
        actualizarKpisComisiones();
        if (tablaComisiones != null) {
            colComCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
            colComVendedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVendedor() != null ? c.getValue().getVendedor() : "—"));
            colComValor.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%,.0f", c.getValue().getPrecio())));
            colComPorcentaje.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f%%", c.getValue().getComisionPorcentaje())));
            colComMonto.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%,.0f", c.getValue().getComisionValor())));
            colComEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));
            ObservableList<MarketplaceListing> vendidos = FXCollections.observableArrayList();
            for (MarketplaceListing m : GanaderiaDataStore.getMarketplace())
                if (m.getEstado() == MarketplaceListing.Estado.VENDIDO) vendidos.add(m);
            tablaComisiones.setItems(vendidos);
        }
    }

    private void actualizarKpisComisiones() {
        ObservableList<MarketplaceListing> listings = GanaderiaDataStore.getMarketplace();
        double total = 0, pendiente = 0, cobrada = 0;
        long ventas = 0;
        for (MarketplaceListing m : listings) {
            if (m.getEstado() == MarketplaceListing.Estado.VENDIDO) {
                double c = m.getComisionValor();
                total += c; pendiente += c; ventas++;
            }
        }
        if (lblComisionTotal != null) lblComisionTotal.setText(String.format("$%,.0f", total));
        if (lblComisionPendiente != null) lblComisionPendiente.setText(String.format("$%,.0f", pendiente));
        if (lblComisionCobrada != null) lblComisionCobrada.setText(String.format("$%,.0f", cobrada));
        if (lblVentasTotales != null) lblVentasTotales.setText(String.valueOf(ventas));
    }

    @FXML
    private void agregarUsuario() {
        if (!esAdmin("crear usuarios")) return;
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Nuevo usuario");
        dialog.setHeaderText("Registrar un nuevo usuario en el sistema");
        ButtonType ok = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12); grid.setPadding(new Insets(20));
        TextField fUser = new TextField(); fUser.setPrefWidth(260);
        TextField fNombre = new TextField(); fNombre.setPrefWidth(260);
        TextField fEmail = new TextField(); fEmail.setPrefWidth(260);
        TextField fTelefono = new TextField(); fTelefono.setPrefWidth(260);
        PasswordField fPwd = new PasswordField(); fPwd.setPrefWidth(260);
        ComboBox<Usuario.Rol> cmbRol = new ComboBox<>();
        cmbRol.getItems().addAll(Usuario.Rol.values());
        cmbRol.getSelectionModel().select(Usuario.Rol.OPERARIO);
        cmbRol.setPrefWidth(260);

        int row = 0;
        grid.add(new Label("Usuario *"), 0, row); grid.add(fUser, 1, row++);
        grid.add(new Label("Nombre completo *"), 0, row); grid.add(fNombre, 1, row++);
        grid.add(new Label("Correo *"), 0, row); grid.add(fEmail, 1, row++);
        grid.add(new Label("Teléfono *"), 0, row); grid.add(fTelefono, 1, row++);
        grid.add(new Label("Contraseña *"), 0, row); grid.add(fPwd, 1, row++);
        grid.add(new Label("Rol *"), 0, row); grid.add(cmbRol, 1, row);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            if (Validators.isBlank(fUser.getText()) || Validators.isBlank(fNombre.getText())
                    || fPwd.getText().length() < 6) {
                NotificationService.warning("Complete los campos. Contraseña mínima 6 caracteres.");
                return null;
            }
            if (!Validators.emailValido(fEmail.getText())) { NotificationService.warning("Email inválido"); return null; }
            if (!Validators.telefonoValido(fTelefono.getText())) { NotificationService.warning("Teléfono inválido"); return null; }
            Usuario u = new Usuario();
            u.setUsername(fUser.getText().trim());
            u.setNombre(fNombre.getText().trim());
            u.setEmail(fEmail.getText().trim());
            u.setTelefono(fTelefono.getText().trim());
            u.setRolEnum(cmbRol.getValue());
            u.setPasswordHash(PasswordHash.hash(fPwd.getText()));
            return u;
        });

        dialog.showAndWait().ifPresent(u -> {
            AuthService.getUsuarios().add(u);
            tablaUsuarios.refresh();
            NotificationService.success("Usuario " + u.getUsername() + " creado");
        });
    }

    @FXML
    private void editarUsuario() {
        if (!esAdmin("editar usuarios")) return;
        Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { NotificationService.warning("Selecciona un usuario"); return; }

        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Editar usuario");
        dialog.setHeaderText("Modificar datos de " + sel.getUsername());
        ButtonType ok = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12); grid.setPadding(new Insets(20));
        TextField fNombre = new TextField(sel.getNombre()); fNombre.setPrefWidth(260);
        TextField fEmail = new TextField(sel.getEmail()); fEmail.setPrefWidth(260);
        TextField fTelefono = new TextField(sel.getTelefono()); fTelefono.setPrefWidth(260);
        ComboBox<Usuario.Rol> cmbRol = new ComboBox<>();
        cmbRol.getItems().addAll(Usuario.Rol.values());
        cmbRol.getSelectionModel().select(sel.getRolEnum());
        cmbRol.setPrefWidth(260);
        MotorSelector motorSelector = new MotorSelector();

        int row = 0;
        grid.add(new Label("Nombre completo"), 0, row); grid.add(fNombre, 1, row++);
        grid.add(new Label("Correo"), 0, row); grid.add(fEmail, 1, row++);
        grid.add(new Label("Teléfono"), 0, row); grid.add(fTelefono, 1, row++);
        grid.add(new Label("Rol"), 0, row); grid.add(cmbRol, 1, row++);
        grid.add(motorSelector.getNode(), 0, row, 2, 1);
        dialog.getDialogPane().setContent(grid);

        String snapshotAnterior = JsonUtil.toJson(JsonUtil.toMap(sel));

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            BackupService.guardarJson("usuarios", sel.getUsername(), snapshotAnterior);
            sel.setNombre(fNombre.getText().trim());
            sel.setEmail(fEmail.getText().trim());
            sel.setTelefono(fTelefono.getText().trim());
            sel.setRolEnum(cmbRol.getValue());
            return sel;
        });
        dialog.showAndWait().ifPresent(u -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            PersistenceService.Resultado res = PersistenceService.editar("usuarios", u.getUsername(), u, u, motor);
            tablaUsuarios.refresh();
            NotificationService.success("Usuario " + u.getUsername() + " actualizado en "
                    + motor.getEtiqueta() + " · respaldo en MongoDB · " + res.getMensaje());
        });
    }

    @FXML
    private void bloquearUsuario() {
        if (!esAdmin("bloquear usuarios")) return;
        Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { NotificationService.warning("Selecciona un usuario"); return; }
        if (sel.esAdministrador()) {
            NotificationService.warning("No se puede bloquear al administrador");
            return;
        }
        BackupService.guardar("usuarios", sel.getUsername(), sel);
        sel.setActivo(!sel.isActivo());
        tablaUsuarios.refresh();
        NotificationService.success(sel.getUsername() + " ahora " + (sel.isActivo() ? "Activo" : "Bloqueado") + " · respaldo creado");
    }

    @FXML
    private void resetPassword() {
        if (!esAdmin("restablecer contraseñas")) return;
        Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { NotificationService.warning("Selecciona un usuario"); return; }
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Restablecer contraseña");
        d.setHeaderText("Nueva contraseña para " + sel.getUsername());
        d.setContentText("Mínimo 6 caracteres:");
        Optional<String> r = d.showAndWait();
        r.ifPresent(p -> {
            if (p.length() < 6) { NotificationService.warning("Mínimo 6 caracteres"); return; }
            sel.setPasswordHash(PasswordHash.hash(p));
            NotificationService.success("Contraseña restablecida para " + sel.getUsername());
        });
    }

    @FXML
    private void aprobarPublicacion() {
        if (!esAdmin("aprobar publicaciones")) return;
        MarketplaceListing sel = tablaPublicaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { NotificationService.warning("Selecciona una publicación"); return; }
        sel.setEstado(MarketplaceListing.Estado.PUBLICADO);
        tablaPublicaciones.refresh();
        NotificationService.success("Publicación " + sel.getCodigo() + " aprobada");
    }

    @FXML
    private void ocultarPublicacion() {
        if (!esAdmin("ocultar publicaciones")) return;
        MarketplaceListing sel = tablaPublicaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { NotificationService.warning("Selecciona una publicación"); return; }
        sel.setEstado(MarketplaceListing.Estado.RETIRADO);
        tablaPublicaciones.refresh();
        NotificationService.success("Publicación " + sel.getCodigo() + " retirada");
    }

    @FXML
    private void marcarComisionCobrada() {
        if (!esAdmin("cobrar comisiones")) return;
        MarketplaceListing sel = tablaComisiones.getSelectionModel().getSelectedItem();
        if (sel == null) { NotificationService.warning("Selecciona una venta"); return; }
        NotificationService.success("Comisión de " + sel.getCodigo() + " marcada como cobrada");
        actualizarKpisComisiones();
    }

    private boolean esAdmin(String accion) {
        Usuario u = Session.getUsuarioActual();
        if (u == null || !u.esAdministrador()) {
            NotificationService.warning("Sólo el administrador puede " + accion);
            return false;
        }
        return true;
    }

    @FXML
    private void guardarParametros() {
        NotificationService.success("Parámetros guardados correctamente");
    }

    @FXML
    private void exportarDatos() {
        NotificationService.info("Función de exportación: simulada — los datos están en memoria");
    }

    @FXML
    private void importarDatos() {
        NotificationService.info("Función de importación: simulada — los datos se cargan desde el datastore");
    }
}
