package com.example.ngoapp.DataClass;

import java.util.Objects;

public class NgoDetails {

    String name="Dummy",address="Dummy",phone_number="Dummy",email="Dummy",latitude="Dummy",longitude="Dummy";

    public NgoDetails() { }

    public NgoDetails(String name, String address, String phone_number, String email, String latitude, String longitude) {
        this.name = name;
        this.address = address;
        this.phone_number = phone_number;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NgoDetails that = (NgoDetails) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(address, that.address) &&
                Objects.equals(phone_number, that.phone_number) &&
                Objects.equals(email, that.email) &&
                Objects.equals(latitude, that.latitude) &&
                Objects.equals(longitude, that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address, phone_number, email, latitude, longitude);
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

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}

