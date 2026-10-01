package com.sistemaganadero.controller;

import com.sistemaganadero.model.Usuario;
import com.sistemaganadero.service.AuthService;
import com.sistemaganadero.util.NotificationService;
import com.sistemaganadero.util.Validators;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class RecuperarClaveController {

    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblMensajeError;

    @FXML private VBox paneStep1;
    @FXML private VBox paneStep2;
    @FXML private VBox paneStep3;

    @FXML private RadioButton rbCorreo;
    @FXML private RadioButton rbTelefono;
    @FXML private Label lblIdent;
    @FXML private TextField txtIdentificacion;

    @FXML private Label lblDestino;
    @FXML private TextField txtCodigo;

    @FXML private PasswordField txtNueva;
    @FXML private PasswordField txtNueva2;

    @FXML private Label lblStep1;
    @FXML private Label lblStep2;
    @FXML private Label lblStep3;

    private final AuthService authService = new AuthService();
    private Usuario usuarioRecuperar;
    private int codigoGenerado;

    @FXML
    public void initialize() {
        ToggleGroup grupo = new ToggleGroup();
        rbCorreo.setToggleGroup(grupo);
        rbTelefono.setToggleGroup(grupo);
        rbCorreo.selectedProperty().addListener((o, a, b) -> actualizarLabelIdent());
        actualizarLabelIdent();

        txtCodigo.textProperty().addListener((o, a, b) -> {
            if (b != null && !b.matches("[0-9]*")) txtCodigo.setText(a);
            if (b != null && b.length() > 6) txtCodigo.setText(b.substring(0, 6));
        });
        mostrarPaso(1);
    }

    private void actualizarLabelIdent() {
        if (rbCorreo.isSelected()) {
            lblIdent.setText("Correo electrónico");
            txtIdentificacion.setPromptText("correo@ejemplo.com");
        } else {
            lblIdent.setText("Teléfono");
            txtIdentificacion.setPromptText("+57 300 000 0000");
        }
    }

    @FXML
    private void enviarCodigo() {
        ocultarError();
        String valor = txtIdentificacion.getText() == null ? "" : txtIdentificacion.getText().trim();
        if (Validators.isBlank(valor)) { mostrarError("Ingresa tu correo o teléfono."); return; }

        Optional<Usuario> u = rbCorreo.isSelected()
                ? authService.buscarPorEmail(valor)
                : authService.buscarPorTelefono(valor);
        if (u.isEmpty()) { mostrarError("No encontramos una cuenta con esos datos."); return; }

        usuarioRecuperar = u.get();
        codigoGenerado = 100000 + (int) (Math.random() * 900000);

        String destino = rbCorreo.isSelected()
                ? "correo " + enmascararEmail(usuarioRecuperar.getEmail())
                : "teléfono " + enmascararTelefono(usuarioRecuperar.getTelefono());
        lblDestino.setText("Te enviamos un código a tu " + destino
                + ".\n(Código demo: " + codigoGenerado + ")");
        mostrarPaso(2);
    }

    @FXML
    private void reenviarCodigo() {
        if (usuarioRecuperar == null) { mostrarPaso(1); return; }
        codigoGenerado = 100000 + (int) (Math.random() * 900000);
        lblDestino.setText("Reenviamos el código.\n(Código demo: " + codigoGenerado + ")");
        NotificationService.info("Código reenviado.");
    }

    @FXML
    private void verificarCodigo() {
        ocultarError();
        String c = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        if (c.length() != 6) { mostrarError("El código debe tener 6 dígitos."); return; }
        if (!String.valueOf(codigoGenerado).equals(c)) { mostrarError("Código incorrecto."); return; }
        mostrarPaso(3);
    }

    @FXML
    private void guardarNueva() {
        ocultarError();
        String n1 = txtNueva.getText();
        String n2 = txtNueva2.getText();
        if (n1 == null || n1.length() < 6) { mostrarError("La contraseña debe tener al menos 6 caracteres."); return; }
        if (!n1.equals(n2)) { mostrarError("Las contraseñas no coinciden."); return; }
        authService.cambiarPassword(usuarioRecuperar, n1);
        NotificationService.success("Contraseña actualizada. Ya puedes iniciar sesión.");
        volverLogin();
    }

    @FXML
    private void volverLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            Stage stage = (Stage) lblTitulo.getScene().getWindow();
            stage.setTitle("Sistema Ganadero — Acceso");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarError("No se pudo volver al login: " + e.getMessage());
        }
    }

    private void mostrarPaso(int paso) {
        toggle(paneStep1, paso == 1);
        toggle(paneStep2, paso == 2);
        toggle(paneStep3, paso == 3);
        lblTitulo.setText(switch (paso) {
            case 2 -> "Verificar identidad";
            case 3 -> "Nueva contraseña";
            default -> "Buscar mi cuenta";
        });
        lblSubtitulo.setText(switch (paso) {
            case 2 -> "Ingresa el código de 6 dígitos que recibiste";
            case 3 -> "Define una contraseña segura para " + (usuarioRecuperar != null ? usuarioRecuperar.getNombre() : "tu cuenta");
            default -> "Indica si quieres recuperar por correo o por teléfono";
        });
        actualizarIndicadoresPaso(paso);
    }

    private void actualizarIndicadoresPaso(int paso) {
        marcarPaso(lblStep1, paso >= 1, paso == 1);
        marcarPaso(lblStep2, paso >= 2, paso == 2);
        marcarPaso(lblStep3, paso >= 3, paso == 3);
    }

    private void marcarPaso(Label l, boolean alcanzado, boolean actual) {
        if (l == null) return;
        if (actual) l.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        else if (alcanzado) l.setStyle("-fx-text-fill: rgba(255,255,255,0.95);");
        else l.setStyle("-fx-text-fill: rgba(255,255,255,0.55);");
    }

    private void toggle(VBox v, boolean visible) {
        v.setVisible(visible);
        v.setManaged(visible);
    }

    private String enmascararEmail(String email) {
        if (email == null) return "(sin correo)";
        int at = email.indexOf('@');
        if (at <= 1) return email;
        return email.charAt(0) + "***" + email.substring(at);
    }

    private String enmascararTelefono(String t) {
        if (t == null || t.length() < 4) return "(sin teléfono)";
        return "*** " + t.substring(t.length() - 4);
    }

    private void mostrarError(String msg) {
        lblMensajeError.setText(msg);
        lblMensajeError.setVisible(true);
        lblMensajeError.setManaged(true);
    }

    private void ocultarError() {
        lblMensajeError.setVisible(false);
        lblMensajeError.setManaged(false);
    }
}
