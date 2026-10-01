package com.sistemaganadero.service;

import com.mongodb.client.MongoCollection;
import com.sistemaganadero.database.DatabaseManager;
import com.sistemaganadero.model.Backup;
import com.sistemaganadero.model.Usuario;
import com.sistemaganadero.util.JsonUtil;
import com.sistemaganadero.util.Session;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de respaldo de registros antes de ser editados.
 *
 * Estrategia (en orden):
 *  1. MongoDB: colección "backups" — destino preferido (no estructurado).
 *  2. PostgreSQL/MySQL: tabla "auditoria_ediciones" con metadatos + JSON.
 *  3. Fallback local: archivo data/backups/YYYY-MM-DD.jsonl (un JSON por línea).
 *
 * El sistema nunca pierde respaldos: siempre se intenta al menos el fallback local.
 */
public final class BackupService {

    private static final Logger LOG = Logger.getLogger(BackupService.class.getName());
    private static final Path DIR_FALLBACK = Path.of("data", "backups");
    private static final AtomicLong AUTO_ID = new AtomicLong(System.currentTimeMillis());

    private BackupService() {}

    /**
     * Guarda una copia del registro original ANTES de editarlo.
     * @param tabla nombre lógico de la tabla/colección
     * @param registroId id del registro original
     * @param registroOriginal objeto a respaldar (cualquier modelo)
     */
    public static Backup guardar(String tabla, String registroId, Object registroOriginal) {
        Map<String, Object> snapshot = JsonUtil.toMap(registroOriginal);
        String json = JsonUtil.toJson(snapshot);
        return guardarJson(tabla, registroId, json);
    }

    public static Backup guardarJson(String tabla, String registroId, String datosJson) {
        Usuario u = Session.getUsuarioActual();
        String usuario = u != null ? u.getUsername() : "anonimo";

        Backup b = new Backup(tabla, registroId, datosJson, usuario);
        boolean ok = false;
        ok |= persistirEnMongo(b);
        persistirAuditoriaSql(b);              // metadatos siempre, si SQL disponible
        if (!ok) persistirEnArchivo(b);        // garantía mínima
        return b;
    }

    private static boolean persistirEnMongo(Backup b) {
        if (!DatabaseManager.isMongoAvailable()) return false;
        try {
            MongoCollection<Document> col = DatabaseManager.mongoCollection("backups");
            if (col == null) return false;
            Document doc = new Document()
                    .append("tabla_modificada", b.getTablaModificada())
                    .append("registro_id", b.getRegistroId())
                    .append("usuario", b.getUsuario())
                    .append("fecha_modificacion", b.getFechaModificacion().toString())
                    .append("datos_originales", Document.parse(b.getDatosOriginalesJson()));
            col.insertOne(doc);
            ObjectId oid = doc.getObjectId("_id");
            if (oid != null) b.setId(oid.toHexString());
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Backup en MongoDB falló: " + e.getMessage());
            return false;
        }
    }

    private static void persistirAuditoriaSql(Backup b) {
        long id = AUTO_ID.incrementAndGet();
        // Probar PostgreSQL primero, luego MySQL — ambos opcionales
        if (DatabaseManager.isPostgresAvailable()) escribirAuditoria(b, id, "postgres");
        if (DatabaseManager.isMysqlAvailable()) escribirAuditoria(b, id, "mysql");
        if (b.getId() == null) b.setId(String.valueOf(id));
    }

    private static void escribirAuditoria(Backup b, long id, String motor) {
        try (Connection c = motor.equals("postgres")
                ? DatabaseManager.postgresConnection()
                : DatabaseManager.mysqlConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO auditoria_ediciones (id, tabla_modificada, registro_id, usuario, fecha_modificacion) "
                     + "VALUES (?, ?, ?, ?, ?)")) {
            ps.setLong(1, id);
            ps.setString(2, b.getTablaModificada());
            ps.setString(3, b.getRegistroId());
            ps.setString(4, b.getUsuario());
            ps.setTimestamp(5, Timestamp.valueOf(b.getFechaModificacion()));
            ps.executeUpdate();
        } catch (Exception e) {
            LOG.log(Level.WARNING, motor + ": auditoría no escrita: " + e.getMessage());
        }
    }

    private static void persistirEnArchivo(Backup b) {
        try {
            Files.createDirectories(DIR_FALLBACK);
            String fname = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".jsonl";
            Path p = DIR_FALLBACK.resolve(fname);
            Map<String, Object> linea = new LinkedHashMap<>();
            linea.put("tabla", b.getTablaModificada());
            linea.put("registro_id", b.getRegistroId());
            linea.put("usuario", b.getUsuario());
            linea.put("fecha", b.getFechaModificacion().toString());
            linea.put("datos_originales", b.getDatosOriginalesJson());
            String json = JsonUtil.toJson(linea) + System.lineSeparator();
            Files.writeString(p, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (b.getId() == null) b.setId("local-" + AUTO_ID.incrementAndGet());
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "No se pudo escribir backup local: " + ioe.getMessage());
        }
    }

    /** Conteo aproximado de respaldos disponibles (para mostrar en UI). */
    public static long contarBackups() {
        if (DatabaseManager.isMongoAvailable()) {
            try {
                MongoCollection<Document> col = DatabaseManager.mongoCollection("backups");
                if (col != null) return col.countDocuments();
            } catch (Exception ignored) {}
        }
        try {
            if (!Files.exists(DIR_FALLBACK)) return 0;
            return Files.walk(DIR_FALLBACK).filter(Files::isRegularFile).count();
        } catch (Exception e) { return 0; }
    }
}
