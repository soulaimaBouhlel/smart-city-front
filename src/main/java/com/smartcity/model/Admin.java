package com.smartcity.model;

import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "admins")
public abstract class Admin extends User {
    private String department;
    private Boolean isActive;
    private Date joinDate;
    private Set<AdminPermission> permissions;

    public Admin() {
        super();
        this.isActive = true;
        this.permissions = new HashSet<>();
        this.joinDate = new Date();
    }

    public Admin(String name, String email, String phone, String password, 
                 String photoUrl, UserRole role, String department) {
        super(name, email, phone, password, photoUrl, role);
        this.department = department;
        this.isActive = true;
        this.permissions = new HashSet<>();
        this.joinDate = new Date();
    }

    // ==================== ALIAS METHOD ====================
    public String getAdminId() {
        return getUserId();
    }
    
    public void setAdminId(String adminId) {
        setUserId(adminId);
    }
    // =====================================================

    // Getters and Setters
    public String getDepartment() { 
        return department; 
    }
    
    public void setDepartment(String department) { 
        this.department = department; 
    }

    public Boolean getActive() { 
        return isActive; 
    }
    
    public void setActive(Boolean active) { 
        isActive = active; 
    }

    public Date getJoinDate() { 
        return joinDate; 
    }
    
    public void setJoinDate(Date joinDate) { 
        this.joinDate = joinDate; 
    }

    public Set<AdminPermission> getPermissions() { 
        return permissions; 
    }
    
    public void setPermissions(Set<AdminPermission> permissions) { 
        this.permissions = permissions; 
    }

    // Business methods
    public boolean hasPermission(AdminPermission permission) {
        return permissions != null && permissions.contains(permission);
    }

    public void activate() { 
        this.isActive = true; 
    }
    
    public void deactivate() { 
        this.isActive = false; 
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "adminId='" + getAdminId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", role=" + getRole() +
                ", department='" + department + '\'' +
                ", isActive=" + isActive +
                ", joinDate=" + joinDate +
                ", permissions=" + permissions +
                '}';
    }
}