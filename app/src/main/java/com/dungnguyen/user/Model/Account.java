package com.dungnguyen.user.Model;

public class Account {
    private String phone;
    private String password;
    private String role;
    private String IsLockUp;

    public Account() {
    }

    public Account(String phone, String password, String role, String isLockUp) {
        this.phone = phone;
        this.password = password;
        this.role = role;
        IsLockUp = isLockUp;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIsLockUp() {
        return IsLockUp;
    }

    public void setIsLockUp(String isLockUp) {
        IsLockUp = isLockUp;
    }
}
