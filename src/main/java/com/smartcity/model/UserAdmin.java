package com.smartcity.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "user_admins")
public class UserAdmin extends Admin {
    
    // Add @Field annotations for inherited fields
    @Field("email")
    @Override
    public String getEmail() {
        return super.getEmail();
    }
    
    @Field("email")
    @Override
    public void setEmail(String email) {
        super.setEmail(email);
    }
    
    @Field("name")
    @Override
    public String getName() {
        return super.getName();
    }
    
    @Field("name")
    @Override
    public void setName(String name) {
        super.setName(name);
    }
    
    @Field("password")
    @Override
    public String getPassword() {
        return super.getPassword();
    }
    
    @Field("password")
    @Override
    public void setPassword(String password) {
        super.setPassword(password);
    }
    
    // Constructor
    public UserAdmin() {
        super();
        this.setRole(UserRole.USER_ADMIN);
    }

    public UserAdmin(String name, String email, String phone, String password, 
                     String photoUrl, String department) {
        super(name, email, phone, password, photoUrl, UserRole.USER_ADMIN, department);
    }

    // ==================== ALIAS METHODS ====================
    public String getUserAdminId() {
        return getUserId();
    }

    public void setUserAdminId(String userAdminId) {
        setUserId(userAdminId);
    }
    // ======================================================

    // Business methods from diagram
    public void createUser(User user) {
        System.out.println("Creating user: " + user.getEmail());
        // User creation logic here
    }

    public void updateUser(User user) {
        System.out.println("Updating user: " + user.getEmail());
        // User update logic here
    }
    
    public void deleteUser(String userId) {
        System.out.println("Deleting user with ID: " + userId);
        // User deletion logic here
    }
    
    public void approveTechnician(Technician technician) {
        System.out.println("Approving technician: " + technician.getName());
        // Technician approval logic here
    }
    
    public void sendNotification(Notification notification) {
        System.out.println("Sending notification: " + notification.getTitle());
        // Notification sending logic here
    }
    
    public void deleteNotification(String notificationId) {
        System.out.println("Deleting notification with ID: " + notificationId);
        // Notification deletion logic here
    }

    @Override
    public String toString() {
        return "UserAdmin{" +
                "userAdminId='" + getUserAdminId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", department='" + getDepartment() + '\'' +
                ", isActive=" + getActive() +
                ", joinDate=" + getJoinDate() +
                '}';
    }
}