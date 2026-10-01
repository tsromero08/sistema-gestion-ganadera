package com.sistemaganadero.controller;

import com.sistemaganadero.model.Usuario;
import com.sistemaganadero.service.AuthService;
import com.sistemaganadero.util.NotificationService;
import com.sistemaganadero.util.Validators;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegistroController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbDepartamento;
    @FXML private TextField txtCiudad;
    @FXML private ComboBox<Usuario.Rol> cmbRol;
    @FXML private Label lblRolHint;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private PasswordField txtClave2;
    @FXML private CheckBox chkTerminos;
    @FXML private Label lblMensajeError;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        cmbDepartamento.setItems(FXCollections.observableArrayList(
                "Cundinamarca", "Antioquia", "Valle del Cauca", "Atlántico", "Bolívar",
                "Boyacá", "Caldas", "Caquetá", "Casanare", "Cesar", "Chocó", "Córdoba",
                "Huila", "La Guajira", "Magdalena", "Meta", "Nariño", "Norte de Santander",
                "Quindío", "Risaralda", "Santander", "Sucre", "Tolima"));

        cmbRol.setItems(FXCollections.observableArrayList(
                Usuario.Rol.VENDEDOR_GANADERO,
                Usuario.Rol.COMPRADOR,
                Usuario.Rol.OPERARIO,
                Usuario.Rol.VETERINARIO));
        cmbRol.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Usuario.Rol r) { return r == null ? "" : r.getEtiqueta(); }
            @Override public Usuario.Rol fromString(String s) { return Usuario.Rol.desdeEtiqueta(s); }
        });
        cmbRol.getSelectionModel().select(Usuario.Rol.COMPRADOR);
        cmbRol.valueProperty().addListener((o, a, b) -> actualizarHintRol(b));
        actualizarHintRol(cmbRol.getValue());
    }

    private void actualizarHintRol(Usuario.Rol rol) {
        if (rol == null) { lblRolHint.setText(""); return; }
        lblRolHint.setText(switch (rol) {
            case VENDEDOR_GANADERO -> "Puedes publicar bovinos, definir precios, ver ofertas y aceptar negociaciones.";
            case COMPRADOR         -> "Puedes ver animales, filtrar por ubicación, hacer pujas y guardar favoritos.";
            case OPERARIO          -> "Puedes registrar pesajes, vacunas, movimientos y operación diaria de la finca.";
            case VETERINARIO       -> "Puedes registrar sanidad, tratamientos, diagnósticos y plan veterinario.";
            default                -> "";
        });
    }

    @FXML
    private void registrar() {
        ocultarError();
        String nombre = safe(txtNombre);
        String doc = safe(txtDocumento);
        String tel = safe(txtTelefono);
        String email = safe(txtEmail);
        String depto = cmbDepartamento.getValue() != null ? cmbDepartamento.getValue().trim() : "";
        String ciudad = safe(txtCiudad);
        String usuario = safe(txtUsuario);
        String clave = txtClave.getText() == null ? "" : txtClave.getText();
        String clave2 = txtClave2.getText() == null ? "" : txtClave2.getText();
        Usuario.Rol rol = cmbRol.getValue();

        String err = validar(nombre, usuario, email, tel, clave, clave2, rol);
        if (err != null) { mostrarError(err); return; }
        if (!chkTerminos.isSelected()) { mostrarError("Debes aceptar los términos del servicio."); return; }

        if (authService.existeUsuario(usuario, email)) {
            mostrarError("Ya existe una cuenta con ese usuario o correo. Intenta iniciar sesión.");
            return;
        }

        Usuario u = new Usuario();
        u.setUsername(usuario);
        u.setNombre(nombre);
        u.setDocumento(doc);
        u.setEmail(email);
        u.setTelefono(tel);
        u.setDepartamento(depto);
        u.setCiudad(ciudad);
        u.setRolEnum(rol);
        authService.registrar(u, clave);

        NotificationService.success("Cuenta creada: " + u.getUsername() + ". Ya puedes iniciar sesión.");
        volverLogin();
    }

    private String validar(String nombre, String usuario, String email, String tel,
                           String clave, String clave2, Usuario.Rol rol) {
        if (Validators.isBlank(nombre))            return "Ingresa tu nombre completo.";
        if (Validators.isBlank(usuario) || usuario.length() < 3)
                                                   return "El usuario debe tener al menos 3 caracteres.";
        if (usuario.contains(" "))                 return "El usuario no debe contener espacios.";
        if (!Validators.emailValido(email))        return "El correo electrónico no es válido.";
        if (!Validators.telefonoValido(tel))       return "El teléfono no es válido. Usa formato +57 300 000 0000.";
        if (clave == null || clave.length() < 6)   return "La contraseña debe tener al menos 6 caracteres.";
        if (!clave.equals(clave2))                 return "Las contraseñas no coinciden.";
        if (rol == null)                           return "Selecciona el tipo de usuario.";
        return null;
    }

    @FXML
    private void volverLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            Stage stage = (Stage) txtNombre.getScene().getWindow();
            stage.setTitle("Sistema Ganadero — Acceso");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarError("No se pudo volver al login: " + e.getMessage());
        }
    }

    private String safe(TextField t) { return t.getText() == null ? "" : t.getText().trim(); }

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
