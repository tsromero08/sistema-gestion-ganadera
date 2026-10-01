package com.sistemaganadero.controller;

import com.sistemaganadero.model.AlertaCentro;
import com.sistemaganadero.model.Animal;
import com.sistemaganadero.service.AlertasCenterService;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.util.NotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AlertasController {

    @FXML private Label lblKpiTotal;
    @FXML private Label lblKpiCriticas;
    @FXML private Label lblKpiPendientes;
    @FXML private Label lblKpiEnProceso;
    @FXML private Label lblKpiResueltas;
    @FXML private Label lblTotalFiltrado;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbSeveridad;
    @FXML private VBox contenedor;

    @FXML
    public void initialize() {
        ObservableList<String> categorias = FXCollections.observableArrayList("Todas");
        for (AlertaCentro.Categoria c : AlertaCentro.Categoria.values()) categorias.add(c.getEtiqueta());
        cmbCategoria.setItems(categorias);
        cmbCategoria.getSelectionModel().selectFirst();

        ObservableList<String> estados = FXCollections.observableArrayList("Todos");
        for (AlertaCentro.Estado e : AlertaCentro.Estado.values()) estados.add(e.getEtiqueta());
        cmbEstado.setItems(estados);
        cmbEstado.getSelectionModel().selectFirst();

        ObservableList<String> severidades = FXCollections.observableArrayList("Todas");
        for (AlertaCentro.Severidad s : AlertaCentro.Severidad.values()) severidades.add(s.getEtiqueta());
        cmbSeveridad.setItems(severidades);
        cmbSeveridad.getSelectionModel().selectFirst();

        cmbCategoria.valueProperty().addListener((o, a, b) -> render());
        cmbEstado.valueProperty().addListener((o, a, b) -> render());
        cmbSeveridad.valueProperty().addListener((o, a, b) -> render());

        AlertasCenterService.regenerar();
        render();
    }

    @FXML
    private void refrescar() {
        AlertasCenterService.regenerar();
        render();
        NotificationService.success("Centro de alertas actualizado");
    }

    private void render() {
        // KPIs
        long total = AlertasCenterService.contarTotalActivas();
        long criticas = AlertasCenterService.contarCriticas();
        long pendientes = AlertasCenterService.contarPorEstado(AlertaCentro.Estado.NUEVA)
                        + AlertasCenterService.contarPorEstado(AlertaCentro.Estado.VISTA);
        long enProceso = AlertasCenterService.contarPorEstado(AlertaCentro.Estado.EN_PROCESO);
        long resueltas = AlertasCenterService.contarPorEstado(AlertaCentro.Estado.RESUELTA);

        lblKpiTotal.setText(String.valueOf(total));
        lblKpiCriticas.setText(String.valueOf(criticas));
        lblKpiPendientes.setText(String.valueOf(pendientes));
        lblKpiEnProceso.setText(String.valueOf(enProceso));
        lblKpiResueltas.setText(String.valueOf(resueltas));

        // Filtrado
        ObservableList<AlertaCentro> alertas = filtrar();
        lblTotalFiltrado.setText(alertas.size() + " mostradas");

        // Agrupar por categoría
        contenedor.getChildren().clear();
        Map<AlertaCentro.Categoria, List<AlertaCentro>> grupos = new EnumMap<>(AlertaCentro.Categoria.class);
        for (AlertaCentro a : alertas) {
            grupos.computeIfAbsent(a.getCategoria(), k -> new java.util.ArrayList<>()).add(a);
        }
        for (AlertaCentro.Categoria c : AlertaCentro.Categoria.values()) {
            List<AlertaCentro> lista = grupos.getOrDefault(c, List.of());
            contenedor.getChildren().add(seccion(c, lista));
        }
    }

    private ObservableList<AlertaCentro> filtrar() {
        ObservableList<AlertaCentro> r = FXCollections.observableArrayList();
        String cat = cmbCategoria.getValue();
        String est = cmbEstado.getValue();
        String sev = cmbSeveridad.getValue();
        for (AlertaCentro a : AlertasCenterService.getTodas()) {
            if (cat != null && !cat.equals("Todas") && !a.getCategoria().getEtiqueta().equals(cat)) continue;
            if (est != null && !est.equals("Todos") && !a.getEstado().getEtiqueta().equals(est)) continue;
            if (sev != null && !sev.equals("Todas") && !a.getSeveridad().getEtiqueta().equals(sev)) continue;
            r.add(a);
        }
        return r;
    }

    private VBox seccion(AlertaCentro.Categoria categoria, List<AlertaCentro> items) {
        VBox v = new VBox(6);
        v.setPadding(new Insets(14));
        v.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1;");
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label titulo = new Label(iconoCategoria(categoria) + "  " + categoria.getEtiqueta().toUpperCase());
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f1f3d;");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Label contador = new Label(items.size() + (items.size() == 1 ? " alerta" : " alertas"));
        contador.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        header.getChildren().addAll(titulo, s, contador);
        v.getChildren().add(header);

        if (items.isEmpty()) {
            Label vacio = new Label("✓ Sin alertas en esta categoría.");
            vacio.setStyle("-fx-text-fill: #1f7a63; -fx-font-size: 12px;");
            v.getChildren().add(vacio);
        } else {
            for (AlertaCentro a : items) v.getChildren().add(filaAlerta(a));
        }
        return v;
    }

    private HBox filaAlerta(AlertaCentro a) {
        HBox fila = new HBox(10);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(8, 10, 8, 10));
        fila.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-cursor: hand;");

        Label sev = new Label("●");
        sev.setStyle("-fx-text-fill: " + a.getSeveridad().getColor() + "; -fx-font-size: 18px;");
        sev.setPrefWidth(20);

        VBox texto = new VBox(2);
        Label titulo = new Label(a.getTitulo());
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");
        titulo.setWrapText(true);
        Label desc = new Label(a.getDescripcion());
        desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");
        desc.setWrapText(true);
        Label meta = new Label(a.getFechaFormateada() + "  ·  Severidad: " + a.getSeveridad().getEtiqueta());
        meta.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        texto.getChildren().addAll(titulo, desc, meta);
        HBox.setHgrow(texto, Priority.ALWAYS);

        Label estadoBadge = badgeEstado(a.getEstado());
        ComboBox<String> cmbAccion = new ComboBox<>();
        cmbAccion.setPromptText("Cambiar estado");
        cmbAccion.getItems().addAll(
                AlertaCentro.Estado.VISTA.getEtiqueta(),
                AlertaCentro.Estado.EN_PROCESO.getEtiqueta(),
                AlertaCentro.Estado.RESUELTA.getEtiqueta(),
                AlertaCentro.Estado.IGNORADA.getEtiqueta()
        );
        cmbAccion.setPrefWidth(140);
        cmbAccion.setOnAction(e -> {
            String sel = cmbAccion.getValue();
            if (sel == null) return;
            AlertaCentro.Estado nuevo = Arrays.stream(AlertaCentro.Estado.values())
                    .filter(es -> es.getEtiqueta().equals(sel)).findFirst().orElse(AlertaCentro.Estado.VISTA);
            a.setEstado(nuevo);
            NotificationService.success("Alerta marcada como " + nuevo.getEtiqueta());
            render();
        });

        Button btnAbrir = new Button("Abrir módulo");
        btnAbrir.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6;");
        btnAbrir.setOnAction(e -> abrirReferencia(a));

        fila.getChildren().addAll(sev, texto, estadoBadge, cmbAccion, btnAbrir);

        // Click en fila marca como VISTA y abre módulo si está NUEVA
        fila.setOnMouseClicked(e -> {
            if (a.getEstado() == AlertaCentro.Estado.NUEVA) {
                a.setEstado(AlertaCentro.Estado.VISTA);
                render();
            }
        });
        return fila;
    }

    private Label badgeEstado(AlertaCentro.Estado e) {
        Label l = new Label(e.getEtiqueta());
        l.setPadding(new Insets(3, 10, 3, 10));
        l.setStyle("-fx-background-color: " + e.getBg() + "; -fx-text-fill: " + e.getFg()
                + "; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        l.setPrefWidth(100);
        return l;
    }

    private void abrirReferencia(AlertaCentro a) {
        if (a.getReferenciaTipo() == null) return;
        switch (a.getReferenciaTipo()) {
            case "animal" -> {
                if (a.getReferenciaId() != null) {
                    Animal an = GanaderiaDataStore.getAnimalById(a.getReferenciaId());
                    if (an != null) AnimalDashboardController.mostrar(an);
                }
            }
            default -> NotificationService.info("Categoría: " + a.getCategoria().getEtiqueta()
                    + " — abre el módulo correspondiente desde el menú.");
        }
    }

    private String iconoCategoria(AlertaCentro.Categoria c) {
        return switch (c) {
            case VACUNAS      -> "💉";
            case SANIDAD      -> "🩺";
            case STOCK        -> "📦";
            case POTREROS     -> "🌾";
            case ALIMENTACION -> "🌽";
            case MARKETPLACE  -> "🛒";
            case VENTAS       -> "💰";
            case USUARIOS     -> "👥";
            case SISTEMA      -> "⚙";
        };
    }
}
