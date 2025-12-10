package com.smartcity.security;

import com.smartcity.model.*;
import com.smartcity.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private UserAdminRepository userAdminRepository;

    @Autowired
    private MaintenanceAdminRepository maintenanceAdminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("CustomUserDetailsService: Looking for user with email: " + email);
        
        // Try to find user in all repositories
        // 1. Check Citizen
        Citizen citizen = citizenRepository.findByEmail(email).orElse(null);
        if (citizen != null) {
            System.out.println("Found citizen: " + citizen.getEmail());
            return buildUserDetails(citizen.getEmail(), citizen.getPassword(), "ROLE_CITIZEN");
        }

        // 2. Check Technician
        Technician technician = technicianRepository.findByEmail(email).orElse(null);
        if (technician != null) {
            System.out.println("Found technician: " + technician.getEmail());
            return buildUserDetails(technician.getEmail(), technician.getPassword(), "ROLE_TECHNICIAN");
        }

        // 3. Check UserAdmin
        UserAdmin userAdmin = userAdminRepository.findUserAdminByEmail(email);
        if (userAdmin != null) {
            System.out.println("Found user admin: " + userAdmin.getEmail());
            // UserAdmin should have multiple authorities
            return buildAdminUserDetails(userAdmin);
        }

        // 4. Check MaintenanceAdmin
        MaintenanceAdmin maintenanceAdmin = maintenanceAdminRepository.findMaintenanceAdminByEmail(email);
        if (maintenanceAdmin != null) {
            System.out.println("Found maintenance admin: " + maintenanceAdmin.getEmail());
            // MaintenanceAdmin should have multiple authorities
            return buildMaintenanceAdminUserDetails(maintenanceAdmin);
        }

        System.out.println("User not found with email: " + email);
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    private UserDetails buildUserDetails(String email, String password, String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        
        System.out.println("Building UserDetails for: " + email + " with role: " + role);
        return new User(email, password, authorities);
    }
    
    private UserDetails buildAdminUserDetails(UserAdmin userAdmin) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add base role
        authorities.add(new SimpleGrantedAuthority("ROLE_USER_ADMIN"));
        
        // Add additional permissions based on admin's permissions
        if (userAdmin.getPermissions() != null) {
            userAdmin.getPermissions().forEach(permission -> {
                authorities.add(new SimpleGrantedAuthority(permission.name()));
            });
        }
        
        // Add default admin permissions
        authorities.add(new SimpleGrantedAuthority("ADMIN_READ"));
        authorities.add(new SimpleGrantedAuthority("ADMIN_WRITE"));
        authorities.add(new SimpleGrantedAuthority("ADMIN_UPDATE"));
        authorities.add(new SimpleGrantedAuthority("ADMIN_DELETE"));
        
        System.out.println("Building UserAdmin UserDetails with authorities: " + authorities);
        return new User(userAdmin.getEmail(), userAdmin.getPassword(), authorities);
    }
    
    private UserDetails buildMaintenanceAdminUserDetails(MaintenanceAdmin maintenanceAdmin) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add base role
        authorities.add(new SimpleGrantedAuthority("ROLE_MAINTENANCE_ADMIN"));
        
        // Add additional permissions
        if (maintenanceAdmin.getPermissions() != null) {
            maintenanceAdmin.getPermissions().forEach(permission -> {
                authorities.add(new SimpleGrantedAuthority(permission.name()));
            });
        }
        
        // Add default permissions
        authorities.add(new SimpleGrantedAuthority("MAINTENANCE_READ"));
        authorities.add(new SimpleGrantedAuthority("MAINTENANCE_WRITE"));
        authorities.add(new SimpleGrantedAuthority("MAINTENANCE_UPDATE"));
        
        System.out.println("Building MaintenanceAdmin UserDetails with authorities: " + authorities);
        return new User(maintenanceAdmin.getEmail(), maintenanceAdmin.getPassword(), authorities);
    }
}