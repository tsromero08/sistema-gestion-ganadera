package com.sistemaganadero.controller;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.InventarioItem;
import com.sistemaganadero.model.MarketplaceListing;
import com.sistemaganadero.model.Puja;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Color;

import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardHomeController {

    @FXML private Label lblFincaNombre;
    @FXML private Label lblAnimales;
    @FXML private Label lblGdpProm;
    @FXML private Label lblCostoKg;
    @FXML private Label lblMargen;

    @FXML private Label lblPublicaciones;
    @FXML private Label lblOfertas;
    @FXML private Label lblVentasCerradas;
    @FXML private Label lblComisionAcum;

    @FXML private HBox razasRow;

    @FXML private LineChart<String, Number> chartPesoSemanal;
    @FXML private PieChart chartGdpLote;

    @FXML private TableView<FilaSanidad> tblSanidad;
    @FXML private TableColumn<FilaSanidad, String> colSanArete;
    @FXML private TableColumn<FilaSanidad, String> colSanRaza;
    @FXML private TableColumn<FilaSanidad, String> colSanSexo;
    @FXML private TableColumn<FilaSanidad, String> colSanPeso;
    @FXML private TableColumn<FilaSanidad, String> colSanEstado;

    @FXML private TableView<FilaNoRentable> tblNoRentables;
    @FXML private TableColumn<FilaNoRentable, String> colNrArete;
    @FXML private TableColumn<FilaNoRentable, String> colNrGdp;
    @FXML private TableColumn<FilaNoRentable, String> colNrPeso;
    @FXML private TableColumn<FilaNoRentable, String> colNrEstado;

    @FXML private ListView<String> listAlertas;

    @FXML
    public void initialize() {
        lblFincaNombre.setText(Session.getNombreFincaActual());
        refrescar();
    }

    private void refrescar() {
        int fincaId = Session.getFincaActualId();
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        ObservableList<InventarioItem> inventario = GanaderiaDataStore.getInventarioPorFinca(fincaId);

        lblAnimales.setText(String.valueOf(animales.size()));

        double gdpProm = animales.stream().mapToDouble(Animal::getGdp).average().orElse(0);
        lblGdpProm.setText(String.format("%.2f kg/día", gdpProm));

        double costoTotal = inventario.stream().mapToDouble(InventarioItem::getValorTotal).sum();
        double kgTotal = animales.stream().mapToDouble(Animal::getPesoActual).sum();
        double costoKg = kgTotal > 0 ? costoTotal / kgTotal : 0;
        lblCostoKg.setText(String.format("$%,.0f", costoKg));

        double valorVenta = animales.stream().mapToDouble(a -> a.getPesoActual() * 5500).sum();
        double margen = valorVenta - costoTotal;
        if (margen >= 1_000_000) lblMargen.setText(String.format("$%.1fM", margen / 1_000_000));
        else lblMargen.setText(String.format("$%,.0f", margen));

        cargarKpisMarketplace();
        cargarRazasRow(animales);
        cargarGraficaPeso();
        cargarGraficaLote(animales);
        cargarTablaSanidad(animales);
        cargarTablaNoRentables(animales);
        cargarAlertas(animales, inventario);
    }

    private void cargarKpisMarketplace() {
        ObservableList<MarketplaceListing> listings = GanaderiaDataStore.getMarketplace();
        long publicaciones = listings.stream()
                .filter(m -> m.getEstado() != MarketplaceListing.Estado.VENDIDO
                        && m.getEstado() != MarketplaceListing.Estado.RETIRADO
                        && m.getEstado() != MarketplaceListing.Estado.CANCELADO).count();
        long ofertas = GanaderiaDataStore.getPujas().size();
        long ventasCerradas = listings.stream().filter(m -> m.getEstado() == MarketplaceListing.Estado.VENDIDO).count();
        double comision = listings.stream()
                .filter(m -> m.getEstado() == MarketplaceListing.Estado.VENDIDO)
                .mapToDouble(MarketplaceListing::getComisionValor).sum();

        lblPublicaciones.setText(String.valueOf(publicaciones));
        lblOfertas.setText(String.valueOf(ofertas));
        lblVentasCerradas.setText(String.valueOf(ventasCerradas));
        if (comision >= 1_000_000) lblComisionAcum.setText(String.format("$%.1fM", comision / 1_000_000));
        else lblComisionAcum.setText(String.format("$%,.0f", comision));
    }

    private void cargarRazasRow(ObservableList<Animal> animales) {
        if (razasRow == null) return;
        razasRow.getChildren().clear();
        Map<String, Long> conteoRazas = new LinkedHashMap<>();
        for (Animal a : animales) {
            conteoRazas.merge(a.getRaza() != null ? a.getRaza() : "Otra", 1L, Long::sum);
        }
        conteoRazas.forEach((raza, cantidad) -> razasRow.getChildren().add(tarjetaRaza(raza, cantidad)));
        if (conteoRazas.isEmpty()) {
            Label vacio = new Label("Sin animales registrados para esta finca");
            vacio.setStyle("-fx-text-fill: #94a3b8;");
            razasRow.getChildren().add(vacio);
        }
    }

    private VBox tarjetaRaza(String raza, long cantidad) {
        VBox c = new VBox(4);
        c.setPrefSize(140, 130);
        c.setAlignment(Pos.CENTER);
        c.setStyle("-fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-cursor: hand;");

        // Imagen estilizada (gradiente representativo por raza, sin descargas externas)
        StackPane imagen = new StackPane();
        imagen.setPrefSize(140, 90);
        imagen.setStyle("-fx-background-radius: 10 10 0 0; -fx-background-color: "
                + gradientePorRaza(raza) + ";");
        Label icono = new Label("🐄");
        icono.setStyle("-fx-font-size: 36px;");
        imagen.getChildren().add(icono);

        Label lblRaza = new Label(raza);
        lblRaza.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label lblCount = new Label(cantidad + (cantidad == 1 ? " animal" : " animales"));
        lblCount.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        c.getChildren().addAll(imagen, lblRaza, lblCount);
        return c;
    }

    private String gradientePorRaza(String raza) {
        // Gradientes representativos por raza
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

    private void cargarGraficaPeso() {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Peso promedio");
        int fincaId = Session.getFincaActualId();
        ObservableList<Animal> animales = GanaderiaDataStore.getAnimalesPorFinca(fincaId);
        double base = animales.stream().mapToDouble(Animal::getPesoActual).average().orElse(300);
        serie.getData().add(new XYChart.Data<>("Sem 1", base * 0.88));
        serie.getData().add(new XYChart.Data<>("Sem 2", base * 0.91));
        serie.getData().add(new XYChart.Data<>("Sem 3", base * 0.95));
        serie.getData().add(new XYChart.Data<>("Sem 4", base * 0.98));
        serie.getData().add(new XYChart.Data<>("Sem 5", base));
        chartPesoSemanal.getData().setAll(serie);
    }

    private void cargarGraficaLote(ObservableList<Animal> animales) {
        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        ObservableList<com.sistemaganadero.model.Lote> lotes = GanaderiaDataStore.getLotesPorFinca(Session.getFincaActualId());
        for (var lote : lotes) {
            long count = animales.stream().filter(a -> a.getLoteNombre().equals(lote.getNombre())).count();
            if (count > 0) datos.add(new PieChart.Data(lote.getNombre(), count));
        }
        chartGdpLote.setData(datos);
    }

    private void cargarTablaSanidad(ObservableList<Animal> animales) {
        colSanArete.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().arete()));
        colSanRaza.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().raza()));
        colSanSexo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().sexo()));
        colSanPeso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().peso()));
        colSanEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().estado()));

        ObservableList<FilaSanidad> filas = FXCollections.observableArrayList();
        animales.forEach(a -> filas.add(new FilaSanidad(
                a.getArete(), a.getRaza(), a.getSexo(),
                String.format("%.0f kg", a.getPesoActual()), a.calcularEstado())));
        tblSanidad.setItems(filas);
    }

    private void cargarTablaNoRentables(ObservableList<Animal> animales) {
        colNrArete.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().arete()));
        colNrGdp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().gdp()));
        colNrPeso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().peso()));
        colNrEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().estado()));

        ObservableList<FilaNoRentable> filas = FXCollections.observableArrayList();
        animales.stream()
                .filter(a -> a.getGdp() < 0.70)
                .forEach(a -> filas.add(new FilaNoRentable(
                        a.getArete(),
                        String.format("%.2f kg/día", a.getGdp()),
                        String.format("%.0f kg", a.getPesoActual()),
                        a.calcularEstado())));
        tblNoRentables.setItems(filas);
    }

    private void cargarAlertas(ObservableList<Animal> animales, ObservableList<InventarioItem> inventario) {
        ObservableList<String> alertas = FXCollections.observableArrayList();
        animales.stream()
                .filter(a -> a.getGdp() < 0.50)
                .forEach(a -> alertas.add("⚠ Arete " + a.getArete() + " no rentable (GDP: " + String.format("%.2f", a.getGdp()) + ")"));
        inventario.stream()
                .filter(i -> i.getCantidad() <= i.getMinimo())
                .forEach(i -> alertas.add("⚠ Stock bajo: " + i.getNombre() + " (" + i.getCantidad() + " " + i.getUnidad() + ")"));
        if (alertas.isEmpty()) alertas.add("✓ Sin alertas críticas en este momento");
        listAlertas.setItems(alertas);
    }

    @FXML
    private void accionPesaje() {
        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION,
                "Abre el módulo Pesajes desde el menú lateral para registrar un nuevo pesaje.").showAndWait();
    }

    @FXML
    private void accionVacuna() {
        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION,
                "Abre el módulo Vacunas desde el menú lateral para registrar una nueva aplicación.").showAndWait();
    }

    @FXML
    private void accionReporte() {
        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION,
                "Abre el módulo Reportes desde el menú lateral para consultar los reportes disponibles.").showAndWait();
    }

    public record FilaSanidad(String arete, String raza, String sexo, String peso, String estado) {}
    public record FilaNoRentable(String arete, String gdp, String peso, String estado) {}
}
