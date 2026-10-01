package com.sistemaganadero.database;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.logging.Logger;

public class DatabaseConnection {

    private static final Logger LOG = Logger.getLogger(DatabaseConnection.class.getName());
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;
    private static boolean configured = false;
    private static boolean loadAttempted = false;

    private DatabaseConnection() {}

    private static void loadConfig() {
        if (loadAttempted) return;
        loadAttempted = true;
        Properties props = new Properties();
        try (InputStream is = new FileInputStream("config/database.properties")) {
            props.load(is);
        } catch (Exception e1) {
            try (InputStream is = DatabaseConnection.class.getResourceAsStream("/database.properties")) {
                if (is == null) {
                    LOG.info("Sin configuración de base de datos. Modo demo activo.");
                    return;
                }
                props.load(is);
            } catch (Exception e2) {
                LOG.info("Sin configuración de base de datos. Modo demo activo.");
                return;
            }
        }
        dbUrl = props.getProperty("db.url", "").trim();
        dbUser = props.getProperty("db.user", "").trim();
        dbPassword = props.getProperty("db.password", "").trim();
        configured = !dbUrl.isEmpty() && !dbUser.isEmpty();
        if (configured) LOG.info("Base de datos configurada: " + dbUrl);
    }

    public static boolean isConfigured() {
        loadConfig();
        return configured;
    }

    public static Connection getConnection() throws Exception {
        loadConfig();
        if (!configured) throw new IllegalStateException("Base de datos no configurada. Edite config/database.properties");
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public static void testConnection() throws Exception {
        try (Connection conn = getConnection()) {
            if (!conn.isValid(5)) throw new Exception("Conexión inválida");
        }
    }
}
