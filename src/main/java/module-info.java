module com.example.project1 { // Updated module name
    requires javafx.controls;
    requires javafx.fxml;
    // javafx.media is NO LONGER REQUIRED as MediaView was removed from FXML

    requires java.sql;     // Required for SQLite JDBC
    requires java.logging; // Required for Java Logging (as per project PDF)

    // Open packages for FXML reflection
    opens com.example.project1 to javafx.fxml; // For MainApplication if needed
    opens com.example.project1.controller to javafx.fxml; // For all your controllers

    // Export packages for broader access within the module system
    exports com.example.project1;
    exports com.example.project1.controller;
    exports com.example.project1.model; // Will be created later
    exports com.example.project1.dao;    // Will be created later
    exports com.example.project1.util;   // Will be created later
}