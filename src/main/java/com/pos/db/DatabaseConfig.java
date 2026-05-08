package com.pos.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static DatabaseConfig instance;
    private Connection connection;
    private static final String DATABASE_URL = "jdbc:sqlite:shop.db";

    private DatabaseConfig() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DATABASE_URL);
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }

    public static DatabaseConfig getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeDatabase() throws SQLException {
        String createProductsTableSQL = "CREATE TABLE IF NOT EXISTS products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "barcode TEXT UNIQUE NOT NULL," +
                "name TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "stock INTEGER NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String createTransactionsTableSQL = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "timestamp TIMESTAMP NOT NULL," +
                "items_json TEXT NOT NULL," +
                "subtotal REAL NOT NULL," +
                "discount REAL NOT NULL," +
                "total REAL NOT NULL," +
                "payment_method TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String createMmebersTableSQL = "CREATE TABLE IF NOT EXISTS members (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "phone TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String createAdminsTableSQL = "CREATE TABLE IF NOT EXISTS admins (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String createActionLogsTableSQL = "CREATE TABLE IF NOT EXISTS action_logs (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER," +
            "user_role TEXT NOT NULL," +
            "action TEXT NOT NULL," +
            "action_details TEXT," +
            "products_json TEXT," +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createProductsTableSQL);
            stmt.execute(createTransactionsTableSQL);
            stmt.execute(createMmebersTableSQL);
            stmt.execute(createActionLogsTableSQL);
            stmt.execute(createAdminsTableSQL);

            String seedAdminSQL = "INSERT OR IGNORE INTO admins(username, password) VALUES('admin', '1234')"; 
            stmt.execute(seedAdminSQL);
        }
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
