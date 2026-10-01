package com.sistemaganadero.controller;

import com.sistemaganadero.model.Usuario;
import com.sistemaganadero.service.AuthService;
import com.sistemaganadero.util.Session;
import com.sistemaganadero.util.Validators;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private Label lblMensajeError;

    private final AuthService authService = new AuthService();

    @FXML
    private void iniciarSesion() {
        ocultarError();
        String usuario = txtUsuario.getText() == null ? "" : txtUsuario.getText().trim();
        String clave = txtClave.getText() == null ? "" : txtClave.getText();

        if (Validators.isBlank(usuario) || Validators.isBlank(clave)) {
            mostrarError("Debes ingresar usuario y contraseña.");
            return;
        }
        if (clave.length() < 4) {
            mostrarError("La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        Optional<Usuario> resultado = authService.autenticar(usuario, clave);
        if (resultado.isEmpty()) {
            mostrarError("Usuario o contraseña incorrectos. Verifica tus datos.");
            return;
        }
        Usuario u = resultado.get();
        if (!u.isActivo()) {
            mostrarError("Tu cuenta está bloqueada. Contacta al administrador.");
            return;
        }
        abrirSistema(u);
    }

    private void abrirSistema(Usuario usuario) {
        try {
            Session.setUsuarioActual(usuario);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_layout.fxml"));
            Scene scene = new Scene(loader.load(), 1300, 820);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setTitle("Sistema Ganadero — " + usuario.getNombre() + " (" + usuario.getRol() + ")");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarError("No se pudo abrir el sistema: " + e.getMessage());
        }
    }

    @FXML
    private void abrirRegistro() {
        cargarVistaAuth("/view/registro.fxml", "Sistema Ganadero — Crear cuenta", 1280, 780);
    }

    @FXML
    private void recuperarClave() {
        cargarVistaAuth("/view/recuperar_clave.fxml", "Sistema Ganadero — Recuperar contraseña", 1280, 720);
    }

    private void cargarVistaAuth(String recurso, String titulo, double ancho, double alto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(recurso));
            Scene scene = new Scene(loader.load(), ancho, alto);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarError("No se pudo abrir la vista: " + e.getMessage());
        }
    }

    private void mostrarError(String msg) {
        if (lblMensajeError == null) return;
        lblMensajeError.setText(msg);
        lblMensajeError.setVisible(true);
        lblMensajeError.setManaged(true);
    }

    private void ocultarError() {
        if (lblMensajeError == null) return;
        lblMensajeError.setVisible(false);
        lblMensajeError.setManaged(false);
    }

}
