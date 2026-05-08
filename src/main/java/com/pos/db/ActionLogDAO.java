package com.pos.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.pos.session.POSSession;

public class ActionLogDAO {
    private final Connection connection;

    public ActionLogDAO() throws SQLException {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    public void logAction(String action, String actionDetails, String productsJson) {
        Integer userId = POSSession.getCurrentUserId();
        String role = POSSession.getCurrentRole();

        String sql = "INSERT INTO action_logs (user_id, user_role, action, action_details, products_json, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (userId == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, userId);
            }
            stmt.setString(2, role);
            stmt.setString(3, action);
            stmt.setString(4, actionDetails);
            stmt.setString(5, productsJson);
            stmt.setString(6, LocalDateTime.now().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // or log properly
        }
    }

    // Convenience method for simple actions (no products)
    public void logAction(String action, String actionDetails) {
        logAction(action, actionDetails, null);
    }
}