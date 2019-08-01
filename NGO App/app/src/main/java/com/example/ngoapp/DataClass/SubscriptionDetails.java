package com.example.ngoapp.DataClass;

import java.util.Objects;

public class SubscriptionDetails {

    String ngoEmail,ngoName;
    String userEmail,userName;

    public SubscriptionDetails() { }

    public SubscriptionDetails(String ngoEmail, String ngoName, String userEmail, String userName) {
        this.ngoEmail = ngoEmail;
        this.ngoName = ngoName;
        this.userEmail = userEmail;
        this.userName = userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionDetails that = (SubscriptionDetails) o;
        return Objects.equals(ngoEmail, that.ngoEmail) &&
                Objects.equals(ngoName, that.ngoName) &&
                Objects.equals(userEmail, that.userEmail) &&
                Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ngoEmail, ngoName, userEmail, userName);
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
}
