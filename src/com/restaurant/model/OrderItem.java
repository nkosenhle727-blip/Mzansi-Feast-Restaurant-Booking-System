package com.restaurant.model;

public class OrderItem {
    private MenuItem menuItem;
    private int quantity;

    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public String getItemName() { return menuItem.getName(); }
    public double getUnitPrice() { return menuItem.getPrice(); }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Algorithm 4.5.4: Item Total Calculation
    public double getSubtotal() {
        return menuItem.getPrice() * quantity;
    }
}