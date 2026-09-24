package com.restaurant.dao;

import com.restaurant.model.Reservation;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.SQLException;

public class ReservationDAO {

    public boolean createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (user_id, table_number, reservation_date, reservation_time, party_size, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservation.getUserId());
            stmt.setInt(2, reservation.getTableNumber());
            stmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
            stmt.setTime(4, Time.valueOf(reservation.getReservationTime()));
            stmt.setInt(5, reservation.getPartySize());
            stmt.setString(6, reservation.getStatus());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}