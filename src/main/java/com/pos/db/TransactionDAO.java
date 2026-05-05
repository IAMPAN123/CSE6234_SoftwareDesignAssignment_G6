package com.pos.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.pos.model.Transaction;

public class TransactionDAO {
    private final Connection connection;

    public TransactionDAO() throws SQLException {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    public void addTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (timestamp, items_json, subtotal, discount, total, payment_method) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, transaction.getTimestamp().toString());
            stmt.setString(2, transaction.getItemsJson());
            stmt.setDouble(3, transaction.getSubtotal());
            stmt.setDouble(4, transaction.getDiscount());
            stmt.setDouble(5, transaction.getTotal());
            stmt.setString(6, transaction.getPaymentMethod());
            stmt.executeUpdate();
        }
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY id DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getInt("id"),
                    LocalDateTime.parse(rs.getString("timestamp")),
                    rs.getString("items_json"),
                    rs.getDouble("subtotal"),
                    rs.getDouble("discount"),
                    rs.getDouble("total"),
                    rs.getString("payment_method")
                ));
            }
        }
        return transactions;
    }
}
