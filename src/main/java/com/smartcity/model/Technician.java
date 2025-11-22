package com.smartcity.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "technicians")
public class Technician {
    @Id
    private String technicianId;
    private String name;
    private String email;
    private String phone;
    private String password; // encrypted - removed mofPasse
    private Boolean available;
    private Double rating;
    private String photoUrl;
    
    // Constructors
    public Technician() {
        this.available = true;
        this.rating = 0.0;
    }
    
    public Technician(String name, String email, String phone, String password, String photoUrl) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.photoUrl = photoUrl;
        this.available = true;
        this.rating = 0.0;
    }
    
    // Getters and Setters
    public String getTechnicianId() { return technicianId; }
    public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    
    // Business methods
    public void unavailable() {
        this.available = false;
    }
    
    public void available() {
        this.available = true;
    }
    
    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    
    public void updateRating(Double newRating) {
        this.rating = newRating;
    }
    
    @Override
    public String toString() {
        return "Technician{" +
                "technicianId='" + technicianId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", available=" + available +
                ", rating=" + rating +
                '}';
    }
}