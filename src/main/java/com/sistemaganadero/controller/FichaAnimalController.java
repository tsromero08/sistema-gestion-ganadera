package com.sistemaganadero.controller;

import com.sistemaganadero.model.Animal;
import com.sistemaganadero.model.Finca;
import com.sistemaganadero.model.RegistroSanitario;
import com.sistemaganadero.model.Vacuna;
import com.sistemaganadero.service.GanaderiaDataStore;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class FichaAnimalController {

    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label lblFotoIcon;
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblBadgeEstado;
    @FXML private Label lblBadgeSanidad;
    @FXML private Label lblBadgeUso;

    // Resumen
    @FXML private Label lblCodigo;
    @FXML private Label lblNombre;
    @FXML private Label lblRaza;
    @FXML private Label lblSexo;
    @FXML private Label lblEdad;
    @FXML private Label lblColor;
    @FXML private Label lblPeso;
    @FXML private Label lblTipoProd;
    @FXML private Label lblEstadoCorporal;
    @FXML private FlowPane paneGaleria;
    @FXML private Label lblGaleriaVacia;
    @FXML private Label lblVideo;
    @FXML private Label lblObservaciones;

    // Ubicación
    @FXML private Label lblFinca;
    @FXML private Label lblLote;
    @FXML private Label lblDepartamento;
    @FXML private Label lblCiudad;
    @FXML private Label lblVereda;
    @FXML private Label lblDistancia;

    // Sanidad
    @FXML private Label lblEstadoSanitario;
    @FXML private Label lblVeterinario;
    @FXML private Label lblEstadoRepro;
    @FXML private TableView<Vacuna> tblVacunas;
    @FXML private TableColumn<Vacuna, String> colVacTipo;
    @FXML private TableColumn<Vacuna, String> colVacFecha;
    @FXML private TableColumn<Vacuna, String> colVacProx;
    @FXML private TableColumn<Vacuna, String> colVacResp;
    @FXML private TableColumn<Vacuna, String> colVacEstado;

    @FXML private TableView<RegistroSanitario> tblSanidad;
    @FXML private TableColumn<RegistroSanitario, String> colSanFecha;
    @FXML private TableColumn<RegistroSanitario, String> colSanDiag;
    @FXML private TableColumn<RegistroSanitario, String> colSanTrat;
    @FXML private TableColumn<RegistroSanitario, String> colSanVet;
    @FXML private TableColumn<RegistroSanitario, String> colSanEstado;

    // Productivos
    @FXML private Label lblGdp;
    @FXML private Label lblLeche;
    @FXML private Label lblUsoRec;
    @FXML private Label lblPartos;
    @FXML private Label lblGestacion;
    @FXML private Label lblUltParto;
    @FXML private Label lblCalidad;
    @FXML private Label lblLinea;
    @FXML private Label lblProxServ;
    @FXML private Label lblPesoResumen;

    // Documentos
    @FXML private Label lblIca;
    @FXML private Label lblRegGen;
    @FXML private Label lblGuiaSan;
    @FXML private Label lblCertVac;
    @FXML private Label lblRegProp;
    @FXML private Label lblFactura;
    @FXML private ListView<String> lvDocumentos;
    @FXML private Label lblDocVacios;

    private Animal animal;

    public static void mostrar(Animal animal) {
        if (animal == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(FichaAnimalController.class.getResource("/view/ficha_animal.fxml"));
            javafx.scene.Parent root = loader.load();
            FichaAnimalController c = loader.getController();
            c.cargar(animal);

            Scene scene = new Scene(root, 1100, 720);
            scene.getStylesheets().add(FichaAnimalController.class.getResource("/css/app.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Ficha técnica · " + animal.getArete() + " · " + animal.getNombre());
            stage.setScene(scene);
            stage.initModality(Modality.NONE);
            stage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ficha: " + e.getMessage()).showAndWait();
        }
    }

    private void cargar(Animal a) {
        this.animal = a;

        // Header
        lblTitulo.setText(safe(a.getNombre(), "Sin nombre") + " · " + safe(a.getArete(), "—"));
        lblSubtitulo.setText(String.format("%s · %s · %s · %s",
                safe(a.getRaza(), "—"), safe(a.getSexo(), "—"),
                a.getEdadFormateada(), a.getPesoActualFormateado()));

        aplicarBadge(lblBadgeEstado, a.getEstado(), badgeOk(a.calcularEstado()));
        aplicarBadge(lblBadgeSanidad, safe(a.getEstadoSanitario(), "—"), badgeSanidad(a.getEstadoSanitario()));
        aplicarBadge(lblBadgeUso, safe(a.getUsoRecomendado(), "—"), "ficha-badge ficha-badge-info");

        // Resumen
        lblCodigo.setText(safe(a.getArete(), "—"));
        lblNombre.setText(safe(a.getNombre(), "Sin nombre"));
        lblRaza.setText(safe(a.getRaza(), "—"));
        lblSexo.setText(safe(a.getSexo(), "—"));
        lblEdad.setText(a.getEdadFormateada());
        lblColor.setText(safe(a.getColor(), "—"));
        lblPeso.setText(a.getPesoActualFormateado());
        lblTipoProd.setText(safe(a.getTipoProduccion(), "—"));
        lblEstadoCorporal.setText(a.getEstadoCorporalEtiqueta());
        lblObservaciones.setText(safe(a.getObservaciones(), "Sin observaciones registradas"));
        lblVideo.setText(a.getVideoPath() != null && !a.getVideoPath().isBlank()
                ? "🎥 " + a.getVideoPath() : "Sin video disponible");

        // Galería
        paneGaleria.getChildren().clear();
        List<String> fotos = a.getGaleriaFotos();
        if (a.getFotoPath() != null && !a.getFotoPath().isBlank()) paneGaleria.getChildren().add(tarjetaFoto("Principal", a.getFotoPath()));
        if (fotos != null) for (int i = 0; i < fotos.size(); i++) paneGaleria.getChildren().add(tarjetaFoto("Foto " + (i + 1), fotos.get(i)));
        boolean vacia = paneGaleria.getChildren().isEmpty();
        lblGaleriaVacia.setVisible(vacia);
        lblGaleriaVacia.setManaged(vacia);

        // Ubicación
        Finca finca = GanaderiaDataStore.getFincas().stream()
                .filter(f -> f.getId() == a.getFincaId()).findFirst().orElse(null);
        lblFinca.setText(finca != null ? finca.getNombre() : safe(a.getFincaNombre(), "—"));
        lblLote.setText(safe(a.getLoteNombre(), "—"));
        lblDepartamento.setText(finca != null ? safe(finca.getDepartamento(), "—") : "—");
        lblCiudad.setText(finca != null ? safe(finca.getMunicipio(), "—") : "—");
        lblVereda.setText(finca != null ? safe(finca.getVereda(), "—") : "—");
        lblDistancia.setText(finca != null && finca.getDistanciaKm() > 0
                ? String.format("~%.0f km", finca.getDistanciaKm()) : "Por consultar");

        // Sanidad
        lblEstadoSanitario.setText(safe(a.getEstadoSanitario(), "Saludable"));
        lblVeterinario.setText(safe(a.getVeterinarioResponsable(), "Sin asignar"));
        lblEstadoRepro.setText(safe(a.getEstadoReproductivo(), "Normal"));

        configurarTablaVacunas();
        ObservableList<Vacuna> vacunasAnimal = FXCollections.observableArrayList(
                GanaderiaDataStore.getVacunasPorFinca(a.getFincaId()).stream()
                        .filter(v -> v.getAnimalId() == a.getId() || (v.getArete() != null && v.getArete().equals(a.getArete())))
                        .collect(Collectors.toList()));
        tblVacunas.setItems(vacunasAnimal);

        configurarTablaSanidad();
        ObservableList<RegistroSanitario> regAnimal = FXCollections.observableArrayList(
                GanaderiaDataStore.getRegistrosSanitariosPorFinca(a.getFincaId()).stream()
                        .filter(r -> r.getAnimalId() == a.getId() || (r.getArete() != null && r.getArete().equals(a.getArete())))
                        .collect(Collectors.toList()));
        tblSanidad.setItems(regAnimal);

        // Productivos
        lblGdp.setText(a.getGdpFormateado());
        lblLeche.setText(a.getProduccionLecheDiaria() > 0
                ? String.format("%.1f L/día", a.getProduccionLecheDiaria()) : "—");
        lblUsoRec.setText(safe(a.getUsoRecomendado(), "—"));
        lblPartos.setText(String.valueOf(a.getNumeroPartos()));
        lblGestacion.setText(safe(a.getEstadoGestacion(), "—"));
        lblUltParto.setText(formatFecha(a.getUltimoParto()));
        lblCalidad.setText(safe(a.getCalidadGenetica(), "—"));
        lblLinea.setText(safe(a.getLineaGenetica(), "—"));
        lblProxServ.setText(formatFecha(a.getProximoServicio()));
        lblPesoResumen.setText(String.format("Inicial: %.1f kg · Actual: %.1f kg · Ganancia: %.1f kg · GDP: %s",
                a.getPesoInicial(), a.getPesoActual(), a.getGananciaTotal(), a.getGdpFormateado()));

        // Documentos
        lblIca.setText(textoDoc(a.getRegistroIcaNumero()));
        lblRegGen.setText(textoDoc(a.getRegistroGenealogico()));
        lblGuiaSan.setText(textoDoc(a.getGuiaSanitariaPath()));
        lblCertVac.setText(textoDoc(a.getCertificadoVacunacionPath()));
        lblRegProp.setText(textoDoc(a.getRegistroPropiedadPath()));
        lblFactura.setText(textoDoc(a.getFacturaCompraPath()));

        List<String> docs = a.getDocumentosAdjuntos();
        lvDocumentos.setItems(FXCollections.observableArrayList(docs != null ? docs : List.of()));
        boolean sinDocs = docs == null || docs.isEmpty();
        lblDocVacios.setVisible(sinDocs);
        lblDocVacios.setManaged(sinDocs);
        lvDocumentos.setVisible(!sinDocs);
        lvDocumentos.setManaged(!sinDocs);
    }

    private void configurarTablaVacunas() {
        colVacTipo.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().getNombreVacuna(), "—")));
        colVacFecha.setCellValueFactory(c -> new SimpleStringProperty(formatFecha(c.getValue().getFechaAplicacion())));
        colVacProx.setCellValueFactory(c -> new SimpleStringProperty(formatFecha(c.getValue().getProximaDosis())));
        colVacResp.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().getResponsable(), "—")));
        colVacEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
    }

    private void configurarTablaSanidad() {
        colSanFecha.setCellValueFactory(c -> new SimpleStringProperty(formatFecha(c.getValue().getFecha())));
        colSanDiag.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue().getTipo() != null ? c.getValue().getTipo().name() + " · " : "") + safe(c.getValue().getDescripcion(), "—")));
        colSanTrat.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().getObservaciones(), "—")));
        colSanVet.setCellValueFactory(c -> new SimpleStringProperty(safe(c.getValue().getResponsable(), "—")));
        colSanEstado.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().esVencido() ? "Vencido" : (c.getValue().estaProximo() ? "Próximo" : "Vigente")));
    }

    private VBox tarjetaFoto(String titulo, String path) {
        VBox v = new VBox(4);
        v.setPrefSize(150, 130);
        v.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e2e8f0; -fx-border-width: 1;"
                + " -fx-background-color: linear-gradient(to bottom right, #d1fae5, #1f7a63);");
        v.setPadding(new Insets(8));
        StackPane sp = new StackPane(new Label("📷"));
        sp.setPrefSize(150, 90);
        sp.getChildren().get(0).setStyle("-fx-font-size: 40px;");
        Label l = new Label(titulo);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lp = new Label(path);
        lp.setStyle("-fx-font-size: 9px; -fx-text-fill: #f1f5f9;");
        lp.setWrapText(true);
        v.getChildren().addAll(sp, l, lp);
        return v;
    }

    private void aplicarBadge(Label l, String texto, String clase) {
        l.setText(texto);
        l.getStyleClass().setAll(clase.split(" "));
    }

    private String badgeOk(String estado) {
        if (estado == null) return "ficha-badge ficha-badge-ok";
        return switch (estado) {
            case "Rentable", "Mantener" -> "ficha-badge ficha-badge-ok";
            case "Bajo"                  -> "ficha-badge ficha-badge-warn";
            case "No Rentable"           -> "ficha-badge ficha-badge-danger";
            default                       -> "ficha-badge ficha-badge-info";
        };
    }

    private String badgeSanidad(String estado) {
        if (estado == null) return "ficha-badge ficha-badge-ok";
        String s = estado.toLowerCase();
        if (s.contains("salud")) return "ficha-badge ficha-badge-ok";
        if (s.contains("observ") || s.contains("trat")) return "ficha-badge ficha-badge-warn";
        return "ficha-badge ficha-badge-info";
    }

    private String safe(String s, String fallback) {
        return s == null || s.isBlank() ? fallback : s;
    }

    private String textoDoc(String s) {
        return s == null || s.isBlank() ? "No adjuntado" : "📎 " + s;
    }

    private String formatFecha(LocalDate d) {
        return d == null ? "—" : d.format(F_FECHA);
    }

    @FXML
    private void cerrar() {
        ((Stage) lblTitulo.getScene().getWindow()).close();
    }
}
