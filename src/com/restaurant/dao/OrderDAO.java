package com.restaurant.dao;

import com.restaurant.model.OrderItem;
import java.sql.*;
import java.util.List;

public class OrderDAO {

    public int createOrderWithItems(int userId, List<OrderItem> items, double totalAmount) {
        String orderSql = "INSERT INTO orders (user_id, total_amount, order_status) VALUES (?, ?, 'COMPLETED')";
        String itemSql = "INSERT INTO order_items (order_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement stmtOrder = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                stmtOrder.setInt(1, userId);
                stmtOrder.setDouble(2, totalAmount);
                stmtOrder.executeUpdate();

                ResultSet rs = stmtOrder.getGeneratedKeys();
                int orderId = 0;
                if (rs.next()) {
                    orderId = rs.getInt(1);
                }

                if (orderId > 0) {
                    try (PreparedStatement stmtItem = conn.prepareStatement(itemSql)) {
                        for (OrderItem item : items) {
                            stmtItem.setInt(1, orderId);
                            stmtItem.setInt(2, item.getMenuItem().getItemId());
                            stmtItem.setInt(3, item.getQuantity());
                            stmtItem.setDouble(4, item.getUnitPrice());
                            stmtItem.addBatch();
                        }
                        stmtItem.executeBatch();
                    }
                    conn.commit(); // Commit transaction
                    return orderId;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
