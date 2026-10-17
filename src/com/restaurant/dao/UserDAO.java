package com.restaurant.dao;

import com.restaurant.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class UserDAO {

    public User authenticateUser(String email, String password) {
        // Query by email only to prevent SQL column name mismatch errors
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ResultSetMetaData md = rs.getMetaData();
                int columnCount = md.getColumnCount();

                String dbPassword = "";
                int userId = rs.getInt("user_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String userEmail = rs.getString("email");
                int roleId = 1;

                // Dynamically inspect column names present in your table
                for (int i = 1; i <= columnCount; i++) {
                    String colName = md.getColumnName(i).toLowerCase();
                    if (colName.equals("password_hash") || colName.equals("password")) {
                        dbPassword = rs.getString(i);
                    }
                    if (colName.equals("role_id")) {
                        roleId = rs.getInt(i);
                    }
                }

                // Verify password match in Java
                if (password.equals(dbPassword)) {
                    return new User(userId, firstName, lastName, userEmail, roleId);
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDAO Authentication Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}