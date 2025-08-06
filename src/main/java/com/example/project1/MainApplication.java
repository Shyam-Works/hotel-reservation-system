package com.example.project1;

import com.example.project1.dao.BookingDao;
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
        // Step 1: Initialize the database tables at the start of the application.
        // This ensures the database exists and has the correct schema
        // before any booking actions are attempted.
        new BookingDao().createBookingsTable();

        // Step 2: Load the initial UI (e.g., KioskWelcome.fxml)
        URL fxmlLocation = getClass().getResource("/com/example/project1/KioskWelcome.fxml");
        if (fxmlLocation == null) {
            System.err.println("KioskWelcome.fxml not found! Check the path: /com/example/project1/KioskWelcome.fxml");
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Hotel ABC Kiosk");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}