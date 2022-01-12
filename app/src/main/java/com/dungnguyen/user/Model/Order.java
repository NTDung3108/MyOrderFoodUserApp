package com.dungnguyen.user.Model;



public class Order {
    private String ProductId;
    private String ProductName;
    private int Quantity;
    private double Price;
    private double Discount;

    public Order() {
    }

    public Order(String productId, String productName, int quantity, double price, double discount) {
        ProductId = productId;
        ProductName = productName;
        Quantity = quantity;
        Price = price;
        Discount = discount;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public double getDiscount() {
        return Discount;
    }

    public void setDiscount(double discount) {
        Discount = discount;
    }
}
