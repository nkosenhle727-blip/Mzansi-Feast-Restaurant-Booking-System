package com.restaurant.model;

import java.time.LocalDate;

public class Bill {
    private int billId;
    private int orderId;
    private int userId;
    private double totalAmount;
    private LocalDate billDate;
    private String paymentStatus;

    public Bill(int billId, int orderId, int userId, double totalAmount, LocalDate billDate, String paymentStatus) {
        this.billId = billId;
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.billDate = billDate;
        this.paymentStatus = paymentStatus;
    }

    public int getBillId() { return billId; }
    public int getOrderId() { return orderId; }
    public int getUserId() { return userId; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDate getBillDate() { return billDate; }
    public String getPaymentStatus() { return paymentStatus; }
}