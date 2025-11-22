package com.smartcity.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "citizens")
public class Citizen {
    @Id
    private String citizenId;
    private String name;
    private String email;
    private String phone;
    private String password; // encrypted - removed mofPasse
    private Integer rewardPoints;
    private String photoUrl;
    
    // Constructors
    public Citizen() {
        this.rewardPoints = 0;
    }
    
    public Citizen(String name, String email, String phone, String password, String photoUrl) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.photoUrl = photoUrl;
        this.rewardPoints = 0;
    }
    
    // Getters and Setters
    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Integer getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(Integer rewardPoints) { this.rewardPoints = rewardPoints; }
    
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    
    // Business methods
    public void addRewardPoints(Integer points) {
        this.rewardPoints += points;
    }
    
    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    
    @Override
    public String toString() {
        return "Citizen{" +
                "citizenId='" + citizenId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", rewardPoints=" + rewardPoints +
                '}';
    }
}