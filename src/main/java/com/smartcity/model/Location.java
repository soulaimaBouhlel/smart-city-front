// src/main/java/com/smartcity/model/Location.java
package com.smartcity.model;

public class Location {
    private Double latitude;
    private Double longitude;
    private String address;
    
    // Constructors
    public Location() {}
    
    public Location(Double latitude, Double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
    
    // Getters and Setters
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                '}';
    }
}