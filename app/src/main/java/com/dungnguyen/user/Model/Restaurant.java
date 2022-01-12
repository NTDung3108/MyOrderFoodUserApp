package com.dungnguyen.user.Model;

public class Restaurant {

    public String name;
    public String address;
    public String imageURL;
    public String phone;

    public Restaurant() {
    }

    public Restaurant(String name, String address, String imageURL, String phone) {
        this.name = name;
        this.address = address;
        this.imageURL = imageURL;
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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
