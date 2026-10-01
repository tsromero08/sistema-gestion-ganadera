package com.sistemaganadero.service;

import com.sistemaganadero.model.Usuario;
import com.sistemaganadero.util.PasswordHash;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Optional;

public class AuthService {

    private static final ObservableList<Usuario> USUARIOS = FXCollections.observableArrayList();
    private static boolean inicializado = false;

    public AuthService() {
        inicializarDemoUsers();
    }

    private static synchronized void inicializarDemoUsers() {
        if (inicializado) return;
        agregar(crear("admin",       "admin123", "Administrador General",   Usuario.Rol.ADMINISTRADOR,    "admin@sistemaganadero.co",     "+57 301 389 6861"));
        agregar(crear("operario",    "1234",     "Operario Principal",      Usuario.Rol.OPERARIO,         "operario@sistemaganadero.co",  "+57 311 222 3344"));
        agregar(crear("veterinario", "vet123",   "Dr. Veterinario de Campo",Usuario.Rol.VETERINARIO,      "vet@sistemaganadero.co",       "+57 312 555 1212"));
        agregar(crear("vendedor",    "vend123",  "Carlos Rodríguez (Vendedor)", Usuario.Rol.VENDEDOR_GANADERO, "carlos@sistemaganadero.co",   "+57 310 555 1234"));
        agregar(crear("comprador",   "comp123",  "Juan Pérez (Comprador)",  Usuario.Rol.COMPRADOR,        "juan@sistemaganadero.co",      "+57 315 444 5566"));
        inicializado = true;
    }

    private static Usuario crear(String username, String passPlain, String nombre, Usuario.Rol rol, String email, String tel) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPasswordHash(PasswordHash.hash(passPlain));
        u.setNombre(nombre);
        u.setRolEnum(rol);
        u.setEmail(email);
        u.setTelefono(tel);
        u.setDepartamento("Colombia");
        u.setCiudad("—");
        return u;
    }

    private static void agregar(Usuario u) { USUARIOS.add(u); }

    public Optional<Usuario> autenticar(String usernameOrEmail, String passwordPlain) {
        if (usernameOrEmail == null || passwordPlain == null) return Optional.empty();
        String q = usernameOrEmail.trim().toLowerCase();
        return USUARIOS.stream()
                .filter(Usuario::isActivo)
                .filter(u -> (u.getUsername() != null && u.getUsername().equalsIgnoreCase(q))
                        || (u.getEmail() != null && u.getEmail().equalsIgnoreCase(q)))
                .filter(u -> PasswordHash.matches(passwordPlain, u.getPasswordHash()))
                .findFirst();
    }

    public boolean existeUsuario(String username, String email) {
        return USUARIOS.stream().anyMatch(u ->
                (username != null && u.getUsername() != null && u.getUsername().equalsIgnoreCase(username.trim()))
             || (email != null && u.getEmail() != null && email.trim().length() > 0 && u.getEmail().equalsIgnoreCase(email.trim())));
    }

    public Usuario registrar(Usuario datos, String passwordPlain) {
        datos.setPasswordHash(PasswordHash.hash(passwordPlain));
        datos.setActivo(true);
        USUARIOS.add(datos);
        return datos;
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        return USUARIOS.stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    public Optional<Usuario> buscarPorTelefono(String telefono) {
        if (telefono == null) return Optional.empty();
        String norm = telefono.replaceAll("[^0-9]", "");
        return USUARIOS.stream()
                .filter(u -> u.getTelefono() != null && u.getTelefono().replaceAll("[^0-9]", "").endsWith(norm.length() >= 7 ? norm.substring(Math.max(0, norm.length() - 7)) : norm))
                .findFirst();
    }

    public void cambiarPassword(Usuario u, String nuevaPlain) {
        u.setPasswordHash(PasswordHash.hash(nuevaPlain));
    }

    public static ObservableList<Usuario> getUsuarios() {
        if (!inicializado) inicializarDemoUsers();
        return USUARIOS;
    }
}
