package com.example.project1; // Updated package name
import com.example.project1.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load KioskWelcome.fxml from the correct resource path
        DatabaseUtil.initializeDatabase();
        URL fxmlLocation = getClass().getResource("/com/example/project1/KioskWelcome.fxml");
        if (fxmlLocation == null) {
            System.err.println("KioskWelcome.fxml not found! Check the path: /com/example/project1/KioskWelcome.fxml");
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 800, 600); // Set initial scene size
        stage.setTitle("Hotel ABC Kiosk");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}