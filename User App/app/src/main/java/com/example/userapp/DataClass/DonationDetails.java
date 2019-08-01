package com.example.userapp.DataClass;

import java.util.Objects;

public class DonationDetails {

    String ngoEmail,ngoName;
    String userEmail,userName;
    String amount;
    String dateTime;

    public DonationDetails() { }

    public DonationDetails(String ngoEmail, String ngoName, String userEmail, String userName, String amount, String dateTime) {
        this.ngoEmail = ngoEmail;
        this.ngoName = ngoName;
        this.userEmail = userEmail;
        this.userName = userName;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DonationDetails that = (DonationDetails) o;
        return Objects.equals(ngoEmail, that.ngoEmail) &&
                Objects.equals(ngoName, that.ngoName) &&
                Objects.equals(userEmail, that.userEmail) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ngoEmail, ngoName, userEmail, userName, amount, dateTime);
    }

    public String getNgoEmail() {
        return ngoEmail;
    }

    public void setNgoEmail(String ngoEmail) {
        this.ngoEmail = ngoEmail;
    }

    public String getNgoName() {
        return ngoName;
    }

    public void setNgoName(String ngoName) {
        this.ngoName = ngoName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
