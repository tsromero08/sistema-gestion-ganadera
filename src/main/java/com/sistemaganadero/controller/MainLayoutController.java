package com.sistemaganadero.controller;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.Finca;
import com.sistemaganadero.model.Lote;
import com.sistemaganadero.model.Notificacion;
import com.sistemaganadero.model.Usuario;
import com.sistemaganadero.service.AlertasCenterService;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.service.NotificationCenter;
import com.sistemaganadero.util.NotificationService;
import com.sistemaganadero.util.Session;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainLayoutController {

    @FXML private Label lblUsuario;
    @FXML private Label lblRol;
    @FXML private ComboBox<Finca> cmbFinca;
    @FXML private StackPane contentPane;
    @FXML private TextField txtBuscarGlobal;

    @FXML private VBox sidebar;
    @FXML private VBox sidebarLogo;
    @FXML private Label lblLogoLine1;
    @FXML private Label lblLogoLine2;
    @FXML private Label lblLogoSub;
    @FXML private Label lblSecPrincipal;
    @FXML private Label lblSecOperaciones;
    @FXML private Label lblSecSistema;
    @FXML private Button btnToggleSidebar;

    @FXML private Button btnInicio;
    @FXML private Button btnAnimales;
    @FXML private Button btnPesajes;
    @FXML private Button btnVacunas;
    @FXML private Button btnSanidadModulo;
    @FXML private Button btnInventario;
    @FXML private Button btnPotreros;
    @FXML private Button btnCultura;
    @FXML private Button btnVentas;
    @FXML private Button btnAlertasModulo;
    @FXML private Button btnReportes;
    @FXML private Button btnConfiguracion;
    @FXML private Button btnCerrarSesion;

    @FXML private Button btnCampana;
    @FXML private Label lblBadgeCampana;

    private List<Button> navButtons;
    private Popup searchPopup;
    private ListView<String> listaResultados;
    private List<Object> resultadosCache;

    private boolean sidebarColapsado = false;
    private static final double SIDEBAR_EXPANDIDO = 220;
    private static final double SIDEBAR_COLAPSADO = 64;

    private record NavEtiqueta(Button boton, String expandido, String colapsado) {}
    private List<NavEtiqueta> etiquetas;

    @FXML
    public void initialize() {
        navButtons = new ArrayList<>(Arrays.asList(
                btnInicio, btnAnimales, btnPesajes, btnVacunas, btnSanidadModulo,
                btnInventario, btnPotreros, btnCultura, btnVentas,
                btnAlertasModulo, btnReportes, btnConfiguracion
        ));
        navButtons.removeIf(b -> b == null);

        etiquetas = List.of(
                new NavEtiqueta(btnInicio,         "🏠  Inicio",            "🏠"),
                new NavEtiqueta(btnAnimales,       "🐄  Animales",          "🐄"),
                new NavEtiqueta(btnPesajes,        "⚖  Pesajes",            "⚖"),
                new NavEtiqueta(btnVacunas,        "💉  Vacunas",           "💉"),
                new NavEtiqueta(btnSanidadModulo,  "🩺  Sanidad",           "🩺"),
                new NavEtiqueta(btnInventario,     "📦  Inventario",        "📦"),
                new NavEtiqueta(btnPotreros,       "🌾  Potreros",          "🌾"),
                new NavEtiqueta(btnCultura,        "🌱  Cultura/Pasturas",  "🌱"),
                new NavEtiqueta(btnVentas,         "🛒  Marketplace",       "🛒"),
                new NavEtiqueta(btnAlertasModulo,  "🔔  Centro de Alertas", "🔔"),
                new NavEtiqueta(btnReportes,       "📊  Reportes",          "📊"),
                new NavEtiqueta(btnConfiguracion,  "⚙  Configuración",     "⚙")
        );

        if (Session.getUsuarioActual() != null) {
            Usuario u = Session.getUsuarioActual();
            lblUsuario.setText(u.getNombre());
            lblRol.setText(u.getRol());
            aplicarVisibilidadPorRol(u);
        }

        cmbFinca.getItems().addAll(GanaderiaDataStore.getFincas());
        cmbFinca.getSelectionModel().selectFirst();
        Finca primera = GanaderiaDataStore.getFincas().isEmpty() ? null : GanaderiaDataStore.getFincas().get(0);
        if (primera != null) Session.setFincaActual(primera);

        cmbFinca.valueProperty().addListener((obs, old, nueva) -> {
            if (nueva != null) {
                Session.setFincaActual(nueva);
                abrirInicio();
            }
        });

        NotificationService.setOverlayPane(contentPane);
        configurarBusquedaGlobal();
        configurarResponsive();

        // Inicializar centro de alertas + notificaciones
        AlertasCenterService.regenerar();
        NotificationCenter.regenerar();
        actualizarBadgeCampana();
        // Refrescar cada 30s sin bloquear UI + chequear sesión vigente
        Timeline refresco = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            if (Session.getUsuarioActual() != null && !Session.sesionVigente()) {
                NotificationService.warning("Tu sesión expiró por inactividad. Vuelve a iniciar sesión.");
                volverAlLogin();
                return;
            }
            AlertasCenterService.regenerar();
            NotificationCenter.regenerar();
            actualizarBadgeCampana();
        }));
        refresco.setCycleCount(Timeline.INDEFINITE);
        refresco.play();

        abrirInicio();
    }

    private void aplicarVisibilidadPorRol(Usuario u) {
        if (u.esAdministrador()) return;
        switch (u.getRolEnum()) {
            case COMPRADOR -> ocultar(btnAnimales, btnPesajes, btnVacunas, btnSanidadModulo,
                    btnInventario, btnPotreros, btnCultura, btnReportes, btnConfiguracion);
            case VENDEDOR_GANADERO -> ocultar(btnConfiguracion);
            case OPERARIO, VETERINARIO -> ocultar(btnConfiguracion);
            default -> { /* sin restricción */ }
        }
    }

    private void ocultar(Button... botones) {
        for (Button b : botones) {
            if (b == null) continue;
            b.setVisible(false);
            b.setManaged(false);
        }
    }

    @FXML
    private void toggleSidebar() {
        sidebarColapsado = !sidebarColapsado;
        double ancho = sidebarColapsado ? SIDEBAR_COLAPSADO : SIDEBAR_EXPANDIDO;
        if (sidebar != null) sidebar.setPrefWidth(ancho);

        // Mostrar/ocultar etiquetas largas
        for (NavEtiqueta ne : etiquetas) {
            if (ne.boton() == null) continue;
            ne.boton().setText(sidebarColapsado ? ne.colapsado() : ne.expandido());
            ne.boton().setAlignment(sidebarColapsado ? Pos.CENTER : Pos.CENTER_LEFT);
        }
        // Ocultar logo expandido
        if (lblLogoLine1 != null) { lblLogoLine1.setVisible(!sidebarColapsado); lblLogoLine1.setManaged(!sidebarColapsado); }
        if (lblLogoLine2 != null) lblLogoLine2.setText(sidebarColapsado ? "SG" : "GANADERO");
        if (lblLogoSub != null)   { lblLogoSub.setVisible(!sidebarColapsado); lblLogoSub.setManaged(!sidebarColapsado); }
        // Ocultar etiquetas de sección
        for (Label l : new Label[]{lblSecPrincipal, lblSecOperaciones, lblSecSistema}) {
            if (l != null) { l.setVisible(!sidebarColapsado); l.setManaged(!sidebarColapsado); }
        }
    }

    /** Auto-colapsa el sidebar cuando la ventana es angosta (tablet / monitor pequeño). */
    private void configurarResponsive() {
        if (contentPane == null) return;
        // Esperamos a que el Scene esté disponible
        contentPane.sceneProperty().addListener((obs, oldS, scene) -> {
            if (scene == null) return;
            scene.widthProperty().addListener((o, a, val) -> ajustarSidebarPorAncho(val.doubleValue()));
            ajustarSidebarPorAncho(scene.getWidth());
        });
    }

    private void ajustarSidebarPorAncho(double ancho) {
        if (sidebar == null) return;
        boolean debeColapsar = ancho < 1100;
        if (debeColapsar && !sidebarColapsado) toggleSidebar();
        else if (!debeColapsar && sidebarColapsado && ancho >= 1280) toggleSidebar();
    }

    private void configurarBusquedaGlobal() {
        if (txtBuscarGlobal == null) return;

        listaResultados = new ListView<>();
        listaResultados.setPrefWidth(380);
        listaResultados.setMaxHeight(280);
        listaResultados.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);"
        );

        searchPopup = new Popup();
        searchPopup.setAutoHide(true);
        searchPopup.getContent().add(listaResultados);

        txtBuscarGlobal.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.trim().length() < 2) {
                searchPopup.hide();
                return;
            }
            buscarGlobal(val.trim().toLowerCase());
        });

        txtBuscarGlobal.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) searchPopup.hide();
        });

        listaResultados.setOnMouseClicked(e -> {
            int idx = listaResultados.getSelectionModel().getSelectedIndex();
            if (idx < 0 || resultadosCache == null || idx >= resultadosCache.size()) return;
            Object seleccionado = resultadosCache.get(idx);
            searchPopup.hide();
            txtBuscarGlobal.clear();
            if (seleccionado instanceof Animal a) {
                AnimalDashboardController.mostrar(a);
            } else if (seleccionado instanceof Lote) {
                abrirAnimales();
            }
        });
    }

    private void buscarGlobal(String query) {
        if (searchPopup == null) return;
        int fincaId = Session.getFincaActualId();
        ObservableList<String> items = FXCollections.observableArrayList();
        resultadosCache = new java.util.ArrayList<>();

        GanaderiaDataStore.getAnimalesPorFinca(fincaId).stream()
                .filter(a -> (a.getArete() != null && a.getArete().toLowerCase().contains(query))
                        || (a.getNombre() != null && a.getNombre().toLowerCase().contains(query))
                        || (a.getRaza() != null && a.getRaza().toLowerCase().contains(query)))
                .limit(8)
                .forEach(a -> {
                    items.add("Animal: " + a.getArete() + " — " + a.getNombre() + " (" + a.calcularEstado() + ")");
                    resultadosCache.add(a);
                });

        GanaderiaDataStore.getLotesPorFinca(fincaId).stream()
                .filter(l -> l.getNombre().toLowerCase().contains(query))
                .limit(4)
                .forEach(l -> {
                    items.add("Lote: " + l.getNombre());
                    resultadosCache.add(l);
                });

        if (items.isEmpty()) {
            items.add("Sin resultados para \"" + query + "\"");
            resultadosCache.add(null);
        }

        listaResultados.setItems(items);
        listaResultados.setPrefHeight(Math.min(items.size() * 38, 280));

        if (!searchPopup.isShowing() && txtBuscarGlobal.getScene() != null) {
            var bounds = txtBuscarGlobal.localToScreen(txtBuscarGlobal.getBoundsInLocal());
            if (bounds != null) {
                searchPopup.show(txtBuscarGlobal, bounds.getMinX(), bounds.getMaxY() + 4);
            }
        }
    }

    private void actualizarBadgeCampana() {
        if (lblBadgeCampana == null) return;
        long noLeidas = NotificationCenter.contarNoLeidas();
        Platform.runLater(() -> {
            if (noLeidas == 0) {
                lblBadgeCampana.setVisible(false);
                lblBadgeCampana.setManaged(false);
            } else {
                lblBadgeCampana.setText(String.valueOf(Math.min(noLeidas, 99)));
                lblBadgeCampana.setVisible(true);
                lblBadgeCampana.setManaged(true);
            }
        });
    }

    @FXML
    private void mostrarNotificaciones() {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        VBox box = new VBox(0);
        box.setPrefWidth(380);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 14, 0, 0, 6);");

        // Header
        HBox header = new HBox(8);
        header.setPadding(new Insets(14, 16, 10, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        Label titulo = new Label("Notificaciones");
        titulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Hyperlink limpiar = new Hyperlink("Marcar todas como leídas");
        limpiar.setStyle("-fx-font-size: 11px; -fx-text-fill: #1f7a63;");
        limpiar.setOnAction(e -> {
            NotificationCenter.marcarTodasLeidas();
            actualizarBadgeCampana();
            popup.hide();
        });
        header.getChildren().addAll(titulo, s, limpiar);
        box.getChildren().add(header);

        ListView<Notificacion> lv = new ListView<>(NotificationCenter.getRecientes(20));
        lv.setPrefHeight(360);
        lv.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        lv.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(Notificacion n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) { setGraphic(null); return; }
                VBox v = new VBox(2);
                v.setPadding(new Insets(10, 14, 10, 14));
                Label asunto = new Label(n.getAsunto());
                asunto.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: "
                        + (n.isLeida() ? "#64748b" : "#0f1f3d") + ";");
                Label mensaje = new Label(n.getMensaje());
                mensaje.setWrapText(true);
                mensaje.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
                Label meta = new Label(n.getCategoria() + " · " + n.getFechaFormateada());
                meta.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
                v.getChildren().addAll(asunto, mensaje, meta);
                if (!n.isLeida()) {
                    v.setStyle("-fx-background-color: #f0f9f6; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
                } else {
                    v.setStyle("-fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                }
                setGraphic(v);
            }
        });
        lv.setOnMouseClicked(e -> {
            Notificacion sel = lv.getSelectionModel().getSelectedItem();
            if (sel != null) {
                sel.setLeida(true);
                actualizarBadgeCampana();
                navegarACategoria(sel.getCategoria());
                popup.hide();
            }
        });
        box.getChildren().add(lv);

        Button verCentro = new Button("Ver centro de alertas completo");
        verCentro.setMaxWidth(Double.MAX_VALUE);
        verCentro.getStyleClass().add("btn-primary-action");
        verCentro.setOnAction(e -> { popup.hide(); abrirAlertas(); });
        VBox.setMargin(verCentro, new Insets(8, 12, 12, 12));
        box.getChildren().add(verCentro);

        popup.getContent().add(box);
        if (btnCampana.getScene() != null) {
            var bounds = btnCampana.localToScreen(btnCampana.getBoundsInLocal());
            if (bounds != null) popup.show(btnCampana, bounds.getMinX() - 280, bounds.getMaxY() + 4);
        }
    }

    private void navegarACategoria(String categoria) {
        if (categoria == null) return;
        switch (categoria.toUpperCase()) {
            case "VACUNAS"      -> abrirVacunas();
            case "SANIDAD"      -> abrirSanidad();
            case "STOCK"        -> abrirInventario();
            case "POTREROS"     -> abrirPotreros();
            case "ALIMENTACION" -> abrirPotreros();
            case "MARKETPLACE"  -> abrirVentas();
            case "VENTAS"       -> abrirVentas();
            case "USUARIOS"     -> abrirConfiguracion();
            case "SISTEMA"      -> abrirAlertas();
            default             -> abrirAlertas();
        }
    }

    @FXML private void abrirInicio()        { activarBoton(btnInicio);        cargarVista("/view/dashboard_home.fxml"); }
    @FXML private void abrirAnimales()      { activarBoton(btnAnimales);      cargarVista("/view/animales.fxml"); }
    @FXML private void abrirPesajes()       { activarBoton(btnPesajes);       cargarVista("/view/pesajes.fxml"); }
    @FXML private void abrirVacunas()       { activarBoton(btnVacunas);       cargarVista("/view/vacunas.fxml"); }
    @FXML private void abrirSanidad()       { activarBoton(btnSanidadModulo); cargarVista("/view/sanidad.fxml"); }
    @FXML private void abrirInventario()    { activarBoton(btnInventario);    cargarVista("/view/inventario.fxml"); }
    @FXML private void abrirPotreros()      { activarBoton(btnPotreros);      cargarVista("/view/potreros.fxml"); }
    @FXML private void abrirCultura()       { activarBoton(btnCultura);       cargarVista("/view/cultura.fxml"); }
    @FXML private void abrirVentas()        { activarBoton(btnVentas);        cargarVista("/view/marketplace.fxml"); }
    @FXML private void abrirAlertas()       { activarBoton(btnAlertasModulo); cargarVista("/view/alertas.fxml"); }
    @FXML private void abrirReportes()      { activarBoton(btnReportes);      cargarVista("/view/reportes.fxml"); }
    @FXML private void abrirConfiguracion() { activarBoton(btnConfiguracion); cargarVista("/view/configuracion.fxml"); }

    @FXML
    private void cerrarSesion() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Deseas cerrar la sesión actual?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Cerrar sesión");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) volverAlLogin();
        });
    }

    private void volverAlLogin() {
        try {
            Session.setUsuarioActual(null);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Sistema Ganadero — Acceso");
            stage.centerOnScreen();
        } catch (Exception e) {
            NotificationService.error("Error al cerrar sesión: " + e.getMessage());
        }
    }

    private void cargarVista(String recurso) {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource(recurso));
            // Conservar marca de agua en index 0 si existe
            if (contentPane.getChildren().size() > 1) {
                contentPane.getChildren().remove(1, contentPane.getChildren().size());
            }
            contentPane.getChildren().add(vista);
        } catch (Exception e) {
            NotificationService.error("Error cargando módulo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void activarBoton(Button seleccionado) {
        Session.registrarActividad();
        for (Button btn : navButtons) {
            btn.getStyleClass().removeAll("nav-selected");
        }
        if (seleccionado != null) seleccionado.getStyleClass().add("nav-selected");
    }
}
