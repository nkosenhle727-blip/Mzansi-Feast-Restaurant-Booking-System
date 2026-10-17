package com.restaurant.dao;

import com.restaurant.model.MenuItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDAO {

    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM menu_items";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new MenuItem(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("description")
                ));
            }

            // Auto-seed if database table is currently empty
            if (list.isEmpty()) {
                seedDefaultMenuItems(conn);
                return getAllMenuItems(); // Reload after seeding
            }

        } catch (SQLException e) {
            System.err.println("MenuItemDAO Error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private void seedDefaultMenuItems(Connection conn) {
        String insertSql = "INSERT INTO menu_items (name, category, price, description) VALUES " +
                "('Mogodu & Dombolo', 'Mains', 110.00, 'Traditional tripe served with fresh steam bread'), " +
                "('Chakalaka & Braai Platter', 'Mains', 185.00, 'Boerewors, lamb chops, chicken wings with spicy chakalaka'), " +
                "('Kota Supreme', 'Light Meals', 65.00, 'Quarter loaf stuffed with chips, polony, cheese, and egg'), " +
                "('Malva Pudding', 'Desserts', 45.00, 'Warm South African sponge cake with warm custard'), " +
                "('Rooibos Iced Tea', 'Beverages', 25.00, 'Freshly brewed home-style iced tea')";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(insertSql);
            System.out.println("Default menu items seeded into database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}