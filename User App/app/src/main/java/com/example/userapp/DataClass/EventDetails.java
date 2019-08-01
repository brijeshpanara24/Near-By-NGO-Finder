package com.example.userapp.DataClass;

import java.util.Objects;

public class EventDetails {
    String name,organisedBy,description,location,start_date,start_time,end_date,end_time;
    String category = "not defined";
    public EventDetails() { }

    public EventDetails(String name, String category, String organisedBy, String description, String location, String start_date, String start_time, String end_date, String end_time) {
        this.name = name;
        this.category = category;
        this.organisedBy = organisedBy;
        this.description = description;
        this.location = location;
        this.start_date = start_date;
        this.start_time = start_time;
        this.end_date = end_date;
        this.end_time = end_time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventDetails that = (EventDetails) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(organisedBy, that.organisedBy) &&
                Objects.equals(description, that.description) &&
                Objects.equals(location, that.location) &&
                Objects.equals(start_date, that.start_date) &&
                Objects.equals(start_time, that.start_time) &&
                Objects.equals(end_date, that.end_date) &&
                Objects.equals(end_time, that.end_time) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, organisedBy, description, location, start_date, start_time, end_date, end_time, category);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganisedBy() {
        return organisedBy;
    }

    public void setOrganisedBy(String organisedBy) {
        this.organisedBy = organisedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

}

