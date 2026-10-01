package com.sistemaganadero.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestor central de conexiones multi-motor (MySQL + PostgreSQL + MongoDB).
 * Lee config desde config/database.properties con fallback graceful a modo demo.
 *
 * MySQL/PostgreSQL: datos estructurados relacionales.
 * MongoDB: backups, historiales, logs, datos no estructurados.
 */
public class DatabaseManager {

    private static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());

    private static String mysqlUrl, mysqlUser, mysqlPass;
    private static String pgUrl, pgUser, pgPass;
    private static String mongoUri, mongoDb;

    private static boolean mysqlOk, pgOk, mongoOk;
    private static boolean loaded = false;

    private static MongoClient mongoClient;

    private DatabaseManager() {}

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;

        Properties p = new Properties();
        boolean cargado = false;
        try (InputStream is = new FileInputStream("config/database.properties")) {
            p.load(is);
            cargado = true;
        } catch (Exception ignored) {
            try (InputStream is = DatabaseManager.class.getResourceAsStream("/database.properties")) {
                if (is != null) { p.load(is); cargado = true; }
            } catch (Exception ignored2) {}
        }
        if (!cargado) {
            LOG.info("Sin config/database.properties — modo demo (sin persistencia externa).");
            return;
        }

        // MySQL
        mysqlUrl = trim(p.getProperty("mysql.url"));
        mysqlUser = trim(p.getProperty("mysql.user"));
        mysqlPass = trim(p.getProperty("mysql.password"));
        if (mysqlUrl.isEmpty()) { // compat clave antigua
            mysqlUrl = trim(p.getProperty("db.url"));
            mysqlUser = trim(p.getProperty("db.user"));
            mysqlPass = trim(p.getProperty("db.password"));
        }
        if (!mysqlUrl.isEmpty()) probarMysql();

        // PostgreSQL
        pgUrl = trim(p.getProperty("postgresql.url"));
        pgUser = trim(p.getProperty("postgresql.user"));
        pgPass = trim(p.getProperty("postgresql.password"));
        if (!pgUrl.isEmpty()) probarPostgres();

        // MongoDB
        mongoUri = trim(p.getProperty("mongodb.uri"));
        mongoDb = trim(p.getProperty("mongodb.database", "sistemaganadero"));
        if (!mongoUri.isEmpty()) probarMongo();

        if (mysqlOk || pgOk || mongoOk) {
            inicializarEsquemas();
        }
    }

    private static void probarMysql() {
        try (Connection c = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass)) {
            mysqlOk = c.isValid(5);
            LOG.info("MySQL conectado: " + mysqlUrl);
        } catch (Exception e) {
            mysqlOk = false;
            LOG.log(Level.WARNING, "MySQL no disponible: " + e.getMessage());
        }
    }

    private static void probarPostgres() {
        try (Connection c = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            pgOk = c.isValid(5);
            LOG.info("PostgreSQL conectado: " + pgUrl);
        } catch (Exception e) {
            pgOk = false;
            LOG.log(Level.WARNING, "PostgreSQL no disponible: " + e.getMessage());
        }
    }

    private static void probarMongo() {
        try {
            mongoClient = MongoClients.create(mongoUri);
            mongoClient.getDatabase(mongoDb).runCommand(new Document("ping", 1));
            mongoOk = true;
            LOG.info("MongoDB conectado: " + mongoDb);
        } catch (Exception e) {
            mongoOk = false;
            if (mongoClient != null) { try { mongoClient.close(); } catch (Exception ignored) {} }
            mongoClient = null;
            LOG.log(Level.WARNING, "MongoDB no disponible: " + e.getMessage());
        }
    }

    /** Crea tablas/colecciones necesarias si no existen. No falla si un motor no está disponible. */
    private static void inicializarEsquemas() {
        if (mysqlOk) crearTablasSql(mysqlUrl, mysqlUser, mysqlPass, "MySQL");
        if (pgOk) crearTablasSql(pgUrl, pgUser, pgPass, "PostgreSQL");
        if (mongoOk) crearColeccionesMongo();
    }

    private static void crearTablasSql(String url, String user, String pass, String motor) {
        try (Connection c = DriverManager.getConnection(url, user, pass);
             Statement s = c.createStatement()) {
            // Marcador de auto-init — solo las tablas no destructivas que faltan.
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auditoria_ediciones (
                    id BIGINT PRIMARY KEY,
                    tabla_modificada VARCHAR(80) NOT NULL,
                    registro_id VARCHAR(80) NOT NULL,
                    usuario VARCHAR(120),
                    fecha_modificacion TIMESTAMP NOT NULL
                )
            """);
            LOG.info(motor + ": esquemas verificados/creados");
        } catch (SQLException e) {
            LOG.log(Level.WARNING, motor + ": no se pudieron crear tablas: " + e.getMessage());
        }
    }

    private static void crearColeccionesMongo() {
        try {
            MongoDatabase db = mongoClient.getDatabase(mongoDb);
            // listCollectionNames materializa la lista; si "backups" no está, la creamos.
            boolean existe = false;
            for (String n : db.listCollectionNames()) if ("backups".equals(n)) { existe = true; break; }
            if (!existe) {
                db.createCollection("backups");
                LOG.info("MongoDB: colección 'backups' creada");
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "MongoDB: no se pudieron crear colecciones: " + e.getMessage());
        }
    }

    public static Connection mysqlConnection() throws SQLException {
        load();
        if (!mysqlOk) throw new SQLException("MySQL no disponible (revisa config/database.properties)");
        return DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
    }

    public static Connection postgresConnection() throws SQLException {
        load();
        if (!pgOk) throw new SQLException("PostgreSQL no disponible (revisa config/database.properties)");
        return DriverManager.getConnection(pgUrl, pgUser, pgPass);
    }

    public static MongoDatabase mongoDatabase() {
        load();
        if (!mongoOk) return null;
        return mongoClient.getDatabase(mongoDb);
    }

    public static MongoCollection<Document> mongoCollection(String nombre) {
        MongoDatabase db = mongoDatabase();
        return db == null ? null : db.getCollection(nombre);
    }

    public static boolean isMysqlAvailable() { load(); return mysqlOk; }
    public static boolean isPostgresAvailable() { load(); return pgOk; }
    public static boolean isMongoAvailable() { load(); return mongoOk; }
    public static String estadoMotores() {
        load();
        return String.format("MySQL: %s · PostgreSQL: %s · MongoDB: %s",
                mysqlOk ? "✓" : "—", pgOk ? "✓" : "—", mongoOk ? "✓" : "—");
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
}
