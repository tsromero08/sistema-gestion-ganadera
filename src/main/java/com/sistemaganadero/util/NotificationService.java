package com.sistemaganadero.util;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class NotificationService {

    public enum Type { SUCCESS, ERROR, WARNING, INFO }

    private static StackPane overlayPane;

    public static void setOverlayPane(StackPane pane) { overlayPane = pane; }

    public static void success(String message) { show(Type.SUCCESS, message); }
    public static void error(String message) { show(Type.ERROR, message); }
    public static void warning(String message) { show(Type.WARNING, message); }
    public static void info(String message) { show(Type.INFO, message); }

    private static void show(Type type, String message) {
        if (overlayPane == null) {
            // Fallback to classic alert if overlay not set
            javafx.scene.control.Alert.AlertType alertType = switch (type) {
                case SUCCESS, INFO -> javafx.scene.control.Alert.AlertType.INFORMATION;
                case ERROR -> javafx.scene.control.Alert.AlertType.ERROR;
                case WARNING -> javafx.scene.control.Alert.AlertType.WARNING;
            };
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(alertType);
            a.setHeaderText(null);
            a.setContentText(message);
            a.showAndWait();
            return;
        }
        Platform.runLater(() -> {
            HBox toast = buildToast(type, message);
            toast.setOpacity(0);
            StackPane.setAlignment(toast, Pos.TOP_RIGHT);
            StackPane.setMargin(toast, new Insets(16, 16, 0, 0));
            overlayPane.getChildren().add(toast);

            Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(toast.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(250), new KeyValue(toast.opacityProperty(), 1))
            );
            Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(toast.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(350), new KeyValue(toast.opacityProperty(), 0))
            );
            fadeOut.setOnFinished(e -> overlayPane.getChildren().remove(toast));
            fadeIn.setOnFinished(e -> {
                Timeline hold = new Timeline(new KeyFrame(Duration.seconds(3.5)));
                hold.setOnFinished(ev -> fadeOut.play());
                hold.play();
            });
            fadeIn.play();
        });
    }

    private static HBox buildToast(Type type, String message) {
        record ToastStyle(String bg, String text, String border, String icon) {}
        ToastStyle s = switch (type) {
            case SUCCESS -> new ToastStyle("#d1fae5", "#065f46", "#6ee7b7", "✓");
            case ERROR   -> new ToastStyle("#fee2e2", "#991b1b", "#fca5a5", "✕");
            case WARNING -> new ToastStyle("#fef3c7", "#92400e", "#fcd34d", "⚠");
            case INFO    -> new ToastStyle("#dbeafe", "#1e40af", "#93c5fd", "ℹ");
        };

        Label iconLabel = new Label(s.icon());
        iconLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + s.text() + "; -fx-min-width: 20;");

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + s.text() + "; -fx-wrap-text: true;");
        msgLabel.setMaxWidth(260);

        HBox toast = new HBox(10, iconLabel, msgLabel);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(13, 18, 13, 14));
        toast.setMaxWidth(340);
        toast.setStyle(
            "-fx-background-color: " + s.bg() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + s.border() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 12, 0, 0, 4);"
        );
        return toast;
    }
}
