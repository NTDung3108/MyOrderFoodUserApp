package com.dungnguyen.user.Model;

import java.util.List;


public class Request {
    private String id;
    private String userId;
    private String phone;
    private String name;
    private String address;
    private double total;
    private int status;
    private String restaurant;
    private long timeStamp;
    private List<Order> foods;
    private String reason;

    public Request() {
    }

    public Request(String id, String userId, String phone, String name, String address, double total,
                   int status, String restaurant, long timeStamp, List<Order> foods, String reason) {
        this.id = id;
        this.userId = userId;
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.status = status;
        this.restaurant = restaurant;
        this.timeStamp = timeStamp;
        this.foods = foods;
        this.reason = reason;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
