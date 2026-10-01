package com.sistemaganadero.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.sistemaganadero.database.DatabaseManager;
import com.sistemaganadero.util.JsonUtil;
import com.sistemaganadero.util.Session;
import org.bson.Document;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio central de persistencia multi-motor.
 *
 * Regla del prompt v4:
 *  - El usuario elige en qué motor (MySQL / PostgreSQL / MongoDB) se crea cada registro.
 *  - MongoDB SIEMPRE recibe una copia del registro (replicación obligatoria).
 *  - Al editar: backup del estado anterior va a Mongo; los cambios se aplican en el motor donde vive el dato.
 *  - Al eliminar: backup va a Mongo antes de borrar; en Mongo NUNCA se elimina el respaldo.
 *
 * Esquema SQL genérico (se autocrea en MySQL y PostgreSQL):
 *   entidades(
 *     id           VARCHAR(120),
 *     tabla        VARCHAR(80),
 *     datos_json   TEXT,
 *     fecha        TIMESTAMP,
 *     usuario      VARCHAR(120),
 *     PRIMARY KEY (tabla, id)
 *   )
 *
 * Esquema MongoDB:
 *   db.{tabla}      → documentos vivos
 *   db.backups      → snapshots de todas las ediciones / eliminaciones
 */
public final class PersistenceService {

    private static final Logger LOG = Logger.getLogger(PersistenceService.class.getName());

    public enum Motor {
        MYSQL("MySQL"), POSTGRESQL("PostgreSQL"), MONGODB("MongoDB");
        private final String etiqueta;
        Motor(String e) { this.etiqueta = e; }
        public String getEtiqueta() { return etiqueta; }
        @Override public String toString() { return etiqueta; }
    }

    public enum Resultado {
        OK("Guardado correctamente"),
        OK_FALLBACK("Guardado en archivo local — motor remoto no disponible"),
        SOLO_MEMORIA("Sólo en memoria — ningún motor activo (modo demo)"),
        ERROR("Error al persistir");
        private final String mensaje;
        Resultado(String m) { this.mensaje = m; }
        public String getMensaje() { return mensaje; }
    }

    private PersistenceService() {}

    // ===================== CREAR =====================

    /**
     * Persiste un registro NUEVO en el motor elegido y replica a MongoDB.
     * @return resultado de la operación (OK, OK_FALLBACK, SOLO_MEMORIA, ERROR)
     */
    public static Resultado crear(String tabla, String idRegistro, Object registro, Motor motor) {
        String json = JsonUtil.toJson(JsonUtil.toMap(registro));
        boolean okMotor = persistirEnMotor(tabla, idRegistro, json, motor, false);
        boolean okMongo = replicarEnMongo(tabla, idRegistro, registro, "CREAR");

        BackupService.guardarJson(tabla, idRegistro, json);  // siempre auditoría

        if (!okMotor && !okMongo) return Resultado.SOLO_MEMORIA;
        if (!okMotor) return Resultado.OK_FALLBACK;
        return Resultado.OK;
    }

    // ===================== EDITAR =====================

    /**
     * Edita un registro existente. Antes de modificar, guarda backup del estado ANTERIOR en Mongo.
     */
    public static Resultado editar(String tabla, String idRegistro, Object estadoAnterior, Object estadoNuevo, Motor motor) {
        // 1. Backup ANTES (estado anterior)
        BackupService.guardar(tabla, idRegistro, estadoAnterior);
        // 2. Replica estado anterior en backups Mongo con marca "EDITAR"
        replicarEnBackupsMongo(tabla, idRegistro, estadoAnterior, "EDITAR-PRE");
        // 3. Actualizar en motor
        String jsonNuevo = JsonUtil.toJson(JsonUtil.toMap(estadoNuevo));
        boolean okMotor = persistirEnMotor(tabla, idRegistro, jsonNuevo, motor, true);
        // 4. Reflejar nuevo estado en colección viva de Mongo
        boolean okMongo = replicarEnMongo(tabla, idRegistro, estadoNuevo, "EDITAR-POST");

        if (!okMotor && !okMongo) return Resultado.SOLO_MEMORIA;
        if (!okMotor) return Resultado.OK_FALLBACK;
        return Resultado.OK;
    }

    // ===================== ELIMINAR =====================

    /**
     * Elimina un registro del motor especificado.
     * Antes de eliminar: backup completo en Mongo (NUNCA se borra de la colección backups).
     * Si motor != MongoDB, también elimina de la colección viva de Mongo manteniendo el respaldo.
     */
    public static Resultado eliminar(String tabla, String idRegistro, Object registro, Motor motor) {
        // 1. Backup OBLIGATORIO en Mongo (queda permanentemente)
        BackupService.guardar(tabla, idRegistro, registro);
        replicarEnBackupsMongo(tabla, idRegistro, registro, "ELIMINAR");

        boolean okMotor;
        switch (motor) {
            case MYSQL ->      okMotor = ejecutarDelete(tabla, idRegistro, true);
            case POSTGRESQL -> okMotor = ejecutarDelete(tabla, idRegistro, false);
            case MONGODB ->    okMotor = eliminarDeMongoVivo(tabla, idRegistro);
            default ->         okMotor = false;
        }

        // Si el motor era SQL, también eliminar de la colección viva de Mongo (el backup queda)
        if (motor != Motor.MONGODB) eliminarDeMongoVivo(tabla, idRegistro);

        if (!okMotor) return Resultado.SOLO_MEMORIA;
        return Resultado.OK;
    }

    // ===================== INTERNOS =====================

    private static boolean persistirEnMotor(String tabla, String id, String json, Motor motor, boolean esUpdate) {
        return switch (motor) {
            case MYSQL ->      upsertSql(tabla, id, json, true);
            case POSTGRESQL -> upsertSql(tabla, id, json, false);
            case MONGODB ->    upsertMongoVivo(tabla, id, json);
        };
    }

    private static boolean upsertSql(String tabla, String id, String json, boolean esMysql) {
        boolean disponible = esMysql ? DatabaseManager.isMysqlAvailable() : DatabaseManager.isPostgresAvailable();
        if (!disponible) return false;
        String usuario = Session.getUsuarioActual() != null ? Session.getUsuarioActual().getUsername() : "anonimo";
        try (Connection c = esMysql ? DatabaseManager.mysqlConnection() : DatabaseManager.postgresConnection()) {
            // Asegurar tabla
            try (Statement s = c.createStatement()) {
                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS entidades (
                        id VARCHAR(120) NOT NULL,
                        tabla VARCHAR(80) NOT NULL,
                        datos_json TEXT NOT NULL,
                        fecha TIMESTAMP NOT NULL,
                        usuario VARCHAR(120),
                        PRIMARY KEY (tabla, id)
                    )
                """);
            }
            // Borrar previo y reinsertar (compatible MySQL y PostgreSQL sin upsert nativo común)
            try (PreparedStatement del = c.prepareStatement("DELETE FROM entidades WHERE tabla=? AND id=?")) {
                del.setString(1, tabla); del.setString(2, id); del.executeUpdate();
            }
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO entidades (id, tabla, datos_json, fecha, usuario) VALUES (?, ?, ?, ?, ?)")) {
                ins.setString(1, id);
                ins.setString(2, tabla);
                ins.setString(3, json);
                ins.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ins.setString(5, usuario);
                ins.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, (esMysql ? "MySQL" : "PostgreSQL") + " no pudo persistir " + tabla + "/" + id + ": " + e.getMessage());
            return false;
        }
    }

    private static boolean upsertMongoVivo(String tabla, String id, String json) {
        if (!DatabaseManager.isMongoAvailable()) return false;
        try {
            MongoCollection<Document> col = DatabaseManager.mongoCollection(tabla);
            if (col == null) return false;
            Document doc = Document.parse(json);
            doc.put("_entityId", id);
            col.deleteMany(Filters.eq("_entityId", id));
            col.insertOne(doc);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "MongoDB no pudo persistir " + tabla + "/" + id + ": " + e.getMessage());
            return false;
        }
    }

    /** Replica el registro vivo en MongoDB (colección por entidad). Ejecuta SIEMPRE además del motor elegido. */
    private static boolean replicarEnMongo(String tabla, String id, Object registro, String origen) {
        if (!DatabaseManager.isMongoAvailable()) return false;
        String json = JsonUtil.toJson(JsonUtil.toMap(registro));
        boolean ok = upsertMongoVivo(tabla, id, json);
        if (ok) LOG.fine("Replicado en MongoDB [" + origen + "] " + tabla + "/" + id);
        return ok;
    }

    /** Guarda snapshot histórico en la colección backups de Mongo con marca de origen. */
    private static void replicarEnBackupsMongo(String tabla, String id, Object registro, String origen) {
        if (!DatabaseManager.isMongoAvailable()) return;
        try {
            MongoCollection<Document> col = DatabaseManager.mongoCollection("backups");
            if (col == null) return;
            String json = JsonUtil.toJson(JsonUtil.toMap(registro));
            Document d = new Document()
                    .append("tabla_modificada", tabla)
                    .append("registro_id", id)
                    .append("usuario", Session.getUsuarioActual() != null ? Session.getUsuarioActual().getUsername() : "anonimo")
                    .append("fecha_modificacion", LocalDateTime.now().toString())
                    .append("origen", origen)
                    .append("datos_originales", Document.parse(json));
            col.insertOne(d);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Backup Mongo falló: " + e.getMessage());
        }
    }

    private static boolean ejecutarDelete(String tabla, String id, boolean esMysql) {
        boolean disponible = esMysql ? DatabaseManager.isMysqlAvailable() : DatabaseManager.isPostgresAvailable();
        if (!disponible) return false;
        try (Connection c = esMysql ? DatabaseManager.mysqlConnection() : DatabaseManager.postgresConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM entidades WHERE tabla=? AND id=?")) {
            ps.setString(1, tabla); ps.setString(2, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, (esMysql ? "MySQL" : "PostgreSQL") + " no pudo eliminar " + tabla + "/" + id + ": " + e.getMessage());
            return false;
        }
    }

    private static boolean eliminarDeMongoVivo(String tabla, String id) {
        if (!DatabaseManager.isMongoAvailable()) return false;
        try {
            MongoCollection<Document> col = DatabaseManager.mongoCollection(tabla);
            if (col == null) return false;
            col.deleteMany(Filters.eq("_entityId", id));
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "MongoDB no pudo eliminar " + tabla + "/" + id + ": " + e.getMessage());
            return false;
        }
    }

    // ===================== CONSULTAS PARA UI =====================

    /** Cuenta cuántos registros vivos tiene una tabla en cada motor. */
    public static long contarRegistros(String tabla, Motor motor) {
        return switch (motor) {
            case MYSQL ->      contarSql(tabla, true);
            case POSTGRESQL -> contarSql(tabla, false);
            case MONGODB ->    contarMongo(tabla);
        };
    }

    private static long contarSql(String tabla, boolean esMysql) {
        boolean disponible = esMysql ? DatabaseManager.isMysqlAvailable() : DatabaseManager.isPostgresAvailable();
        if (!disponible) return -1;
        try (Connection c = esMysql ? DatabaseManager.mysqlConnection() : DatabaseManager.postgresConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM entidades WHERE tabla=?")) {
            ps.setString(1, tabla);
            try (var rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0; }
        } catch (Exception e) { return -1; }
    }

    private static long contarMongo(String tabla) {
        if (!DatabaseManager.isMongoAvailable()) return -1;
        try {
            MongoCollection<Document> col = DatabaseManager.mongoCollection(tabla);
            return col == null ? -1 : col.countDocuments();
        } catch (Exception e) { return -1; }
    }
}
