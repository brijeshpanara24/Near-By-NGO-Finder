package com.example.userapp.DataClass;

import java.util.Objects;

public class UserDetails {

    String name,address,phone_number,email;

    public UserDetails() { }

    public UserDetails(String name, String address, String phone_number, String email) {
        this.name = name;
        this.address = address;
        this.phone_number = phone_number;
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetails that = (UserDetails) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(address, that.address) &&
                Objects.equals(phone_number, that.phone_number) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address, phone_number, email);
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

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
