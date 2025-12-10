package com.smartcity.model;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;
import java.util.HashSet;

@Document(collection = "technicians")
public class Technician extends User {
    private Boolean available;
    private Double rating;
    private Set<Skill> skills = new HashSet<>();

    @GeoSpatialIndexed
    private GeoJsonPoint location;

    private Integer currentWorkload = 0;

    public Technician() {
        super();
        this.role = UserRole.TECHNICIAN;
        this.available = true;
        this.rating = 0.0;
        this.currentWorkload = 0;
    }

    public Technician(String name, String email, String phone, String password, String photoUrl) {
        super(name, email, phone, password, photoUrl, UserRole.TECHNICIAN);
        this.available = true;
        this.rating = 0.0;
        this.currentWorkload = 0;
    }
    public String getTechnicianId() {
        return getUserId();  // Returns the inherited userId
    }
    
    public void setTechnicianId(String technicianId) {
        setUserId(technicianId);  // Sets the inherited userId
    }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Set<Skill> getSkills() { return skills; }
    public void setSkills(Set<Skill> skills) { this.skills = skills; }

    public GeoJsonPoint getLocation() { return location; }
    public void setLocation(GeoJsonPoint location) { this.location = location; }

    public Integer getCurrentWorkload() { return currentWorkload; }
    public void setCurrentWorkload(Integer currentWorkload) { this.currentWorkload = currentWorkload; }

    public void unavailable() { this.available = false; }
    public void available() { this.available = true; }

    public boolean isAvailable() {
        return Boolean.TRUE.equals(this.available) &&
                this.currentWorkload != null &&
                this.currentWorkload < 5;
    }

    public void updateRating(Double newRating) { this.rating = newRating; }

    public void increaseWorkload() {
        if (this.currentWorkload == null) this.currentWorkload = 0;
        this.currentWorkload++;
        
        if (this.currentWorkload >=5 ) {
        	this.available = false ; 
        }
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
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", available=" + available +
                ", skills=" + skills +
                ", workload=" + currentWorkload +
                ", rating=" + rating +
                '}';
    }
}