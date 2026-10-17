package com.restaurant.dao;

import com.restaurant.model.Bill;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public boolean createBill(int orderId, int userId, double totalAmount, LocalDate date) {
        String sql = "INSERT INTO bills (order_id, user_id, total_amount, bill_date, payment_status) VALUES (?, ?, ?, ?, 'PAID')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            stmt.setInt(2, userId);
            stmt.setDouble(3, totalAmount);
            stmt.setDate(4, Date.valueOf(date));

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Algorithm 4.5.5: Daily Sales Aggregation
    public double getDailySalesTotal(LocalDate date) {
        String sql = "SELECT SUM(total_amount) FROM bills WHERE bill_date = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public List<Bill> getBillsByDate(LocalDate date) {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE bill_date = ? ORDER BY bill_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Bill(
                        rs.getInt("bill_id"),
                        rs.getInt("order_id"),
                        rs.getInt("user_id"),
                        rs.getDouble("total_amount"),
                        rs.getDate("bill_date").toLocalDate(),
                        rs.getString("payment_status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}