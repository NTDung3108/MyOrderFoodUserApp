package com.dungnguyen.user.Model;


public class Rating {
    private String sender;
    private String foodId;
    private double rateValue;
    private String comment;
    private String image;

    public Rating() {
    }

    public Rating(String sender, String foodId, double rateValue, String comment, String image) {
        this.sender = sender;
        this.foodId = foodId;
        this.rateValue = rateValue;
        this.comment = comment;
        this.image = image;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public double getRateValue() {
        return rateValue;
    }

    public void setRateValue(double rateValue) {
        this.rateValue = rateValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
