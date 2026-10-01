package com.sistemaganadero.app;

import com.sistemaganadero.database.DatabaseInitializer;
import com.sistemaganadero.service.GanaderiaDataStore;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Inicializar datos en memoria y BD cloud si está configurada
        GanaderiaDataStore.initializeData();
        DatabaseInitializer.initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        stage.setTitle("Sistema Ganadero");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(700);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
