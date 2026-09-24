package com.restaurant.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {
    private int reservationId;
    private int userId;
    private int tableNumber;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private int partySize;
    private String status;

    public Reservation(int reservationId, int userId, int tableNumber, LocalDate reservationDate, LocalTime reservationTime, int partySize, String status) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.tableNumber = tableNumber;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.partySize = partySize;
        this.status = status;
    }

    public int getReservationId() { return reservationId; }
    public int getUserId() { return userId; }
    public int getTableNumber() { return tableNumber; }
    public LocalDate getReservationDate() { return reservationDate; }
    public LocalTime getReservationTime() { return reservationTime; }
    public int getPartySize() { return partySize; }
    public String getStatus() { return status; }
}