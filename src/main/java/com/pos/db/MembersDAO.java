package com.pos.db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.pos.model.Member;

public class MembersDAO {
    private final Connection connection;

    public MembersDAO() throws SQLException {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    public void addMember(String name, String email, String phone) throws SQLException {
        String sql = "INSERT INTO members(name, email, phone) VALUES (?, ?, ?)";
    
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.executeUpdate();
        }
    }

    public boolean checkEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM members WHERE email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public Member findMember(String input) throws SQLException {
        String sql = "SELECT * FROM members WHERE email = ? OR name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setString(1, input);
            pstmt.setString(2, input);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Member(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone")
                    );
                }
            }
        }
        return null; // Not found
    }
}


