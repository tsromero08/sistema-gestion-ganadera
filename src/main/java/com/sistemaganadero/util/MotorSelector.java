package com.sistemaganadero.util;

import com.sistemaganadero.database.DatabaseManager;
import com.sistemaganadero.service.PersistenceService.Motor;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Helper UI para que el usuario elija en qué motor de BD se guarda un registro.
 * Muestra estado de disponibilidad en tiempo real.
 *
 * Uso típico en un formulario:
 *   MotorSelector sel = new MotorSelector();
 *   grid.add(sel.getNode(), 1, fila);
 *   ...
 *   Motor motor = sel.getMotorSeleccionado();   // dentro del resultConverter
 */
public class MotorSelector {

    private final ComboBox<Motor> combo;
    private final Label estado;
    private final VBox raiz;

    public MotorSelector() {
        this(Motor.MONGODB);  // por defecto Mongo (siempre disponible vía fallback)
    }

    public MotorSelector(Motor preferido) {
        combo = new ComboBox<>(FXCollections.observableArrayList(Motor.values()));
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Motor m) {
                if (m == null) return "";
                String chk = disponible(m) ? "✓" : "—";
                return chk + "  " + m.getEtiqueta();
            }
            @Override public Motor fromString(String s) { return null; }
        });
        combo.setValue(preferido != null ? preferido : Motor.MONGODB);
        combo.setPrefWidth(220);
        combo.setStyle("-fx-background-color: white; -fx-border-color: #1f7a63; -fx-border-radius: 6; "
                + "-fx-background-radius: 6; -fx-font-size: 13px; -fx-font-weight: bold;");

        estado = new Label();
        estado.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        combo.valueProperty().addListener((o, a, b) -> actualizarEstado());
        actualizarEstado();

        Label titulo = new Label("💾  Motor de base de datos");
        titulo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1f7a63;");

        Label nota = new Label("ⓘ Se guardará una copia automática en MongoDB.");
        nota.setStyle("-fx-font-size: 10px; -fx-text-fill: #92400e; -fx-font-style: italic;");

        raiz = new VBox(4);
        raiz.setPadding(new Insets(10, 12, 10, 12));
        raiz.setStyle("-fx-background-color: linear-gradient(to right, #e8f5f1, #f0f9f6); "
                + "-fx-background-radius: 8; -fx-border-color: #1f7a63; -fx-border-radius: 8; -fx-border-width: 1;");

        HBox fila = new HBox(10, combo, estado);
        fila.setAlignment(Pos.CENTER_LEFT);
        raiz.getChildren().addAll(titulo, fila, nota);
    }

    /** Nodo a insertar en el formulario. */
    public VBox getNode() { return raiz; }

    public Motor getMotorSeleccionado() { return combo.getValue(); }
    public void setMotorSeleccionado(Motor m) { combo.setValue(m); }

    private void actualizarEstado() {
        Motor m = combo.getValue();
        if (m == null) { estado.setText(""); return; }
        boolean disp = disponible(m);
        if (disp) {
            estado.setText("· Motor activo");
            estado.setStyle("-fx-font-size: 11px; -fx-text-fill: #065f46; -fx-font-weight: bold;");
        } else {
            estado.setText("· No conectado — usará archivo local");
            estado.setStyle("-fx-font-size: 11px; -fx-text-fill: #92400e; -fx-font-style: italic;");
        }
    }

    private static boolean disponible(Motor m) {
        return switch (m) {
            case MYSQL ->      DatabaseManager.isMysqlAvailable();
            case POSTGRESQL -> DatabaseManager.isPostgresAvailable();
            case MONGODB ->    DatabaseManager.isMongoAvailable();
        };
    }
}
