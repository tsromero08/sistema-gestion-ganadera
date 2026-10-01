package com.sistemaganadero.controller;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.Pesaje;
import com.sistemaganadero.model.Potrero;
import com.sistemaganadero.model.RecomendacionDieta;
import com.sistemaganadero.model.RegistroSanitario;
import com.sistemaganadero.model.Vacuna;
import com.sistemaganadero.service.DietaService;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.service.GananciaDiariaService;
import com.sistemaganadero.service.SanidadService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class AnimalDashboardController {

    public static void mostrar(Animal animal) {
        if (animal == null) return;

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Dashboard Individual — " + animal.getArete() + " " + animal.getNombre());

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f0f4f8; -fx-background-color: #f0f4f8;");

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f4f8;");

        root.getChildren().add(buildHeader(animal));
        root.getChildren().add(buildKpisRow(animal));
        root.getChildren().add(buildFichaTecnicaRow(animal));
        root.getChildren().add(buildGaleriaRow(animal));
        root.getChildren().add(buildPesoChartRow(animal));
        root.getChildren().add(buildHistorialesRow(animal));
        root.getChildren().add(buildDocumentosRow(animal));
        root.getChildren().add(buildRecomendacionesRow(animal));
        root.getChildren().add(buildPlanIndividualRow(animal));

        scroll.setContent(root);
        Scene scene = new Scene(scroll, 1100, 760);
        var cssUrl = AnimalDashboardController.class.getResource("/css/app.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private static HBox buildHeader(Animal a) {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);");

        // Imagen o avatar
        Region avatar;
        if (a.getFotoPath() != null && !a.getFotoPath().isBlank()
                && new File(a.getFotoPath()).exists()) {
            try {
                ImageView iv = new ImageView(new Image(new File(a.getFotoPath()).toURI().toString(),
                        110, 110, true, true));
                Rectangle clip = new Rectangle(110, 110);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                iv.setClip(clip);
                avatar = new StackPane(iv);
            } catch (Exception e) {
                avatar = avatarPlaceholder(a);
            }
        } else {
            avatar = avatarPlaceholder(a);
        }

        VBox info = new VBox(4);
        Label nombre = new Label(a.getNombre() != null ? a.getNombre() : "Sin nombre");
        nombre.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label codigo = new Label("Código: " + a.getArete());
        codigo.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        HBox tags = new HBox(8);
        tags.setAlignment(Pos.CENTER_LEFT);
        tags.getChildren().addAll(
                tagLabel(a.getRaza(), "#dbeafe", "#1e40af"),
                tagLabel(a.getSexo(), "#fce7f3", "#9d174d"),
                tagLabel(a.calcularEstado(), estadoBg(a.calcularEstado()), estadoFg(a.calcularEstado())),
                tagLabel("Edad: " + a.getEdadFormateada(), "#f3e8ff", "#6b21a8")
        );

        Label finca = new Label("Finca: " + (a.getFincaNombre() != null ? a.getFincaNombre() : "—")
                + "    •    Lote: " + (a.getLoteNombre() != null ? a.getLoteNombre() : "—"));
        finca.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        info.getChildren().addAll(nombre, codigo, tags, finca);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnFicha = new Button("📋 Ver ficha técnica");
        btnFicha.setStyle("-fx-background-color: #1f7a63; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 8 14 8 14; -fx-background-radius: 8;");
        btnFicha.setOnAction(e -> FichaAnimalController.mostrar(a));

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand; -fx-font-weight: bold;");
        btnCerrar.setOnAction(e -> ((Stage) btnCerrar.getScene().getWindow()).close());

        header.getChildren().addAll(avatar, info, spacer, btnFicha, btnCerrar);
        return header;
    }

    private static StackPane avatarPlaceholder(Animal a) {
        StackPane sp = new StackPane();
        sp.setPrefSize(110, 110);
        sp.setStyle("-fx-background-color: linear-gradient(to bottom right, #1f7a63, #0f1f3d); -fx-background-radius: 12;");
        String iniciales = a.getNombre() != null && !a.getNombre().isBlank()
                ? String.valueOf(a.getNombre().charAt(0)).toUpperCase() : "?";
        Label l = new Label(iniciales);
        l.setStyle("-fx-text-fill: white; -fx-font-size: 48px; -fx-font-weight: bold;");
        sp.getChildren().add(l);
        return sp;
    }

    private static Label tagLabel(String text, String bg, String fg) {
        Label l = new Label(text != null ? text : "—");
        l.setPadding(new Insets(3, 10, 3, 10));
        l.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
                "-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }

    private static String estadoBg(String estado) {
        return switch (estado) {
            case "Rentable"    -> "#d1fae5";
            case "Mantener"    -> "#dbeafe";
            case "Bajo"        -> "#fed7aa";
            case "No Rentable" -> "#fee2e2";
            default            -> "#f3f4f6";
        };
    }

    private static String estadoFg(String estado) {
        return switch (estado) {
            case "Rentable"    -> "#065f46";
            case "Mantener"    -> "#1e40af";
            case "Bajo"        -> "#92400e";
            case "No Rentable" -> "#991b1b";
            default            -> "#374151";
        };
    }

    private static HBox buildKpisRow(Animal a) {
        HBox row = new HBox(12);
        double ganancia = a.getGananciaTotal();
        int dias = a.getDiasEnSistema();
        row.getChildren().addAll(
                kpiCard("Tiempo en sistema", dias + " días", "Desde el ingreso", "#3b82f6"),
                kpiCard("Peso ingreso", String.format("%.0f kg", a.getPesoInicial()), "Inicial", "#06b6d4"),
                kpiCard("Peso actual", String.format("%.0f kg", a.getPesoActual()), "Última medición", "#1f7a63"),
                kpiCard("Ganancia total", String.format("%+.0f kg", ganancia), "Acumulado", "#f59e0b"),
                kpiCard("GDP", String.format("%.2f kg/día", a.getGdp()), "Promedio diario", "#0f1f3d")
        );
        return row;
    }

    private static VBox kpiCard(String label, String value, String caption, String borderColor) {
        VBox c = new VBox(4);
        c.setPadding(new Insets(14, 16, 14, 16));
        c.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; " +
                "-fx-border-color: " + borderColor + "; -fx-border-width: 0 0 3 0; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");
        Label l1 = new Label(label);
        l1.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        Label l2 = new Label(value);
        l2.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label l3 = new Label(caption);
        l3.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        c.getChildren().addAll(l1, l2, l3);
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    private static HBox buildPesoChartRow(Animal a) {
        HBox row = new HBox(16);

        VBox chartBox = card("Evolución del peso");
        HBox.setHgrow(chartBox, Priority.ALWAYS);

        CategoryAxis x = new CategoryAxis();
        x.setLabel("Fecha");
        NumberAxis y = new NumberAxis();
        y.setLabel("kg");

        LineChart<String, Number> chart = new LineChart<>(x, y);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(260);

        XYChart.Series<String, Number> serie = new XYChart.Series<>();

        ObservableList<Pesaje> pesajes = GanaderiaDataStore.getPesajesPorFinca(a.getFincaId());
        List<Pesaje> hist = pesajes.stream()
                .filter(p -> p.getAnimalId() == a.getId() && p.getFecha() != null)
                .sorted(Comparator.comparing(Pesaje::getFecha))
                .toList();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (Pesaje p : hist) {
            serie.getData().add(new XYChart.Data<>(p.getFecha().format(fmt), p.getPeso()));
        }
        if (hist.isEmpty()) {
            serie.getData().add(new XYChart.Data<>("Ingreso", a.getPesoInicial()));
            serie.getData().add(new XYChart.Data<>("Actual", a.getPesoActual()));
        }
        chart.getData().add(serie);
        chartBox.getChildren().add(chart);

        // Rendimiento por periodos
        VBox rendBox = card("Rendimiento por períodos");
        rendBox.setPrefWidth(320);
        double gdp30 = GananciaDiariaService.calcularGdpRecientes(hist, 30);
        double gdp60 = GananciaDiariaService.calcularGdpRecientes(hist, 60);
        double gdp90 = GananciaDiariaService.calcularGdpRecientes(hist, 90);
        rendBox.getChildren().addAll(
                periodRow("Últimos 30 días", gdp30),
                periodRow("Últimos 60 días", gdp60),
                periodRow("Últimos 90 días", gdp90),
                periodRow("Total histórico", a.getGdp())
        );

        row.getChildren().addAll(chartBox, rendBox);
        return row;
    }

    private static HBox periodRow(String periodo, double gdp) {
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(6, 0, 6, 0));
        Label l = new Label(periodo);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        Label v = new Label(String.format("%.2f kg/día", gdp));
        String color = gdp >= 0.9 ? "#1f7a63" : gdp >= 0.7 ? "#3b82f6" : gdp >= 0.5 ? "#f59e0b" : "#dc2626";
        v.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        h.getChildren().addAll(l, s, v);
        return h;
    }

    private static HBox buildHistorialesRow(Animal a) {
        HBox row = new HBox(16);

        VBox vacBox = card("Vacunas — aplicadas y pendientes");
        ListView<String> listVac = new ListView<>();
        listVac.setPrefHeight(220);
        ObservableList<Vacuna> vacunas = GanaderiaDataStore.getVacunasPorFinca(a.getFincaId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        ObservableList<String> itemsV = FXCollections.observableArrayList();
        vacunas.stream().filter(v -> v.getAnimalId() == a.getId()).forEach(v -> {
            String estado = v.getProximaDosis() == null ? "única"
                    : (v.getProximaDosis().isBefore(LocalDate.now()) ? "VENCIDA" :
                       v.venceProximamente() ? "próxima" : "vigente");
            itemsV.add("• " + v.getNombreVacuna() + " — aplicada " + v.getFechaAplicacion().format(fmt)
                    + (v.getProximaDosis() != null ? "  →  próxima " + v.getProximaDosis().format(fmt) : "")
                    + "  [" + estado + "]");
        });
        if (itemsV.isEmpty()) itemsV.add("Sin registros de vacunación");
        listVac.setItems(itemsV);
        vacBox.getChildren().add(listVac);
        HBox.setHgrow(vacBox, Priority.ALWAYS);

        VBox tratBox = card("Tratamientos / Sanitarios");
        ListView<String> listTrat = new ListView<>();
        listTrat.setPrefHeight(220);
        ObservableList<RegistroSanitario> regs = GanaderiaDataStore.getRegistrosSanitariosPorFinca(a.getFincaId());
        ObservableList<String> itemsT = FXCollections.observableArrayList();
        regs.stream().filter(r -> r.getAnimalId() == a.getId()).forEach(r -> {
            itemsT.add("• " + r.getTipo() + " — " + r.getDescripcion() + " (" + r.getFecha().format(fmt) + ")");
        });
        if (itemsT.isEmpty()) itemsT.add("Sin tratamientos registrados");
        listTrat.setItems(itemsT);
        tratBox.getChildren().add(listTrat);
        HBox.setHgrow(tratBox, Priority.ALWAYS);

        VBox alertBox = card("Alertas");
        ListView<String> listAlert = new ListView<>();
        listAlert.setPrefHeight(220);
        listAlert.setStyle("-fx-control-inner-background: #fffbeb;");
        ObservableList<String> alertas = FXCollections.observableArrayList();
        if (a.getGdp() < 0.5) alertas.add("⚠ GDP bajo (" + String.format("%.2f", a.getGdp()) + ") — requiere plan individual");
        if (a.getPesoActual() < a.getPesoInicial() * 0.95) alertas.add("⚠ Pérdida de peso detectada");
        ObservableList<Pesaje> todosPesajes = GanaderiaDataStore.getPesajesPorFinca(a.getFincaId());
        List<Pesaje> histAnimal = todosPesajes.stream().filter(p -> p.getAnimalId() == a.getId()).toList();
        if (GananciaDiariaService.requierePesaje(a.getId(), histAnimal))
            alertas.add("⚠ Sin pesaje en más de 60 días");
        SanidadService.vacunasPendientes(a.getFincaId()).stream()
                .filter(av -> av.animal().getId() == a.getId())
                .forEach(av -> alertas.add("⚠ Vacuna " + av.vacuna().getNombreVacuna() + " — " + av.estado()));
        if (alertas.isEmpty()) alertas.add("✓ Sin alertas críticas");
        listAlert.setItems(alertas);
        alertBox.getChildren().add(listAlert);
        alertBox.setPrefWidth(320);

        row.getChildren().addAll(vacBox, tratBox, alertBox);
        return row;
    }

    private static HBox buildRecomendacionesRow(Animal a) {
        HBox row = new HBox(16);

        // Recomendación de dieta
        VBox dietBox = card("Recomendación de Dieta");
        HBox.setHgrow(dietBox, Priority.ALWAYS);
        Potrero potreroAsoc = null;
        ObservableList<Potrero> potreros = GanaderiaDataStore.getPotrerosPorFinca(a.getFincaId());
        if (!potreros.isEmpty()) potreroAsoc = potreros.get(0);
        RecomendacionDieta dieta = DietaService.recomendarParaAnimal(a, potreroAsoc, "Engorde / mantenimiento");

        TextArea taDieta = new TextArea(dieta.resumen());
        taDieta.setEditable(false);
        taDieta.setWrapText(true);
        taDieta.setPrefRowCount(8);
        taDieta.setStyle("-fx-control-inner-background: #f8fafc; -fx-font-size: 12px;");
        dietBox.getChildren().add(taDieta);

        // Recomendaciones sanitarias y vitaminas
        VBox sanBox = card("Vitaminas y Sanidad");
        sanBox.setPrefWidth(380);
        Label lblVit = new Label("• Vitaminas ADE (cada 3 meses)");
        lblVit.setStyle("-fx-text-fill: #475569;");
        Label lblPurg = new Label("• Purga (Ivermectina) cada 4 meses");
        lblPurg.setStyle("-fx-text-fill: #475569;");
        Label lblSal = new Label("• Sal mineralizada — libre acceso");
        lblSal.setStyle("-fx-text-fill: #475569;");
        Label lblComp = new Label("• Complejo B + zinc si GDP < 0.7");
        lblComp.setStyle("-fx-text-fill: #475569;");
        Label lblObs = new Label(a.getObservaciones() != null && !a.getObservaciones().isBlank()
                ? "Observaciones: " + a.getObservaciones() : "Sin observaciones especiales");
        lblObs.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 8 0 0 0;");
        lblObs.setWrapText(true);
        sanBox.getChildren().addAll(lblVit, lblPurg, lblSal, lblComp, lblObs);

        row.getChildren().addAll(dietBox, sanBox);
        return row;
    }

    private static VBox buildPlanIndividualRow(Animal a) {
        VBox box = card("Plan Individual / Predicción de rendimiento");
        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefRowCount(8);
        ta.setStyle("-fx-control-inner-background: #f8fafc; -fx-font-size: 12px;");

        StringBuilder sb = new StringBuilder();
        if (a.getGdp() < 0.5) {
            sb.append(SanidadService.generarPlanIndividualBajoRendimiento(a));
            sb.append("\n\n");
        }
        // Predicción simple a 90 días
        double pesoProyectado = a.getPesoActual() + (a.getGdp() * 90);
        sb.append("PREDICCIÓN A 90 DÍAS\n");
        sb.append("====================\n");
        sb.append("Peso proyectado: ").append(String.format("%.0f kg", pesoProyectado)).append("\n");
        sb.append("Ganancia esperada: ").append(String.format("%+.0f kg", pesoProyectado - a.getPesoActual())).append("\n");
        sb.append("Estado proyectado: ");
        if (a.getGdp() >= 0.9) sb.append("Rentable\n");
        else if (a.getGdp() >= 0.7) sb.append("Mantener\n");
        else if (a.getGdp() >= 0.5) sb.append("Bajo — requiere ajustes\n");
        else sb.append("No rentable — considerar plan o descarte\n");

        ta.setText(sb.toString());
        box.getChildren().add(ta);
        return box;
    }

    private static VBox card(String title) {
        VBox c = new VBox(10);
        c.setPadding(new Insets(16));
        c.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);");
        Label l = new Label(title);
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        c.getChildren().add(l);
        return c;
    }

    private static HBox buildFichaTecnicaRow(Animal a) {
        HBox row = new HBox(16);
        // Datos básicos
        VBox basicos = card("Datos básicos");
        HBox.setHgrow(basicos, Priority.ALWAYS);
        basicos.getChildren().addAll(
                ficha("Color", a.getColor() != null ? a.getColor() : "—"),
                ficha("Estado corporal", a.getEstadoCorporalEtiqueta()),
                ficha("Estado sanitario", a.getEstadoSanitario() != null ? a.getEstadoSanitario() : "—"),
                ficha("Estado reproductivo", a.getEstadoReproductivo() != null ? a.getEstadoReproductivo() : "—"),
                ficha("Tipo producción", a.getTipoProduccion() != null ? a.getTipoProduccion() : "—"),
                ficha("Uso recomendado", a.getUsoRecomendado() != null ? a.getUsoRecomendado() : "—")
        );

        // Productividad
        VBox productividad = card("Datos productivos");
        HBox.setHgrow(productividad, Priority.ALWAYS);
        productividad.getChildren().addAll(
                ficha("Producción leche/día", a.getProduccionLecheDiaria() > 0
                        ? String.format("%.1f L", a.getProduccionLecheDiaria()) : "N/A"),
                ficha("Número de partos", String.valueOf(a.getNumeroPartos())),
                ficha("Estado de gestación", a.getEstadoGestacion() != null ? a.getEstadoGestacion() : "No gestante"),
                ficha("Último parto", a.getUltimoParto() != null ? a.getUltimoParto().toString() : "—"),
                ficha("Próximo servicio", a.getProximoServicio() != null ? a.getProximoServicio().toString() : "—"),
                ficha("Veterinario responsable", a.getVeterinarioResponsable() != null ? a.getVeterinarioResponsable() : "—")
        );

        // Genética
        VBox genetica = card("Genética y registro");
        HBox.setHgrow(genetica, Priority.ALWAYS);
        genetica.getChildren().addAll(
                ficha("Calidad genética", a.getCalidadGenetica() != null ? a.getCalidadGenetica() : "—"),
                ficha("Línea genética", a.getLineaGenetica() != null ? a.getLineaGenetica() : "—"),
                ficha("Registro genealógico", a.getRegistroGenealogico() != null ? a.getRegistroGenealogico() : "—"),
                ficha("Registro ICA", a.getRegistroIcaNumero() != null ? a.getRegistroIcaNumero() : "Pendiente"),
                ficha("Fecha ingreso", a.getFechaIngreso() != null ? a.getFechaIngreso().toString() : "—"),
                ficha("Fecha nacimiento", a.getFechaNacimiento() != null ? a.getFechaNacimiento().toString() : "—")
        );

        row.getChildren().addAll(basicos, productividad, genetica);
        return row;
    }

    private static HBox ficha(String label, String valor) {
        HBox h = new HBox(8);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(4, 0, 4, 0));
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        l.setPrefWidth(170);
        Label v = new Label(valor);
        v.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        v.setWrapText(true);
        HBox.setHgrow(v, Priority.ALWAYS);
        h.getChildren().addAll(l, v);
        return h;
    }

    private static VBox buildGaleriaRow(Animal a) {
        VBox box = card("Galería de fotos y video");
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.CENTER_LEFT);

        // Foto principal
        StackPane principal = thumbAnimal(a, a.getFotoPath(), "Principal");
        fila.getChildren().add(principal);

        // Fotos de galería
        if (a.getGaleriaFotos() != null) {
            for (int i = 0; i < a.getGaleriaFotos().size(); i++) {
                fila.getChildren().add(thumbAnimal(a, a.getGaleriaFotos().get(i), "Foto " + (i + 2)));
            }
        }

        if (a.getVideoPath() != null && !a.getVideoPath().isBlank()) {
            Label v = new Label("🎬 Video: " + a.getVideoPath());
            v.setStyle("-fx-text-fill: #1f7a63; -fx-font-weight: bold; -fx-padding: 8 14;");
            fila.getChildren().add(v);
        }

        Label info = new Label("Agregue fotos adicionales y video desde el módulo de Animales · Soporte: 301 389 6861");
        info.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-style: italic;");
        box.getChildren().addAll(fila, info);
        return box;
    }

    private static StackPane thumbAnimal(Animal a, String path, String etiqueta) {
        StackPane sp = new StackPane();
        sp.setPrefSize(140, 110);
        sp.setStyle("-fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-background-color: #f8fafc;");
        if (path != null && !path.isBlank() && new File(path).exists()) {
            try {
                ImageView iv = new ImageView(new Image(new File(path).toURI().toString(), 140, 110, true, true));
                Rectangle clip = new Rectangle(140, 110);
                clip.setArcWidth(20); clip.setArcHeight(20);
                iv.setClip(clip);
                sp.getChildren().add(iv);
            } catch (Exception e) {
                sp.getChildren().add(placeholderFoto(a, etiqueta));
            }
        } else {
            sp.getChildren().add(placeholderFoto(a, etiqueta));
        }
        return sp;
    }

    private static Label placeholderFoto(Animal a, String etiqueta) {
        Label l = new Label("📷\n" + etiqueta);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-text-alignment: center;");
        l.setWrapText(true);
        return l;
    }

    private static HBox buildDocumentosRow(Animal a) {
        HBox row = new HBox(16);
        VBox box = card("Documentos legales y sanitarios");
        HBox.setHgrow(box, Priority.ALWAYS);

        box.getChildren().addAll(
                docRow("📋 Registro ICA",
                        a.getRegistroIcaNumero() != null && !a.getRegistroIcaNumero().isBlank()
                                ? "Activo · Nº " + a.getRegistroIcaNumero() : "Pendiente"),
                docRow("🐾 Guía sanitaria de movilización",
                        a.getGuiaSanitariaPath() != null ? a.getGuiaSanitariaPath() : "Sin guía registrada"),
                docRow("💉 Certificado de vacunación",
                        a.getCertificadoVacunacionPath() != null ? a.getCertificadoVacunacionPath() : "Sin certificado"),
                docRow("📜 Registro de propiedad",
                        a.getRegistroPropiedadPath() != null ? a.getRegistroPropiedadPath() : "Sin registro"),
                docRow("🧬 Registro genealógico",
                        a.getRegistroGenealogicoPath() != null ? a.getRegistroGenealogicoPath() : "Sin registro"),
                docRow("🧾 Factura de compra",
                        a.getFacturaCompraPath() != null ? a.getFacturaCompraPath() : "Sin factura adjunta")
        );

        // Otros documentos
        if (a.getDocumentosAdjuntos() != null) {
            for (String d : a.getDocumentosAdjuntos()) {
                box.getChildren().add(docRow("📎 Documento adjunto", d));
            }
        }

        row.getChildren().add(box);
        return row;
    }

    private static HBox docRow(String titulo, String valor) {
        HBox h = new HBox(8);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(6, 0, 6, 0));
        Label l = new Label(titulo);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b; -fx-font-weight: 600;");
        l.setPrefWidth(280);
        Label v = new Label(valor);
        v.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
                (valor.startsWith("Pendiente") || valor.startsWith("Sin") ? "#94a3b8" : "#1f7a63") + ";");
        v.setWrapText(true);
        HBox.setHgrow(v, Priority.ALWAYS);
        h.getChildren().addAll(l, v);
        return h;
    }
}
