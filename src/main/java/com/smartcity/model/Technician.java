package com.smartcity.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;
import java.util.HashSet;

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
    
    // NEW FIELDS FOR SMART SCHEDULING
    private Set<Skill> skills = new HashSet<>();
    private Location location;
    private Integer currentWorkload = 0;
    
    // Constructors
    public Technician() {
        this.available = true;
        this.rating = 0.0;
        this.currentWorkload = 0;
    }
    
    public Technician(String name, String email, String phone, String password, String photoUrl) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.photoUrl = photoUrl;
        this.available = true;
        this.rating = 0.0;
        this.currentWorkload = 0;
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
    
    // NEW GETTERS AND SETTERS FOR SMART SCHEDULING
    public Set<Skill> getSkills() { return skills; }
    public void setSkills(Set<Skill> skills) { this.skills = skills; }
    
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    
    public Integer getCurrentWorkload() { return currentWorkload; }
    public void setCurrentWorkload(Integer currentWorkload) { 
        this.currentWorkload = currentWorkload; 
    }
    
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
    
    // NEW BUSINESS METHODS FOR SMART SCHEDULING
    public void increaseWorkload() { 
        if (this.currentWorkload == null) this.currentWorkload = 0;
        this.currentWorkload++; 
    }
    
    public void decreaseWorkload() { 
        if (this.currentWorkload != null && this.currentWorkload > 0) {
            this.currentWorkload--; 
        }
    }
    
    public boolean hasSkill(Skill skill) {
        return this.skills != null && this.skills.contains(skill);
    }
    
    public double calculateSkillMatch(Set<Skill> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) return 1.0;
        if (this.skills == null || this.skills.isEmpty()) return 0.0;
        
        long matches = requiredSkills.stream()
                .filter(this.skills::contains)
                .count();
        
        return (double) matches / requiredSkills.size();
    }
    
    @Override
    public String toString() {
        return "Technician{" +
                "technicianId='" + technicianId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", available=" + available +
                ", skills=" + skills +
                ", workload=" + currentWorkload +
                ", rating=" + rating +
                '}';
    }
}