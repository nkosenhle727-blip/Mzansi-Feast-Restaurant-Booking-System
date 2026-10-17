package com.restaurant.dao;

import com.restaurant.model.Reservation;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public boolean isTableReserved(int tableNumber, LocalDate date, LocalTime time) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE table_number = ? AND reservation_date = ? AND reservation_time = ? AND status != 'CANCELLED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tableNumber);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, Time.valueOf(time));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<LocalTime> getAlternativeSlots(int tableNumber, LocalDate date, LocalTime requestedTime) {
        List<LocalTime> operationalSlots = List.of(
                LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0),
                LocalTime.of(17, 0), LocalTime.of(18, 0), LocalTime.of(19, 0),
                LocalTime.of(20, 0), LocalTime.of(21, 0)
        );

        List<LocalTime> availableAlternatives = new ArrayList<>();

        for (LocalTime slot : operationalSlots) {
            if (slot.equals(requestedTime)) {
                continue;
            }

            if (!isTableReserved(tableNumber, date, slot)) {
                availableAlternatives.add(slot);
            }

            if (availableAlternatives.size() == 3) {
                break;
            }
        }

        return availableAlternatives;
    }

    public String createReservation(Reservation reservation) {
        if (isTableReserved(reservation.getTableNumber(), reservation.getReservationDate(), reservation.getReservationTime())) {
            return "SLOT_OCCUPIED";
        }

        String sql = "INSERT INTO reservations (user_id, table_number, reservation_date, reservation_time, party_size, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservation.getUserId());
            stmt.setInt(2, reservation.getTableNumber());
            stmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
            stmt.setTime(4, Time.valueOf(reservation.getReservationTime()));
            stmt.setInt(5, reservation.getPartySize());
            stmt.setString(6, reservation.getStatus());

            int rows = stmt.executeUpdate();
            return rows > 0 ? "SUCCESS" : "Failed to create reservation record.";

        } catch (SQLException e) {
            System.err.println("SQL Exception during reservation: " + e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * Updates status to CANCELLED for a specific reservation.
     */
    public boolean cancelReservation(int reservationId) {
        String sql = "UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Exception during cancellation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Reservation> getReservationsByUser(int userId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE user_id = ? AND status != 'CANCELLED' ORDER BY reservation_date DESC, reservation_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Reservation(
                        rs.getInt("reservation_id"),
                        rs.getInt("user_id"),
                        rs.getInt("table_number"),
                        rs.getDate("reservation_date").toLocalDate(),
                        rs.getTime("reservation_time").toLocalTime(),
                        rs.getInt("party_size"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}