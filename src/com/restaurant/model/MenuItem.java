package com.restaurant.model;

public class MenuItem {
    private int itemId;
    private String name;
    private String category;
    private double price;
    private String description;

    public MenuItem(int itemId, String name, String category, double price, String description) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
    }

    public int getItemId() { return itemId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return name + " - R" + String.format("%.2f", price);
    }
}