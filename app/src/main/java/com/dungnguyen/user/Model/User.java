package com.dungnguyen.user.Model;


public class User {
    private  String name;
    private  String birthday;
    private  String phone;
    private  String favoriteFood;
    private  String imageURL;
    private  String key;
    private  String iv;

    public User() {
    }

    public User(String name, String birthday, String phone, String favoriteFood, String imageURL, String key, String iv) {
        this.name = name;
        this.birthday = birthday;
        this.phone = phone;
        this.favoriteFood = favoriteFood;
        this.imageURL = imageURL;
        this.key = key;
        this.iv = iv;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFavoriteFood() {
        return favoriteFood;
    }

    public void setFavoriteFood(String favoriteFood) {
        this.favoriteFood = favoriteFood;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
}
