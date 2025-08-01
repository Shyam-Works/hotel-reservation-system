package com.example.project1.config;

public class DatabaseConfig {
    /**
     * The URL for your SQLite database file.
     * "jdbc:sqlite:hotel_bookings.db" means it will create or connect
     * to a file named 'hotel_bookings.db' in your project's root directory
     * (or the directory from which your application is executed).
     */
    public static final String DB_URL = "jdbc:sqlite:hotel_bookings.db";
}