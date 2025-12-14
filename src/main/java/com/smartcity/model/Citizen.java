package com.smartcity.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "citizens")
public class Citizen extends User {
    private Integer rewardPoints;

    public Citizen() {
        super();
        this.role = UserRole.CITIZEN;
        this.rewardPoints = 0;
    }

    public Citizen(String name, String email, String phone, String password, String photoUrl) {
        super(name, email, phone, password, photoUrl, UserRole.CITIZEN);
        this.rewardPoints = 0;
    }

    // Alias methods for backward compatibility
    public String getCitizenId() {
        return getUserId();
    }
    
    public void setCitizenId(String citizenId) {
        setUserId(citizenId);
    }

    public Integer getRewardPoints() { 
        return rewardPoints; 
    }
    
    public void setRewardPoints(Integer rewardPoints) { 
        this.rewardPoints = rewardPoints; 
    }

    public void addRewardPoints(Integer points) {
        this.rewardPoints += points;
    }

    @Override
    public String toString() {
        return "Citizen{" +
                "citizenId='" + getCitizenId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", rewardPoints=" + rewardPoints +
                '}';
    }
}