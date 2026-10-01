package com.sistemaganadero.controller;

import com.sistemaganadero.model.Potrero;
import com.sistemaganadero.service.BackupService;
import com.sistemaganadero.service.GanaderiaDataStore;
import com.sistemaganadero.util.NotificationService;
import com.sistemaganadero.util.Session;
import com.sistemaganadero.util.Validators;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CulturaController {

    @FXML private VBox contenedor;

    @FXML
    public void initialize() {
        int fincaId = Session.getFincaActualId();
        ObservableList<Potrero> potreros = GanaderiaDataStore.getPotrerosPorFinca(fincaId);

        if (potreros.isEmpty()) {
            Label l = new Label("No hay potreros registrados en esta finca. Registre potreros en 'Gestión Inteligente de Potreros y Alimentación' para obtener recomendaciones automáticas.");
            l.setWrapText(true);
            l.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 20;");
            contenedor.getChildren().add(l);
            return;
        }

        // KPIs globales
        HBox kpis = new HBox(12);
        int totalPotreros = potreros.size();
        long requierenFert = potreros.stream().filter(Potrero::requiereFertilizacion).count();
        long requierenRotar = potreros.stream()
                .filter(p -> p.recomendacionRotacion().toUpperCase().contains("MOVER")).count();
        double areaTotal = potreros.stream().mapToDouble(Potrero::getAreaHa).sum();

        kpis.getChildren().addAll(
                kpi("Potreros", String.valueOf(totalPotreros), "registrados", "#3b82f6"),
                kpi("Área total", String.format("%.1f ha", areaTotal), "todas las fincas", "#06b6d4"),
                kpi("Requieren fertilizar", String.valueOf(requierenFert), "intervención necesaria", "#f59e0b"),
                kpi("Rotación urgente", String.valueOf(requierenRotar), "mover ganado", "#dc2626")
        );
        contenedor.getChildren().add(kpis);

        for (Potrero p : potreros) {
            VBox card = new VBox(8);
            card.setPadding(new Insets(16));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

            Label header = new Label(p.getNombre() + " — " + p.getCodigo());
            header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f7a63;");

            Label datos = new Label(String.format("Pasto: %s · Altura: %.0f cm · pH: %.1f · Desgaste: %d%% · Manejo: %s",
                    p.getTipoPasto(), p.getAlturaPasto(), p.getPhSuelo(),
                    p.getNivelDesgaste(), p.getTipoManejo()));
            datos.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");

            Label rotacion = new Label("ROTACIÓN: " + p.recomendacionRotacion());
            rotacion.setWrapText(true);
            rotacion.setStyle("-fx-text-fill: #1f7a63; -fx-font-weight: bold; -fx-padding: 4 0 0 0;");

            Label fert = new Label("FERTILIZACIÓN: " + p.recomendacionFertilizacion());
            fert.setWrapText(true);
            fert.setStyle("-fx-text-fill: #b45309;");

            String mejora = analizarMejoraSuelo(p);
            Label suelo = new Label("MEJORA SUELO: " + mejora);
            suelo.setWrapText(true);
            suelo.setStyle("-fx-text-fill: #4338ca;");

            String pasto = analizarUsoPasto(p);
            Label pastoLbl = new Label("USO DEL PASTO: " + pasto);
            pastoLbl.setWrapText(true);
            pastoLbl.setStyle("-fx-text-fill: #0369a1;");

            // Acciones de cada potrero
            HBox acciones = new HBox(8);
            acciones.setAlignment(Pos.CENTER_LEFT);
            Button btnEditar = new Button("✎ Editar potrero");
            btnEditar.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; "
                    + "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 14 6 14;");
            btnEditar.setOnAction(e -> editarPotreroDesdeCultura(p));
            acciones.getChildren().add(btnEditar);

            card.getChildren().addAll(header, datos, rotacion, fert, suelo, pastoLbl, acciones);
            contenedor.getChildren().add(card);
        }
    }

    private void editarPotreroDesdeCultura(Potrero p) {
        Dialog<Potrero> dialog = new Dialog<>();
        dialog.setTitle("Editar potrero — " + p.getNombre());
        dialog.setHeaderText("Ajuste rápido desde Cultura/Pasturas — se generará respaldo automático");
        ButtonType ok = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(18));
        TextField fNombre = new TextField(p.getNombre());                        fNombre.setPrefWidth(260);
        ComboBox<String> cmbPasto = new ComboBox<>(FXCollections.observableArrayList(
                "Brachiaria", "Estrella", "Kikuyo", "Pangola", "Maralfalfa", "Guinea", "Otro"));
        cmbPasto.setEditable(true);
        if (p.getTipoPasto() != null) cmbPasto.setValue(p.getTipoPasto());
        TextField fAltura = new TextField(String.valueOf(p.getAlturaPasto()));
        TextField fCantidad = new TextField(String.valueOf(p.getCantidadPasto()));
        TextField fPh = new TextField(String.valueOf(p.getPhSuelo()));
        ComboBox<String> cmbEstadoSuelo = new ComboBox<>(FXCollections.observableArrayList(
                "Excelente", "Bueno", "Regular", "Pobre"));
        if (p.getEstadoSuelo() != null) cmbEstadoSuelo.setValue(p.getEstadoSuelo());
        TextField fDesgaste = new TextField(String.valueOf(p.getNivelDesgaste()));
        TextField fObs = new TextField(p.getObservaciones() != null ? p.getObservaciones() : "");

        int r = 0;
        addRow(g, "Nombre *", fNombre, r++);
        addRow(g, "Tipo de pasto", cmbPasto, r++);
        addRow(g, "Altura del pasto (cm)", fAltura, r++);
        addRow(g, "Disponibilidad (kg/ha)", fCantidad, r++);
        addRow(g, "pH del suelo", fPh, r++);
        addRow(g, "Estado del suelo", cmbEstadoSuelo, r++);
        addRow(g, "Nivel de desgaste (0-100)", fDesgaste, r++);
        addRow(g, "Observaciones", fObs, r);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(b -> {
            if (b != ok) return null;
            if (Validators.isBlank(fNombre.getText())) { NotificationService.warning("Nombre obligatorio"); return null; }
            try {
                Double.parseDouble(fAltura.getText().trim());
                Double.parseDouble(fCantidad.getText().trim());
                Double.parseDouble(fPh.getText().trim());
                int d = Integer.parseInt(fDesgaste.getText().trim());
                if (d < 0 || d > 100) { NotificationService.warning("Desgaste debe estar entre 0 y 100"); return null; }
            } catch (Exception ex) { NotificationService.warning("Valores numéricos inválidos"); return null; }
            return p;
        });

        dialog.showAndWait().ifPresent(sel -> {
            BackupService.guardar("potreros", String.valueOf(p.getId()), p);
            p.setNombre(fNombre.getText().trim());
            p.setTipoPasto(cmbPasto.getValue());
            p.setAlturaPasto(Double.parseDouble(fAltura.getText().trim()));
            p.setCantidadPasto(Double.parseDouble(fCantidad.getText().trim()));
            p.setPhSuelo(Double.parseDouble(fPh.getText().trim()));
            p.setEstadoSuelo(cmbEstadoSuelo.getValue());
            p.setNivelDesgaste(Integer.parseInt(fDesgaste.getText().trim()));
            p.setObservaciones(fObs.getText().trim());
            NotificationService.success("Potrero " + p.getNombre() + " actualizado · respaldo creado. Recarga el módulo para ver los nuevos análisis.");
        });
    }

    private void addRow(GridPane g, String label, javafx.scene.Node n, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        g.add(l, 0, row); g.add(n, 1, row);
    }

    private VBox kpi(String label, String value, String caption, String color) {
        VBox c = new VBox(4);
        c.setPadding(new Insets(14));
        c.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 3 0;");
        Label l1 = new Label(label);
        l1.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        Label l2 = new Label(value);
        l2.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label l3 = new Label(caption);
        l3.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        c.getChildren().addAll(l1, l2, l3);
        HBox.setHgrow(c, javafx.scene.layout.Priority.ALWAYS);
        return c;
    }

    private String analizarMejoraSuelo(Potrero p) {
        StringBuilder sb = new StringBuilder();
        if (p.getPhSuelo() > 0 && p.getPhSuelo() < 5.5)
            sb.append("Encalar (1.5-2 t/ha) — pH bajo. ");
        if (p.getNivelDesgaste() >= 60)
            sb.append("Sobresembrar leguminosas (kudzu, cratylia). ");
        if ("Pobre".equalsIgnoreCase(p.getEstadoSuelo()) || "Regular".equalsIgnoreCase(p.getEstadoSuelo()))
            sb.append("Aplicar materia orgánica (10 t/ha de compost). ");
        if (sb.length() == 0) sb.append("Suelo en buenas condiciones — mantener manejo actual.");
        return sb.toString().trim();
    }

    private String analizarUsoPasto(Potrero p) {
        StringBuilder sb = new StringBuilder();
        if (p.getAlturaPasto() > 0 && p.getAlturaPasto() < 15)
            sb.append("Pasto bajo: descansar 25-30 días antes de reingreso. ");
        else if (p.getAlturaPasto() > 50)
            sb.append("Pasto excedido: cosechar para heno o henolaje. ");
        else
            sb.append("Altura adecuada para pastoreo (");
        if (p.getCantidadPasto() < 1500)
            sb.append("Producción baja — sospechar deficiencia nutricional o sobrepastoreo. ");
        else if (p.getCantidadPasto() > 3500)
            sb.append("Alta disponibilidad — aprovechar con mayor carga animal o henificar. ");
        return sb.toString().trim();
    }
}
