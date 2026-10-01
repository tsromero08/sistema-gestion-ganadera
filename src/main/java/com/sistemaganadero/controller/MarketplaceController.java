package com.sistemaganadero.controller;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.MarketplaceListing;
import com.sistemaganadero.model.Puja;
import com.sistemaganadero.model.Usuario;
import com.sistemaganadero.service.BackupService;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.service.PersistenceService;
import com.sistemaganadero.util.MotorSelector;
import com.sistemaganadero.util.NotificationService;
import com.sistemaganadero.util.Session;
import com.sistemaganadero.util.Validators;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MarketplaceController {

    @FXML private Label lblTotal;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private ComboBox<String> cmbFiltroRaza;
    @FXML private ComboBox<String> cmbFiltroSexo;
    @FXML private ComboBox<String> cmbFiltroDepartamento;
    @FXML private TextField txtFiltroCiudad;
    @FXML private TextField txtDistanciaMax;
    @FXML private ComboBox<String> cmbFiltroProduccion;
    @FXML private TextField txtPrecioMin;
    @FXML private TextField txtPrecioMax;
    @FXML private TextField txtEdadMin;
    @FXML private TextField txtEdadMax;
    @FXML private TextField txtPesoMin;
    @FXML private TextField txtPesoMax;
    @FXML private ComboBox<String> cmbFiltroSanidad;
    @FXML private CheckBox chkVacunasAlDia;
    @FXML private CheckBox chkVacunasAlDiaSimple;
    @FXML private CheckBox chkConRegistro;
    @FXML private ComboBox<String> cmbFiltroFecha;
    @FXML private ComboBox<String> cmbOrden;
    @FXML private ComboBox<String> cmbOrdenSimple;
    @FXML private ToggleButton btnFiltrosAvanzados;
    @FXML private HBox filaAvanzada1;
    @FXML private HBox filaAvanzada2;
    @FXML private HBox filaSimple;
    @FXML private ToggleButton btnVistaTablaSimple;
    @FXML private ToggleButton btnVistaTarjetasSimple;

    @FXML private StackPane contenedorVista;
    @FXML private ToggleButton btnVistaTabla;
    @FXML private ToggleButton btnVistaTarjetas;
    @FXML private ScrollPane scrollTarjetas;
    @FXML private FlowPane paneTarjetas;
    @FXML private TableView<MarketplaceListing> tabla;
    @FXML private TableColumn<MarketplaceListing, String> colCodigo;
    @FXML private TableColumn<MarketplaceListing, String> colTipo;
    @FXML private TableColumn<MarketplaceListing, String> colTitulo;
    @FXML private TableColumn<MarketplaceListing, String> colAretes;
    @FXML private TableColumn<MarketplaceListing, String> colUbicacion;
    @FXML private TableColumn<MarketplaceListing, String> colVendedor;
    @FXML private TableColumn<MarketplaceListing, String> colPrecio;
    @FXML private TableColumn<MarketplaceListing, String> colComision;
    @FXML private TableColumn<MarketplaceListing, String> colEstado;
    @FXML private TableColumn<MarketplaceListing, Void> colAccion;

    private ObservableList<MarketplaceListing> listings;
    private FilteredList<MarketplaceListing> filtrados;
    private SortedList<MarketplaceListing> ordenados;

    @FXML
    public void initialize() {
        listings = GanaderiaDataStore.getMarketplace();
        filtrados = new FilteredList<>(listings, m -> true);
        ordenados = new SortedList<>(filtrados);

        configurarColumnas();
        tabla.setItems(ordenados);
        actualizarConteo();

        ObservableList<String> estados = FXCollections.observableArrayList("Todos");
        for (var e : MarketplaceListing.Estado.values()) estados.add(e.name());
        cmbFiltroEstado.setItems(estados);
        cmbFiltroEstado.getSelectionModel().selectFirst();

        if (cmbFiltroRaza != null) {
            cmbFiltroRaza.setItems(FXCollections.observableArrayList("Todas", "Normando", "Brauvión", "Holstein", "Simmental", "Angus", "Brahman"));
            cmbFiltroRaza.getSelectionModel().selectFirst();
        }
        if (cmbFiltroSexo != null) {
            cmbFiltroSexo.setItems(FXCollections.observableArrayList("Todos", "Macho", "Hembra", "Mixto"));
            cmbFiltroSexo.getSelectionModel().selectFirst();
        }
        if (cmbFiltroDepartamento != null) {
            cmbFiltroDepartamento.setItems(FXCollections.observableArrayList(
                    "Todos", "Cundinamarca", "Córdoba", "Antioquia", "Meta", "Boyacá", "Atlántico", "Magdalena", "Caquetá"));
            cmbFiltroDepartamento.getSelectionModel().selectFirst();
        }
        if (cmbFiltroProduccion != null) {
            cmbFiltroProduccion.setItems(FXCollections.observableArrayList(
                    "Todos", "Carne", "Leche", "Doble propósito", "Reproducción", "Levante", "Ceba", "Cría"));
            cmbFiltroProduccion.getSelectionModel().selectFirst();
        }
        ObservableList<String> ordenItems = FXCollections.observableArrayList(
                "Más recientes", "Más cercanos", "Menor precio", "Mayor precio",
                "Mayor peso", "Mejor relación peso/precio", "Más vistos", "Más ofertados");
        if (cmbOrden != null) {
            cmbOrden.setItems(ordenItems);
            cmbOrden.getSelectionModel().selectFirst();
        }
        if (cmbOrdenSimple != null) {
            cmbOrdenSimple.setItems(FXCollections.observableArrayList(ordenItems));
            cmbOrdenSimple.getSelectionModel().selectFirst();
        }

        if (cmbFiltroSanidad != null) {
            cmbFiltroSanidad.setItems(FXCollections.observableArrayList(
                    "Cualquiera", "Saludable", "Observación", "En tratamiento"));
            cmbFiltroSanidad.getSelectionModel().selectFirst();
        }
        if (cmbFiltroFecha != null) {
            cmbFiltroFecha.setItems(FXCollections.observableArrayList(
                    "Cualquier fecha", "Últimos 7 días", "Últimos 30 días", "Últimos 90 días"));
            cmbFiltroFecha.getSelectionModel().selectFirst();
        }

        // Listeners filtros básicos
        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltro());
        cmbFiltroEstado.valueProperty().addListener((o, a, b) -> aplicarFiltro());
        if (cmbFiltroRaza != null) cmbFiltroRaza.valueProperty().addListener((o, a, b) -> aplicarFiltro());
        if (cmbFiltroSexo != null) cmbFiltroSexo.valueProperty().addListener((o, a, b) -> aplicarFiltro());
        if (cmbFiltroDepartamento != null) cmbFiltroDepartamento.valueProperty().addListener((o, a, b) -> aplicarFiltro());
        if (txtFiltroCiudad != null) txtFiltroCiudad.textProperty().addListener((o, a, b) -> aplicarFiltro());
        if (txtDistanciaMax != null) txtDistanciaMax.textProperty().addListener((o, a, b) -> aplicarFiltro());
        if (cmbFiltroProduccion != null) cmbFiltroProduccion.valueProperty().addListener((o, a, b) -> aplicarFiltro());
        if (txtPrecioMin != null) txtPrecioMin.textProperty().addListener((o, a, b) -> aplicarFiltro());
        if (txtPrecioMax != null) txtPrecioMax.textProperty().addListener((o, a, b) -> aplicarFiltro());

        // Listeners filtros avanzados
        if (txtEdadMin != null) txtEdadMin.textProperty().addListener((o, a, b) -> aplicarFiltro());
        if (txtEdadMax != null) txtEdadMax.textProperty().addListener((o, a, b) -> aplicarFiltro());
        if (txtPesoMin != null) txtPesoMin.textProperty().addListener((o, a, b) -> aplicarFiltro());
        if (txtPesoMax != null) txtPesoMax.textProperty().addListener((o, a, b) -> aplicarFiltro());
        if (cmbFiltroSanidad != null) cmbFiltroSanidad.valueProperty().addListener((o, a, b) -> aplicarFiltro());
        if (chkVacunasAlDia != null) chkVacunasAlDia.selectedProperty().addListener((o, a, b) -> {
            if (chkVacunasAlDiaSimple != null) chkVacunasAlDiaSimple.setSelected(b);
            aplicarFiltro();
        });
        if (chkVacunasAlDiaSimple != null) chkVacunasAlDiaSimple.selectedProperty().addListener((o, a, b) -> {
            if (chkVacunasAlDia != null) chkVacunasAlDia.setSelected(b);
            aplicarFiltro();
        });
        if (chkConRegistro != null) chkConRegistro.selectedProperty().addListener((o, a, b) -> aplicarFiltro());
        if (cmbFiltroFecha != null) cmbFiltroFecha.valueProperty().addListener((o, a, b) -> aplicarFiltro());

        // Orden sincronizado
        if (cmbOrden != null) cmbOrden.valueProperty().addListener((o, a, b) -> {
            if (cmbOrdenSimple != null && !java.util.Objects.equals(cmbOrdenSimple.getValue(), b)) cmbOrdenSimple.setValue(b);
            aplicarOrden();
        });
        if (cmbOrdenSimple != null) cmbOrdenSimple.valueProperty().addListener((o, a, b) -> {
            if (cmbOrden != null && !java.util.Objects.equals(cmbOrden.getValue(), b)) cmbOrden.setValue(b);
            aplicarOrden();
        });

        // Sincronizar toggles de vista
        if (btnVistaTablaSimple != null) btnVistaTablaSimple.selectedProperty().bindBidirectional(btnVistaTabla.selectedProperty());
        if (btnVistaTarjetasSimple != null) btnVistaTarjetasSimple.selectedProperty().bindBidirectional(btnVistaTarjetas.selectedProperty());

        // Sincronizar tarjetas cuando cambie el filtro/orden
        ordenados.addListener((javafx.collections.ListChangeListener<MarketplaceListing>) ch -> renderTarjetas());
        renderTarjetas();
    }

    @FXML
    private void cambiarAVistaTabla() {
        if (btnVistaTarjetas != null) btnVistaTarjetas.setSelected(false);
        if (btnVistaTabla != null) btnVistaTabla.setSelected(true);
        if (tabla != null) { tabla.setVisible(true); tabla.setManaged(true); }
        if (scrollTarjetas != null) { scrollTarjetas.setVisible(false); scrollTarjetas.setManaged(false); }
    }

    @FXML
    private void cambiarAVistaTarjetas() {
        if (btnVistaTabla != null) btnVistaTabla.setSelected(false);
        if (btnVistaTarjetas != null) btnVistaTarjetas.setSelected(true);
        if (tabla != null) { tabla.setVisible(false); tabla.setManaged(false); }
        if (scrollTarjetas != null) { scrollTarjetas.setVisible(true); scrollTarjetas.setManaged(true); }
        renderTarjetas();
    }

    private void renderTarjetas() {
        if (paneTarjetas == null) return;
        paneTarjetas.getChildren().clear();
        for (MarketplaceListing m : ordenados) paneTarjetas.getChildren().add(tarjetaListing(m));
    }

    private VBox tarjetaListing(MarketplaceListing m) {
        VBox card = new VBox(8);
        card.setPrefWidth(290);
        card.setPadding(new Insets(0));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 14; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 3); -fx-cursor: hand;");

        // Imagen estilizada (gradiente por raza)
        StackPane imagen = new StackPane();
        imagen.setPrefSize(290, 140);
        imagen.setStyle("-fx-background-color: " + gradientePorRaza(m.getRaza())
                + "; -fx-background-radius: 14 14 0 0;");
        Label icono = new Label("🐄");
        icono.setStyle("-fx-font-size: 60px;");
        imagen.getChildren().add(icono);

        // Badge estado
        Label badge = new Label(m.getEstado().name());
        badge.setStyle("-fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 10 4 10; "
                + estiloEstado(m.getEstado().name()));
        StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(10, 10, 0, 0));
        imagen.getChildren().add(badge);

        VBox cuerpo = new VBox(6);
        cuerpo.setPadding(new Insets(14, 14, 14, 14));
        Label titulo = new Label(m.getTituloLote() != null ? m.getTituloLote() : "Sin título");
        titulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titulo.setWrapText(true);

        Label ubic = new Label("📍  " + (m.getUbicacion() != null ? m.getUbicacion() : "Sin ubicación")
                + (m.getDistanciaKm() > 0 ? String.format(" · %.0f km", m.getDistanciaKm()) : ""));
        ubic.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        Label datos = new Label((m.getRaza() != null ? m.getRaza() : "—") + "  ·  "
                + (m.getSexo() != null ? m.getSexo() : "—") + "  ·  "
                + (m.getEdadMeses() > 0 ? m.getEdadMeses() + "m" : "?")
                + (m.getPesoTotal() > 0 ? "  ·  " + String.format("%.0f kg", m.getPesoTotal()) : ""));
        datos.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");

        Label precio = new Label(String.format("$%,.0f", m.getPrecio()));
        precio.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f7a63;");

        Label comision = new Label(String.format("Comisión %.1f%%  ·  Vistas: %d  ·  Ofertas: %d",
                m.getComisionPorcentaje(), m.getTotalVistas(), m.getTotalOfertas()));
        comision.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");

        Label vendedor = new Label("👤 " + (m.getVendedor() != null ? m.getVendedor() : "—"));
        vendedor.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");

        HBox acciones = new HBox(6);
        Button btnVer = new Button("Ver ficha");
        btnVer.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6;");
        btnVer.setOnAction(e -> verDetalle(m));
        Button btnPujar = new Button("Pujar");
        btnPujar.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6;");
        btnPujar.setOnAction(e -> pujar(m));
        acciones.getChildren().addAll(btnVer, btnPujar);
        if (puedeVerPrecioMinimo()) {
            Button btnGes = new Button("Gestionar");
            btnGes.setStyle("-fx-background-color: #ddd6fe; -fx-text-fill: #5b21b6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6;");
            btnGes.setOnAction(e -> gestionar(m));
            acciones.getChildren().add(btnGes);
        }

        cuerpo.getChildren().addAll(titulo, ubic, datos, precio, comision, vendedor, acciones);
        card.getChildren().addAll(imagen, cuerpo);
        card.setOnMouseClicked(e -> { if (e.getClickCount() == 2) verDetalle(m); });
        return card;
    }

    private static String gradientePorRaza(String raza) {
        if (raza == null) return "linear-gradient(to bottom right, #d1fae5, #1f7a63)";
        return switch (raza.toLowerCase()) {
            case "normando"   -> "linear-gradient(to bottom right, #fef3c7, #92400e)";
            case "holstein"   -> "linear-gradient(to bottom right, #f3f4f6, #1e293b)";
            case "brahman"    -> "linear-gradient(to bottom right, #fde68a, #d97706)";
            case "angus"      -> "linear-gradient(to bottom right, #1e293b, #0f1f3d)";
            case "simmental"  -> "linear-gradient(to bottom right, #fef3c7, #b45309)";
            case "brauvión", "brauvion" -> "linear-gradient(to bottom right, #fed7aa, #7c2d12)";
            default            -> "linear-gradient(to bottom right, #d1fae5, #1f7a63)";
        };
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo() != null ? c.getValue().getTipo().name() : "—"));
        colTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTituloLote()));
        colAretes.setCellValueFactory(c -> new SimpleStringProperty(String.join(", ", c.getValue().getAretes())));
        colUbicacion.setCellValueFactory(c -> {
            MarketplaceListing m = c.getValue();
            String distancia = m.getDistanciaKm() > 0 ? String.format(" (%.0f km)", m.getDistanciaKm()) : "";
            String texto = (m.getUbicacion() != null ? m.getUbicacion() : "—") + distancia;
            return new SimpleStringProperty(texto);
        });
        colVendedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVendedor() != null ? c.getValue().getVendedor() : "—"));
        colPrecio.setCellValueFactory(c -> {
            MarketplaceListing m = c.getValue();
            String texto = String.format("$%,.0f", m.getPrecio());
            if (puedeVerPrecioMinimo() && m.getPrecioMinimo() > 0) {
                texto += String.format("  ⓘ mín: $%,.0f", m.getPrecioMinimo());
            }
            return new SimpleStringProperty(texto);
        });
        colComision.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f%%", c.getValue().getComisionPorcentaje())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(estado);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; " + estiloEstado(estado));
                setGraphic(badge);
                setText(null);
            }
        });

        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("Ver");
            private final Button btnPujar = new Button("Pujar");
            private final Button btnGestionar = new Button("Gestionar");
            private final HBox box = new HBox(4);
            {
                btnVer.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                btnPujar.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                btnGestionar.setStyle("-fx-background-color: #ddd6fe; -fx-text-fill: #5b21b6; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
                box.setAlignment(Pos.CENTER_LEFT);
                btnVer.setOnAction(e -> verDetalle(getTableView().getItems().get(getIndex())));
                btnPujar.setOnAction(e -> pujar(getTableView().getItems().get(getIndex())));
                btnGestionar.setOnAction(e -> gestionar(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                box.getChildren().setAll(btnVer, btnPujar);
                if (puedeVerPrecioMinimo()) box.getChildren().add(btnGestionar);
                setGraphic(box);
            }
        });
    }

    private static String estiloEstado(String estado) {
        return switch (estado) {
            case "PUBLICADO", "RECIBIENDO_OFERTAS"     -> "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
            case "OFERTA_RECIBIDA", "OFERTA_ACEPTABLE" -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
            case "OFERTA_BAJA"                         -> "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
            case "NEGOCIANDO", "EN_NEGOCIACION"        -> "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
            case "RESERVADO"                           -> "-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1;";
            case "VENDIDO"                             -> "-fx-background-color: #dcfce7; -fx-text-fill: #166534;";
            case "CANCELADO", "RETIRADO"               -> "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;";
            default                                    -> "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;";
        };
    }

    private boolean puedeVerPrecioMinimo() {
        Usuario u = Session.getUsuarioActual();
        return u != null && u.puedeVerPrecioMinimo();
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase().trim();
        String estado = cmbFiltroEstado.getValue();
        String raza = cmbFiltroRaza != null ? cmbFiltroRaza.getValue() : "Todas";
        String sexo = cmbFiltroSexo != null ? cmbFiltroSexo.getValue() : "Todos";
        String depto = cmbFiltroDepartamento != null ? cmbFiltroDepartamento.getValue() : "Todos";
        String ciudad = txtFiltroCiudad != null && txtFiltroCiudad.getText() != null ? txtFiltroCiudad.getText().toLowerCase().trim() : "";
        String prod = cmbFiltroProduccion != null ? cmbFiltroProduccion.getValue() : "Todos";
        String sanidad = cmbFiltroSanidad != null ? cmbFiltroSanidad.getValue() : "Cualquiera";
        String fechaRango = cmbFiltroFecha != null ? cmbFiltroFecha.getValue() : "Cualquier fecha";
        double pMin = parseOr(txtPrecioMin, 0);
        double pMax = parseOr(txtPrecioMax, Double.MAX_VALUE);
        double distMax = parseOr(txtDistanciaMax, Double.MAX_VALUE);
        int edadMin = (int) parseOr(txtEdadMin, 0);
        int edadMax = (int) parseOr(txtEdadMax, Integer.MAX_VALUE);
        double pesoMin = parseOr(txtPesoMin, 0);
        double pesoMax = parseOr(txtPesoMax, Double.MAX_VALUE);
        boolean soloVacunas = (chkVacunasAlDia != null && chkVacunasAlDia.isSelected())
                || (chkVacunasAlDiaSimple != null && chkVacunasAlDiaSimple.isSelected());
        boolean soloRegistro = chkConRegistro != null && chkConRegistro.isSelected();

        java.time.LocalDate corteFecha = switch (fechaRango == null ? "" : fechaRango) {
            case "Últimos 7 días"  -> java.time.LocalDate.now().minusDays(7);
            case "Últimos 30 días" -> java.time.LocalDate.now().minusDays(30);
            case "Últimos 90 días" -> java.time.LocalDate.now().minusDays(90);
            default -> null;
        };

        filtrados.setPredicate(m -> {
            boolean cText = texto.isEmpty()
                    || m.getCodigo().toLowerCase().contains(texto)
                    || (m.getVendedor() != null && m.getVendedor().toLowerCase().contains(texto))
                    || (m.getUbicacion() != null && m.getUbicacion().toLowerCase().contains(texto))
                    || (m.getTituloLote() != null && m.getTituloLote().toLowerCase().contains(texto));
            boolean cEst = estado == null || estado.equals("Todos") || m.getEstado().name().equals(estado);
            boolean cRaza = raza == null || raza.equals("Todas") || (m.getRaza() != null && m.getRaza().contains(raza));
            boolean cSexo = sexo == null || sexo.equals("Todos") || sexo.equalsIgnoreCase(m.getSexo());
            boolean cDepto = depto == null || depto.equals("Todos") || depto.equalsIgnoreCase(m.getDepartamento());
            boolean cCiudad = ciudad.isEmpty() || (m.getCiudad() != null && m.getCiudad().toLowerCase().contains(ciudad));
            boolean cProd = prod == null || prod.equals("Todos") || prod.equalsIgnoreCase(m.getTipoProduccion());
            boolean cPrecio = m.getPrecio() >= pMin && m.getPrecio() <= pMax;
            boolean cDist = m.getDistanciaKm() == 0 || m.getDistanciaKm() <= distMax;
            boolean cEdad = m.getEdadMeses() >= edadMin && m.getEdadMeses() <= edadMax;
            boolean cPeso = m.getPesoTotal() >= pesoMin && m.getPesoTotal() <= pesoMax;
            boolean cSan = sanidad == null || sanidad.equals("Cualquiera")
                    || (m.getEstadoSanitario() != null && m.getEstadoSanitario().equalsIgnoreCase(sanidad));
            boolean cVac = !soloVacunas || m.isVacunasAlDia();
            boolean cReg = !soloRegistro || m.isConRegistro();
            boolean cFecha = corteFecha == null || (m.getFechaPublicacion() != null && !m.getFechaPublicacion().isBefore(corteFecha));
            return cText && cEst && cRaza && cSexo && cDepto && cCiudad && cProd
                    && cPrecio && cDist && cEdad && cPeso && cSan && cVac && cReg && cFecha;
        });
        actualizarConteo();
    }

    private double parseOr(TextField t, double fallback) {
        if (t == null || t.getText() == null || t.getText().isBlank()) return fallback;
        try { return Double.parseDouble(t.getText().trim()); } catch (NumberFormatException e) { return fallback; }
    }

    @FXML
    private void toggleFiltrosAvanzados() {
        boolean activo = btnFiltrosAvanzados != null && btnFiltrosAvanzados.isSelected();
        toggleNodo(filaAvanzada1, activo);
        toggleNodo(filaAvanzada2, activo);
        toggleNodo(filaSimple, !activo);
    }

    private void toggleNodo(javafx.scene.Node n, boolean visible) {
        if (n == null) return;
        n.setVisible(visible);
        n.setManaged(visible);
    }

    @FXML
    private void limpiarFiltros() {
        if (txtBuscar != null) txtBuscar.clear();
        if (cmbFiltroEstado != null) cmbFiltroEstado.getSelectionModel().selectFirst();
        if (cmbFiltroRaza != null) cmbFiltroRaza.getSelectionModel().selectFirst();
        if (cmbFiltroSexo != null) cmbFiltroSexo.getSelectionModel().selectFirst();
        if (cmbFiltroDepartamento != null) cmbFiltroDepartamento.getSelectionModel().selectFirst();
        if (txtFiltroCiudad != null) txtFiltroCiudad.clear();
        if (cmbFiltroProduccion != null) cmbFiltroProduccion.getSelectionModel().selectFirst();
        if (cmbFiltroSanidad != null) cmbFiltroSanidad.getSelectionModel().selectFirst();
        if (cmbFiltroFecha != null) cmbFiltroFecha.getSelectionModel().selectFirst();
        for (TextField t : new TextField[]{txtPrecioMin, txtPrecioMax, txtDistanciaMax,
                txtEdadMin, txtEdadMax, txtPesoMin, txtPesoMax}) {
            if (t != null) t.clear();
        }
        if (chkVacunasAlDia != null) chkVacunasAlDia.setSelected(false);
        if (chkVacunasAlDiaSimple != null) chkVacunasAlDiaSimple.setSelected(false);
        if (chkConRegistro != null) chkConRegistro.setSelected(false);
        aplicarFiltro();
        NotificationService.info("Filtros restablecidos.");
    }

    private void aplicarOrden() {
        if (cmbOrden == null || cmbOrden.getValue() == null) return;
        Comparator<MarketplaceListing> cmp = switch (cmbOrden.getValue()) {
            case "Más cercanos"            -> Comparator.comparingDouble(MarketplaceListing::getDistanciaKm);
            case "Menor precio"            -> Comparator.comparingDouble(MarketplaceListing::getPrecio);
            case "Mayor precio"            -> Comparator.comparingDouble(MarketplaceListing::getPrecio).reversed();
            case "Mayor peso"              -> Comparator.comparingDouble(MarketplaceListing::getPesoTotal).reversed();
            case "Mejor relación peso/precio" -> Comparator.comparingDouble((MarketplaceListing m) ->
                    m.getPrecio() > 0 ? m.getPesoTotal() / m.getPrecio() : 0).reversed();
            case "Más vistos"              -> Comparator.comparingInt(MarketplaceListing::getTotalVistas).reversed();
            case "Más ofertados"           -> Comparator.comparingInt(MarketplaceListing::getTotalOfertas).reversed();
            default                        -> Comparator.comparing(MarketplaceListing::getFechaPublicacion).reversed();
        };
        ordenados.setComparator(cmp);
    }

    private void actualizarConteo() {
        lblTotal.setText("Total: " + filtrados.size() + " publicaciones");
    }

    @FXML
    private void publicarIndividual() { publicar(MarketplaceListing.Tipo.INDIVIDUAL); }

    @FXML
    private void publicarLote()       { publicar(MarketplaceListing.Tipo.LOTE); }

    private void publicar(MarketplaceListing.Tipo tipo) {
        int fincaId = Session.getFincaActualId();
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        if (animales.isEmpty()) {
            NotificationService.warning("No hay animales registrados en la finca actual");
            return;
        }

        Dialog<MarketplaceListing> dialog = new Dialog<>();
        dialog.setTitle(tipo == MarketplaceListing.Tipo.INDIVIDUAL ? "Publicar animal" : "Publicar lote");
        dialog.setHeaderText("Publicación pública en el Marketplace · Soporte: 301 389 6861");

        ButtonType ok = new ButtonType("Publicar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12); grid.setPadding(new Insets(20));

        ListView<Animal> lvAnimales = new ListView<>();
        lvAnimales.setItems(animales);
        lvAnimales.setPrefHeight(160);
        lvAnimales.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Animal a, boolean empty) {
                super.updateItem(a, empty);
                setText(empty || a == null ? null : a.getArete() + " — " + a.getNombre()
                        + " (" + a.getRaza() + ", " + String.format("%.0f kg", a.getPesoActual()) + ")");
            }
        });
        lvAnimales.getSelectionModel().setSelectionMode(
                tipo == MarketplaceListing.Tipo.INDIVIDUAL ? SelectionMode.SINGLE : SelectionMode.MULTIPLE);

        TextField fTitulo = new TextField();
        fTitulo.setPromptText(tipo == MarketplaceListing.Tipo.INDIVIDUAL
                ? "Ej: Toro Normando 450kg" : "Ej: Lote levante — 5 animales");
        TextField fResumen = new TextField(); fResumen.setPromptText("Breve descripción comercial");
        TextField fPrecioDeseado = new TextField(); fPrecioDeseado.setPromptText("Precio deseado (público)");
        TextField fPrecioMinimo = new TextField(); fPrecioMinimo.setPromptText("Precio mínimo (privado)");
        TextField fComision = new TextField("3.0");

        ComboBox<String> cmbDepto = new ComboBox<>();
        cmbDepto.setItems(FXCollections.observableArrayList("Cundinamarca","Córdoba","Antioquia","Meta","Boyacá","Atlántico","Magdalena","Caquetá"));
        cmbDepto.setEditable(true);
        TextField fCiudad = new TextField(); fCiudad.setPromptText("Ciudad / municipio");
        TextField fDistancia = new TextField("0"); fDistancia.setPromptText("Distancia aprox (km)");

        ComboBox<String> cmbProd = new ComboBox<>();
        cmbProd.setItems(FXCollections.observableArrayList("Carne","Leche","Doble propósito","Reproducción","Levante","Ceba","Cría"));
        cmbProd.getSelectionModel().selectFirst();

        TextField fVendedor = new TextField(Session.getUsuarioActual() != null ? Session.getUsuarioActual().getNombre() : "");
        TextField fContacto = new TextField();
        if (Session.getUsuarioActual() != null && Session.getUsuarioActual().getTelefono() != null)
            fContacto.setText(Session.getUsuarioActual().getTelefono());
        fContacto.setPromptText("WhatsApp / teléfono / email");
        CheckBox chkVacunas = new CheckBox("Vacunas al día");
        chkVacunas.setSelected(true);
        CheckBox chkRegistro = new CheckBox("Tiene registro genealógico / ICA");
        TextField fObs = new TextField();

        for (TextField t : new TextField[]{fTitulo, fResumen, fPrecioDeseado, fPrecioMinimo,
                fComision, fCiudad, fDistancia, fVendedor, fContacto, fObs}) t.setPrefWidth(310);
        cmbDepto.setPrefWidth(310); cmbProd.setPrefWidth(310);

        for (TextField numerico : new TextField[]{fPrecioDeseado, fPrecioMinimo, fComision, fDistancia}) {
            numerico.textProperty().addListener((o, a, val) -> {
                if (val != null && !val.matches("[0-9]*\\.?[0-9]*")) numerico.setText(a);
            });
        }

        int row = 0;
        addRow(grid, "Animales *", lvAnimales, row++);
        addRow(grid, "Título *", fTitulo, row++);
        addRow(grid, "Resumen", fResumen, row++);
        addRow(grid, "Precio deseado ($) *", fPrecioDeseado, row++);
        addRow(grid, "Precio mínimo ($) *", fPrecioMinimo, row++);
        addRow(grid, "Comisión (%)", fComision, row++);
        addRow(grid, "Tipo producción", cmbProd, row++);
        addRow(grid, "Departamento *", cmbDepto, row++);
        addRow(grid, "Ciudad", fCiudad, row++);
        addRow(grid, "Distancia (km)", fDistancia, row++);
        addRow(grid, "Vendedor", fVendedor, row++);
        addRow(grid, "Contacto *", fContacto, row++);
        addRow(grid, "Sanidad", chkVacunas, row++);
        addRow(grid, "Documentos", chkRegistro, row++);
        addRow(grid, "Observaciones", fObs, row);

        Label lblPrivado = new Label("ⓘ El precio mínimo NO se muestra al comprador. Sólo lo ve admin, vendedor y el sistema de negociación.");
        lblPrivado.setWrapText(true);
        lblPrivado.setStyle("-fx-text-fill: #92400e; -fx-font-size: 11px; -fx-font-style: italic;");
        grid.add(lblPrivado, 0, ++row, 2, 1);

        MotorSelector motorSelector = new MotorSelector();
        grid.add(motorSelector.getNode(), 0, ++row, 2, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(580);
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefWidth(620);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            List<Animal> sel = new ArrayList<>(lvAnimales.getSelectionModel().getSelectedItems());
            if (sel.isEmpty()) { NotificationService.warning("Seleccione al menos un animal"); return null; }
            if (tipo == MarketplaceListing.Tipo.INDIVIDUAL && sel.size() > 1) sel = List.of(sel.get(0));
            if (Validators.isBlank(fTitulo.getText())) { NotificationService.warning("Ingrese el título"); return null; }
            if (!Validators.precioValido(fPrecioDeseado.getText())) { NotificationService.warning("Precio deseado inválido"); return null; }
            if (!Validators.precioValido(fPrecioMinimo.getText())) { NotificationService.warning("Precio mínimo inválido"); return null; }
            double pDes = Validators.parsePrecio(fPrecioDeseado.getText());
            double pMin = Validators.parsePrecio(fPrecioMinimo.getText());
            if (pMin > pDes) { NotificationService.warning("El mínimo no puede ser mayor que el deseado"); return null; }
            if (Validators.isBlank(fContacto.getText())) { NotificationService.warning("Ingrese contacto"); return null; }

            MarketplaceListing m = new MarketplaceListing();
            m.setId(listings.size() + 1000 + (int)(Math.random() * 100));
            m.setCodigo(GanaderiaDataStore.generateMarketplaceCode());
            m.setTipo(tipo);
            m.setFincaId(fincaId);
            m.setFincaNombre(Session.getNombreFincaActual());
            m.setUbicacion((cmbDepto.getValue() != null ? cmbDepto.getValue() : "") +
                    (Validators.isBlank(fCiudad.getText()) ? "" : ", " + fCiudad.getText().trim()));
            m.setDepartamento(cmbDepto.getValue());
            m.setCiudad(fCiudad.getText().trim());
            m.setDistanciaKm(Validators.parsePrecio(fDistancia.getText()));
            m.setAnimalIds(sel.stream().map(Animal::getId).collect(Collectors.toList()));
            m.setAretes(sel.stream().map(Animal::getArete).collect(Collectors.toList()));
            m.setTituloLote(fTitulo.getText().trim());
            m.setResumen(fResumen.getText().trim());
            m.setPrecio(pDes);
            m.setPrecioMinimo(pMin);
            m.setComisionPorcentaje(Validators.numeroValido(fComision.getText()) ? Double.parseDouble(fComision.getText()) : 3.0);
            m.setRaza(sel.size() == 1 ? sel.get(0).getRaza() : "Mixto");
            m.setSexo(sel.size() == 1 ? sel.get(0).getSexo() : "Mixto");
            m.setEdadMeses(sel.stream().mapToInt(Animal::getEdadMeses).max().orElse(0));
            m.setPesoTotal(sel.stream().mapToDouble(Animal::getPesoActual).sum());
            m.setTipoProduccion(cmbProd.getValue());
            m.setEstadoSanitario(sel.get(0).getEstadoSanitario());
            m.setVacunasAlDia(chkVacunas.isSelected());
            m.setConRegistro(chkRegistro.isSelected()
                    || (sel.size() == 1 && sel.get(0).getRegistroGenealogico() != null && !sel.get(0).getRegistroGenealogico().isBlank()));
            m.setVendedor(fVendedor.getText().trim());
            m.setContacto(fContacto.getText().trim());
            m.setObservaciones(fObs.getText().trim());
            m.setEstado(MarketplaceListing.Estado.PUBLICADO);
            return m;
        });

        Optional<MarketplaceListing> result = dialog.showAndWait();
        result.ifPresent(m -> {
            PersistenceService.Motor motor = motorSelector.getMotorSeleccionado();
            GanaderiaDataStore.agregarMarketplace(m);
            PersistenceService.Resultado res = PersistenceService.crear("marketplace", m.getCodigo(), m, motor);
            actualizarConteo();
            NotificationService.success("Publicación " + m.getCodigo() + " creada — " + m.getTituloLote()
                    + " en " + motor.getEtiqueta() + " · copia en MongoDB · " + res.getMensaje());
        });
    }

    private void verDetalle(MarketplaceListing m) {
        m.setTotalVistas(m.getTotalVistas() + 1);

        // Si es publicación individual, abrir la ficha técnica completa
        if (m.getTipo() == MarketplaceListing.Tipo.INDIVIDUAL && !m.getAnimalIds().isEmpty()) {
            int animalId = m.getAnimalIds().get(0);
            Animal a = GanaderiaDataStore.getAnimalesPorFinca(m.getFincaId()).stream()
                    .filter(x -> x.getId() == animalId).findFirst().orElse(null);
            if (a != null) { FichaAnimalController.mostrar(a); tabla.refresh(); return; }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Tipo: ").append(m.getTipo()).append('\n');
        sb.append("Finca: ").append(m.getFincaNombre()).append('\n');
        sb.append("Ubicación: ").append(m.getUbicacion());
        if (m.getDistanciaKm() > 0) sb.append(String.format("  (~%.0f km)", m.getDistanciaKm()));
        sb.append('\n');
        sb.append("Raza/Sexo: ").append(m.getRaza()).append(" · ").append(m.getSexo()).append('\n');
        sb.append("Edad: ").append(m.getEdadMeses()).append(" meses · ");
        sb.append(String.format("Peso: %.0f kg%n", m.getPesoTotal()));
        sb.append("Tipo producción: ").append(m.getTipoProduccion() != null ? m.getTipoProduccion() : "—").append('\n');
        sb.append("Sanidad: ").append(m.getEstadoSanitario() != null ? m.getEstadoSanitario() : "—");
        sb.append(m.isVacunasAlDia() ? " · Vacunas al día" : " · Vacunas pendientes").append('\n');
        sb.append("Aretes: ").append(String.join(", ", m.getAretes())).append('\n');
        sb.append("Resumen: ").append(m.getResumen() != null ? m.getResumen() : "—").append("\n\n");
        sb.append(String.format("Precio deseado: $%,.0f%n", m.getPrecio()));
        if (puedeVerPrecioMinimo() && m.getPrecioMinimo() > 0) {
            sb.append(String.format("Precio mínimo (privado): $%,.0f%n", m.getPrecioMinimo()));
        }
        sb.append(String.format("Comisión (%.1f%%): $%,.0f%n", m.getComisionPorcentaje(), m.getComisionValor()));
        sb.append(String.format("Neto vendedor: $%,.0f%n%n", m.getNetoVendedor()));
        sb.append("Vendedor: ").append(m.getVendedor()).append('\n');
        sb.append("Contacto: ").append(m.getContacto()).append('\n');
        sb.append("Estado: ").append(m.getEstado()).append('\n');
        sb.append("Vistas: ").append(m.getTotalVistas()).append(" · Ofertas: ").append(m.getTotalOfertas());
        if (!Validators.isBlank(m.getObservaciones())) sb.append("\nNotas: ").append(m.getObservaciones());

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Publicación " + m.getCodigo());
        info.setHeaderText(m.getTituloLote());
        info.setContentText(sb.toString());
        info.getDialogPane().setPrefWidth(620);
        info.showAndWait();
        tabla.refresh();
    }

    private void pujar(MarketplaceListing m) {
        if (m.getEstado() == MarketplaceListing.Estado.VENDIDO
                || m.getEstado() == MarketplaceListing.Estado.RETIRADO
                || m.getEstado() == MarketplaceListing.Estado.CANCELADO) {
            NotificationService.warning("Esta publicación ya no recibe ofertas.");
            return;
        }
        Dialog<Puja> dialog = new Dialog<>();
        dialog.setTitle("Hacer puja");
        dialog.setHeaderText("Publicación: " + m.getCodigo() + " — " + m.getTituloLote()
                + "\nPrecio público: " + String.format("$%,.0f", m.getPrecio()));

        ButtonType ok = new ButtonType("Enviar oferta", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(18));

        TextField fValor = new TextField(); fValor.setPromptText("Valor ofertado ($)"); fValor.setPrefWidth(280);
        fValor.textProperty().addListener((o,a,b) -> { if (b != null && !b.matches("[0-9]*\\.?[0-9]*")) fValor.setText(a); });
        TextField fComp = new TextField();
        if (Session.getUsuarioActual() != null) fComp.setText(Session.getUsuarioActual().getNombre());
        fComp.setPromptText("Tu nombre");
        TextField fContacto = new TextField();
        if (Session.getUsuarioActual() != null) fContacto.setText(Session.getUsuarioActual().getTelefono());
        fContacto.setPromptText("WhatsApp / teléfono / correo");
        TextField fObs = new TextField(); fObs.setPromptText("Observaciones / condiciones");

        addRow(g, "Valor ofertado *", fValor, 0);
        addRow(g, "Comprador *", fComp, 1);
        addRow(g, "Contacto *", fContacto, 2);
        addRow(g, "Observaciones", fObs, 3);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            if (!Validators.precioValido(fValor.getText())) { NotificationService.warning("Valor inválido"); return null; }
            if (Validators.isBlank(fComp.getText())) { NotificationService.warning("Ingresa tu nombre"); return null; }
            if (Validators.isBlank(fContacto.getText())) { NotificationService.warning("Ingresa un contacto"); return null; }
            double valor = Validators.parsePrecio(fValor.getText());
            Puja p = new Puja(GanaderiaDataStore.nextPujaId(), m.getId(), m.getCodigo(),
                    fComp.getText().trim(), fContacto.getText().trim(), valor);
            p.setObservaciones(fObs.getText().trim());
            return p;
        });

        Optional<Puja> resultado = dialog.showAndWait();
        resultado.ifPresent(p -> {
            MarketplaceListing.Estado evalEstado = m.evaluarOferta(p.getValorOferta());
            if (evalEstado == MarketplaceListing.Estado.OFERTA_BAJA) {
                p.setEstado(Puja.Estado.OFERTA_BAJA);
                m.setEstado(MarketplaceListing.Estado.OFERTA_BAJA);
            } else {
                p.setEstado(Puja.Estado.ACEPTABLE);
                m.setEstado(MarketplaceListing.Estado.OFERTA_ACEPTABLE);
            }
            m.setTotalOfertas(m.getTotalOfertas() + 1);
            GanaderiaDataStore.agregarPuja(p);
            tabla.refresh();
            if (p.getEstado() == Puja.Estado.OFERTA_BAJA) {
                NotificationService.warning("Oferta registrada como BAJA. Puede mejorar el valor.");
            } else {
                NotificationService.success("Oferta enviada. Comparada contra precio mínimo: ACEPTABLE.");
            }
        });
    }

    private void gestionar(MarketplaceListing m) {
        if (!puedeVerPrecioMinimo()) {
            NotificationService.warning("Sólo administradores o vendedores pueden gestionar pujas.");
            return;
        }
        ObservableList<Puja> pujas = GanaderiaDataStore.getPujasPorListing(m.getId());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Gestionar publicación " + m.getCodigo());
        dialog.setHeaderText(m.getTituloLote()
                + String.format("%nPrecio público: $%,.0f · Mínimo privado: $%,.0f",
                        m.getPrecio(), m.getPrecioMinimo()));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox box = new VBox(10);
        box.setPadding(new Insets(14));

        ListView<Puja> lv = new ListView<>(pujas);
        lv.setPrefHeight(220);
        lv.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(Puja p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setText(null); return; }
                setText(String.format("#%d  %s — %s · $%,.0f · %s",
                        p.getId(), p.getComprador(), p.getCompradorContacto(), p.getValorOferta(), p.getEstado()));
            }
        });

        HBox acciones = new HBox(8);
        Button btnAceptar = new Button("Aceptar oferta");
        btnAceptar.getStyleClass().add("primary-button");
        btnAceptar.setOnAction(e -> {
            Puja p = lv.getSelectionModel().getSelectedItem();
            if (p == null) { NotificationService.warning("Selecciona una oferta"); return; }
            // Respaldo: cambia precio y estado del listing — prompt v2
            BackupService.guardar("marketplace", m.getCodigo(), m);
            p.setEstado(Puja.Estado.ACEPTADA);
            m.setEstado(MarketplaceListing.Estado.VENDIDO);
            m.setPrecio(p.getValorOferta());
            lv.refresh(); tabla.refresh();
            NotificationService.success("Oferta aceptada. Listing marcado como VENDIDO · respaldo creado.");
        });
        Button btnRechazar = new Button("Rechazar");
        btnRechazar.getStyleClass().add("danger-button");
        btnRechazar.setOnAction(e -> {
            Puja p = lv.getSelectionModel().getSelectedItem();
            if (p == null) return;
            BackupService.guardar("marketplace_pujas", String.valueOf(p.getId()), p);
            p.setEstado(Puja.Estado.RECHAZADA);
            lv.refresh();
            NotificationService.info("Oferta rechazada · respaldo creado.");
        });
        Button btnContra = new Button("Contra-oferta");
        btnContra.getStyleClass().add("secondary-button");
        btnContra.setOnAction(e -> {
            Puja p = lv.getSelectionModel().getSelectedItem();
            if (p == null) return;
            TextInputDialog d = new TextInputDialog(String.valueOf((long) m.getPrecioMinimo()));
            d.setHeaderText("Contra-oferta para " + p.getComprador());
            d.setContentText("Nuevo valor sugerido:");
            d.showAndWait().ifPresent(v -> {
                if (Validators.precioValido(v)) {
                    BackupService.guardar("marketplace_pujas", String.valueOf(p.getId()), p);
                    BackupService.guardar("marketplace", m.getCodigo(), m);
                    p.setContraOferta(Double.parseDouble(v));
                    p.setEstado(Puja.Estado.CONTRA_OFERTA);
                    m.setEstado(MarketplaceListing.Estado.NEGOCIANDO);
                    lv.refresh(); tabla.refresh();
                    NotificationService.success("Contra-oferta enviada · respaldo creado.");
                }
            });
        });
        Button btnReservar = new Button("Reservar");
        btnReservar.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1;");
        btnReservar.setOnAction(e -> {
            BackupService.guardar("marketplace", m.getCodigo(), m);
            m.setEstado(MarketplaceListing.Estado.RESERVADO);
            tabla.refresh();
            NotificationService.info("Publicación reservada · respaldo creado.");
        });
        Button btnRetirar = new Button("Retirar publicación");
        btnRetirar.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151;");
        btnRetirar.setOnAction(e -> {
            BackupService.guardar("marketplace", m.getCodigo(), m);
            m.setEstado(MarketplaceListing.Estado.RETIRADO);
            tabla.refresh();
            NotificationService.info("Publicación retirada · respaldo creado.");
        });

        acciones.getChildren().addAll(btnAceptar, btnRechazar, btnContra, btnReservar, btnRetirar);
        box.getChildren().addAll(new Label("Ofertas recibidas (" + pujas.size() + ")"), lv, acciones);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().setPrefWidth(640);
        dialog.showAndWait();
    }

    private void addRow(GridPane grid, String label, javafx.scene.Node control, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        grid.add(l, 0, row);
        grid.add(control, 1, row);
    }
}
