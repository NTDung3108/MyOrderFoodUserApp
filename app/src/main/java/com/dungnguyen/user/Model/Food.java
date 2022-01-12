package com.dungnguyen.user.Model;


import java.io.Serializable;

public class Food implements Serializable {
    private String id;
    private String name;
    private String image;
    private double price;
    private double discount;
    private String menuId;
    private String restaurants;
    private String description;
    private double ratting;

    public Food() {
    }

    public Food(String id, String name, String image, double price, double discount, String menuId,
                String restaurants, String description, double ratting) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.price = price;
        this.discount = discount;
        this.menuId = menuId;
        this.restaurants = restaurants;
        this.description = description;
        this.ratting = ratting;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(String restaurants) {
        this.restaurants = restaurants;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRatting() {
        return ratting;
    }

    public void setRatting(double ratting) {
        this.ratting = ratting;
    }
}
