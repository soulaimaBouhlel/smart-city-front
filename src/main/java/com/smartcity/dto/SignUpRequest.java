package com.smartcity.dto;

public class SignUpRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String photoUrl;
    private String role; // "CITIZEN" or "TECHNICIAN"
    
    // For Technician specific fields (optional)
    private Double initialRating;
    
    // Constructors
    public SignUpRequest() {}
    
    public SignUpRequest(String name, String email, String phone, String password, String role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }
    
    // Getters and Setters (REMOVE logID getters/setters)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Double getInitialRating() { return initialRating; }
    public void setInitialRating(Double initialRating) { this.initialRating = initialRating; }
}