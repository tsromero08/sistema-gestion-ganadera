package com.sistemaganadero.controller;

import com.sistemaganadero.model.*;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.util.NotificationService;
import com.sistemaganadero.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportesController {

    // --- Cabecera ---
    @FXML private Label lblFinca;

    // --- KPIs ganaderos ---
    @FXML private Label lblTotalAnimales;
    @FXML private Label lblAvgGdp;
    @FXML private Label lblRentables;
    @FXML private Label lblCriticos;
    @FXML private Label lblPesoPromedio;
    @FXML private Label lblMortalidad;
    @FXML private Label lblTotalVacunas;

    // --- Tablas ganaderos ---
    @FXML private TableView<Map.Entry<String, Long>> tablaLotes;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colLoteNombre;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colLoteAnimales;

    @FXML private TableView<Animal> tablaNoRentables;
    @FXML private TableColumn<Animal, String> colNRarete;
    @FXML private TableColumn<Animal, String> colNRgdp;
    @FXML private TableColumn<Animal, String> colNRpeso;
    @FXML private TableColumn<Animal, String> colNRlote;

    @FXML private TableView<Vacuna> tablaVacunasPendientes;
    @FXML private TableColumn<Vacuna, String> colVParete;
    @FXML private TableColumn<Vacuna, String> colVPvacuna;
    @FXML private TableColumn<Vacuna, String> colVPfecha;

    @FXML private TableView<InventarioItem> tablaInventarioBajo;
    @FXML private TableColumn<InventarioItem, String> colInvNombre;
    @FXML private TableColumn<InventarioItem, String> colInvCantidad;
    @FXML private TableColumn<InventarioItem, String> colInvMinimo;

    @FXML private TableView<Potrero> tablaUsoPotreros;
    @FXML private TableColumn<Potrero, String> colUpCodigo;
    @FXML private TableColumn<Potrero, String> colUpNombre;
    @FXML private TableColumn<Potrero, String> colUpArea;
    @FXML private TableColumn<Potrero, String> colUpCapacidad;
    @FXML private TableColumn<Potrero, String> colUpActuales;
    @FXML private TableColumn<Potrero, String> colUpCarga;

    // --- KPIs marketplace ---
    @FXML private Label lblMPublicaciones;
    @FXML private Label lblMOfertas;
    @FXML private Label lblMVendidos;
    @FXML private Label lblMValorVendido;
    @FXML private Label lblMComision;

    @FXML private TableView<Map.Entry<String, double[]>> tablaMejoresVendedores;
    @FXML private TableColumn<Map.Entry<String, double[]>, String> colMvVendedor;
    @FXML private TableColumn<Map.Entry<String, double[]>, String> colMvCantidad;
    @FXML private TableColumn<Map.Entry<String, double[]>, String> colMvValor;

    @FXML private TableView<Map.Entry<String, double[]>> tablaRazas;
    @FXML private TableColumn<Map.Entry<String, double[]>, String> colRazaNombre;
    @FXML private TableColumn<Map.Entry<String, double[]>, String> colRazaCantidad;
    @FXML private TableColumn<Map.Entry<String, double[]>, String> colRazaValor;

    @FXML private TableView<Map.Entry<String, double[]>> tablaZonas;
    @FXML private TableColumn<Map.Entry<String, double[]>, String> colZonaDepto;
    @FXML private TableColumn<Map.Entry<String, double[]>, String> colZonaPublicaciones;
    @FXML private TableColumn<Map.Entry<String, double[]>, String> colZonaVendidos;
    @FXML private TableColumn<Map.Entry<String, double[]>, String> colZonaValor;

    @FXML
    public void initialize() {
        int fincaId = Session.getFincaActualId();
        lblFinca.setText(Session.getNombreFincaActual());

        var animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        var pesajes = GanaderiaDataStore.getPesajesPorFinca(fincaId);
        var vacunas = GanaderiaDataStore.getVacunasPorFinca(fincaId);
        var inventario = GanaderiaDataStore.getInventarioPorFinca(fincaId);
        var potreros = GanaderiaDataStore.getPotrerosPorFinca(fincaId);

        long rentables = animales.stream().filter(a -> "Rentable".equals(a.calcularEstado())).count();
        long criticos = animales.stream().filter(a -> "No Rentable".equals(a.calcularEstado())).count();
        long mortalidad = animales.stream().filter(a -> !a.isActivo()).count();
        double avgGdp = animales.stream().mapToDouble(Animal::getGdp).average().orElse(0);
        double pesoProm = animales.stream().mapToDouble(Animal::getPesoActual).average().orElse(0);

        lblTotalAnimales.setText(String.valueOf(animales.size()));
        lblAvgGdp.setText(String.format("%.2f", avgGdp));
        lblRentables.setText(String.valueOf(rentables));
        lblCriticos.setText(String.valueOf(criticos));
        lblPesoPromedio.setText(String.format("%.0f kg", pesoProm));
        lblMortalidad.setText(String.valueOf(mortalidad));
        lblTotalVacunas.setText(String.valueOf(vacunas.size()));

        // Animales por lote
        Map<String, Long> porLote = animales.stream()
                .collect(Collectors.groupingBy(Animal::getLoteNombre, Collectors.counting()));
        ObservableList<Map.Entry<String, Long>> lotesData = FXCollections.observableArrayList(porLote.entrySet());
        colLoteNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey()));
        colLoteAnimales.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getValue() + " animales"));
        tablaLotes.setItems(lotesData);

        ObservableList<Animal> noRentables = FXCollections.observableArrayList(
                animales.stream().filter(a -> a.getGdp() < 0.70).toList());
        colNRarete.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArete()));
        colNRgdp.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f kg/día", c.getValue().getGdp())));
        colNRpeso.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.0f kg", c.getValue().getPesoActual())));
        colNRlote.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLoteNombre()));
        tablaNoRentables.setItems(noRentables);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        ObservableList<Vacuna> proximas = FXCollections.observableArrayList(
                vacunas.stream().filter(Vacuna::venceProximamente).toList());
        colVParete.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArete()));
        colVPvacuna.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreVacuna()));
        colVPfecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getProximaDosis() != null ? c.getValue().getProximaDosis().format(fmt) : "N/A"));
        tablaVacunasPendientes.setItems(proximas);

        ObservableList<InventarioItem> bajoStock = FXCollections.observableArrayList(
                inventario.stream().filter(i -> i.getCantidad() <= i.getMinimo()).toList());
        colInvNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colInvCantidad.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.0f %s", c.getValue().getCantidad(), c.getValue().getUnidad())));
        colInvMinimo.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.0f %s mínimo", c.getValue().getMinimo(), c.getValue().getUnidad())));
        colInvCantidad.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(item);
                b.setPadding(new Insets(3, 10, 3, 10));
                b.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
                setGraphic(b); setText(null);
            }
        });
        tablaInventarioBajo.setItems(bajoStock);

        // Uso de potreros
        colUpCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colUpNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colUpArea.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f", c.getValue().getAreaHa())));
        colUpCapacidad.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCapacidadAnimal())));
        colUpActuales.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLoteAsignadoNombre() != null ? c.getValue().getLoteAsignadoNombre() : "Libre"));
        colUpCarga.setCellValueFactory(c -> {
            int cap = c.getValue().getCapacidadAnimal();
            int act = c.getValue().getLoteAsignadoNombre() != null ? cap : 0;
            if (cap == 0) return new SimpleStringProperty("—");
            double pct = act * 100.0 / cap;
            return new SimpleStringProperty(String.format("%.0f%% %s", pct,
                    pct > 90 ? "(sobrecargado)" : pct > 70 ? "(adecuado)" : "(holgado)"));
        });
        tablaUsoPotreros.setItems(potreros);

        cargarMarketplace();
    }

    private void cargarMarketplace() {
        ObservableList<MarketplaceListing> listings = GanaderiaDataStore.getMarketplace();

        long publicaciones = listings.size();
        long vendidos = listings.stream().filter(m -> m.getEstado() == MarketplaceListing.Estado.VENDIDO).count();
        long ofertas = GanaderiaDataStore.getPujas().size();
        double valorVendido = listings.stream()
                .filter(m -> m.getEstado() == MarketplaceListing.Estado.VENDIDO)
                .mapToDouble(MarketplaceListing::getPrecio).sum();
        double comision = listings.stream()
                .filter(m -> m.getEstado() == MarketplaceListing.Estado.VENDIDO)
                .mapToDouble(MarketplaceListing::getComisionValor).sum();

        lblMPublicaciones.setText(String.valueOf(publicaciones));
        lblMOfertas.setText(String.valueOf(ofertas));
        lblMVendidos.setText(String.valueOf(vendidos));
        lblMValorVendido.setText(formatoCorto(valorVendido));
        lblMComision.setText(formatoCorto(comision));

        // Mejores vendedores: nombre -> [count, valor]
        Map<String, double[]> vendedores = new HashMap<>();
        for (MarketplaceListing m : listings) {
            String v = m.getVendedor() != null ? m.getVendedor() : "—";
            vendedores.computeIfAbsent(v, k -> new double[2]);
            vendedores.get(v)[0] += 1;
            vendedores.get(v)[1] += m.getPrecio();
        }
        List<Map.Entry<String, double[]>> topV = vendedores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[1], a.getValue()[1])).toList();
        colMvVendedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey()));
        colMvCantidad.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.0f publicaciones", c.getValue().getValue()[0])));
        colMvValor.setCellValueFactory(c -> new SimpleStringProperty(formatoCorto(c.getValue().getValue()[1])));
        tablaMejoresVendedores.setItems(FXCollections.observableArrayList(topV));

        // Razas más vendidas
        Map<String, double[]> razas = new HashMap<>();
        for (MarketplaceListing m : listings) {
            if (m.getEstado() != MarketplaceListing.Estado.VENDIDO) continue;
            String r = m.getRaza() != null ? m.getRaza() : "—";
            razas.computeIfAbsent(r, k -> new double[2]);
            razas.get(r)[0] += 1;
            razas.get(r)[1] += m.getPrecio();
        }
        List<Map.Entry<String, double[]>> topR = razas.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0])).toList();
        colRazaNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey()));
        colRazaCantidad.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.0f", c.getValue().getValue()[0])));
        colRazaValor.setCellValueFactory(c -> new SimpleStringProperty(formatoCorto(c.getValue().getValue()[1])));
        tablaRazas.setItems(FXCollections.observableArrayList(topR));

        // Zonas (departamento) -> [publicaciones, vendidos, valor vendido]
        Map<String, double[]> zonas = new HashMap<>();
        for (MarketplaceListing m : listings) {
            String d = m.getDepartamento() != null ? m.getDepartamento() : "—";
            zonas.computeIfAbsent(d, k -> new double[3]);
            zonas.get(d)[0] += 1;
            if (m.getEstado() == MarketplaceListing.Estado.VENDIDO) {
                zonas.get(d)[1] += 1;
                zonas.get(d)[2] += m.getPrecio();
            }
        }
        List<Map.Entry<String, double[]>> topZ = zonas.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0])).toList();
        colZonaDepto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey()));
        colZonaPublicaciones.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.0f", c.getValue().getValue()[0])));
        colZonaVendidos.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.0f", c.getValue().getValue()[1])));
        colZonaValor.setCellValueFactory(c -> new SimpleStringProperty(formatoCorto(c.getValue().getValue()[2])));
        tablaZonas.setItems(FXCollections.observableArrayList(topZ));
    }

    private static String formatoCorto(double v) {
        if (v >= 1_000_000) return String.format("$%.1fM", v / 1_000_000);
        return String.format("$%,.0f", v);
    }

    @FXML
    private void exportarReporte() {
        NotificationService.info("Exportación a PDF/Excel disponible próximamente. Datos en memoria listos.");
    }
}
