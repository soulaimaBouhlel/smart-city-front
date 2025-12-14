package com.smartcity.service;

import com.smartcity.dto.SignInRequest;
import com.smartcity.dto.SignUpRequest;
import com.smartcity.dto.AuthResponse;
import com.smartcity.model.*;
import com.smartcity.repository.*;
import com.smartcity.security.CustomUserDetailsService;
import com.smartcity.util.JwtUtil;
import com.smartcity.util.CustomPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private UserAdminRepository userAdminRepository;

    @Autowired
    private MaintenanceAdminRepository maintenanceAdminRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomPasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // Helper method to validate role
    private boolean isValidRole(String role) {
        if (role == null) return false;
        String upperRole = role.toUpperCase();
        return upperRole.equals("CITIZEN") || 
               upperRole.equals("TECHNICIAN") || 
               upperRole.equals("USER_ADMIN") || 
               upperRole.equals("MAINTENANCE_ADMIN");
    }

    public AuthResponse signUp(SignUpRequest signUpRequest) {
        // Validate input
        if (signUpRequest.getEmail() == null || signUpRequest.getEmail().trim().isEmpty()) {
            return new AuthResponse(false, "Email is required!");
        }

        if (signUpRequest.getPassword() == null || signUpRequest.getPassword().length() < 6) {
            return new AuthResponse(false, "Password must be at least 6 characters long!");
        }

        if (signUpRequest.getRole() == null || !isValidRole(signUpRequest.getRole())) {
            return new AuthResponse(false, "Role must be either CITIZEN, TECHNICIAN, USER_ADMIN, or MAINTENANCE_ADMIN!");
        }

        // Additional validation for admins
        String role = signUpRequest.getRole().toUpperCase();
        if ((role.equals("USER_ADMIN") || role.equals("MAINTENANCE_ADMIN")) && 
            (signUpRequest.getDepartment() == null || signUpRequest.getDepartment().trim().isEmpty())) {
            return new AuthResponse(false, "Department is required for admin roles!");
        }

        String email = signUpRequest.getEmail().toLowerCase();

        // Check if email exists in any collection
        if (citizenRepository.existsByEmail(email) || 
            technicianRepository.existsByEmail(email) ||
            userAdminRepository.existsByEmail(email) ||
            maintenanceAdminRepository.existsByEmail(email)) {
            return new AuthResponse(false, "This email is already registered!");
        }

        try {
            String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());

            switch (role) {
                case "CITIZEN":
                    Citizen citizen = new Citizen(
                        signUpRequest.getName(),
                        email,
                        signUpRequest.getPhone(),
                        encodedPassword,
                        signUpRequest.getPhotoUrl()
                    );
                    Citizen savedCitizen = citizenRepository.save(citizen);

                    String citizenToken = jwtUtil.generateToken(
                        savedCitizen.getCitizenId(),
                        savedCitizen.getEmail(),
                        "CITIZEN"
                    );

                    return new AuthResponse(
                        true,
                        "Citizen account created successfully!",
                        savedCitizen.getCitizenId(),
                        savedCitizen.getName(),
                        savedCitizen.getEmail(),
                        "CITIZEN",
                        citizenToken
                    );

                case "TECHNICIAN":
                    Technician technician = new Technician(
                        signUpRequest.getName(),
                        email,
                        signUpRequest.getPhone(),
                        encodedPassword,
                        signUpRequest.getPhotoUrl()
                    );

                    if (signUpRequest.getInitialRating() != null) {
                        technician.setRating(signUpRequest.getInitialRating());
                    }

                    Technician savedTechnician = technicianRepository.save(technician);

                    String technicianToken = jwtUtil.generateToken(
                        savedTechnician.getTechnicianId(),
                        savedTechnician.getEmail(),
                        "TECHNICIAN"
                    );

                    return new AuthResponse(
                        true,
                        "Technician account created successfully!",
                        savedTechnician.getTechnicianId(),
                        savedTechnician.getName(),
                        savedTechnician.getEmail(),
                        "TECHNICIAN",
                        technicianToken
                    );

                case "USER_ADMIN":
                    UserAdmin userAdmin = new UserAdmin(
                        signUpRequest.getName(),
                        email,
                        signUpRequest.getPhone(),
                        encodedPassword,
                        signUpRequest.getPhotoUrl(),
                        signUpRequest.getDepartment()
                    );
                    userAdmin.setActive(true);
                    
                    UserAdmin savedUserAdmin = userAdminRepository.save(userAdmin);

                    String userAdminToken = jwtUtil.generateToken(
                        savedUserAdmin.getUserAdminId(),
                        savedUserAdmin.getEmail(),
                        "USER_ADMIN"
                    );

                    return new AuthResponse(
                        true,
                        "User Admin account created successfully!",
                        savedUserAdmin.getUserAdminId(),
                        savedUserAdmin.getName(),
                        savedUserAdmin.getEmail(),
                        "USER_ADMIN",
                        userAdminToken
                    );

                case "MAINTENANCE_ADMIN":
                    MaintenanceAdmin maintenanceAdmin = new MaintenanceAdmin(
                        signUpRequest.getName(),
                        email,
                        signUpRequest.getPhone(),
                        encodedPassword,
                        signUpRequest.getPhotoUrl(),
                        signUpRequest.getDepartment()
                    );
                    maintenanceAdmin.setActive(true);
                    
                    MaintenanceAdmin savedMaintenanceAdmin = maintenanceAdminRepository.save(maintenanceAdmin);

                    String maintenanceAdminToken = jwtUtil.generateToken(
                        savedMaintenanceAdmin.getMaintenanceAdminId(),
                        savedMaintenanceAdmin.getEmail(),
                        "MAINTENANCE_ADMIN"
                    );

                    return new AuthResponse(
                        true,
                        "Maintenance Admin account created successfully!",
                        savedMaintenanceAdmin.getMaintenanceAdminId(),
                        savedMaintenanceAdmin.getName(),
                        savedMaintenanceAdmin.getEmail(),
                        "MAINTENANCE_ADMIN",
                        maintenanceAdminToken
                    );

                default:
                    return new AuthResponse(false, "Invalid role specified!");
            }
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return new AuthResponse(false, "Registration failed: " + e.getMessage());
        }
    }

    // Updated signIn method to support admins
    public AuthResponse signIn(SignInRequest signInRequest) {
        // Input validation
        if (signInRequest.getEmail() == null || signInRequest.getEmail().trim().isEmpty()) {
            return new AuthResponse(false, "Email is required!");
        }

        if (signInRequest.getPassword() == null || signInRequest.getPassword().isEmpty()) {
            return new AuthResponse(false, "Password is required!");
        }

        if (signInRequest.getRole() == null || !isValidRole(signInRequest.getRole())) {
            return new AuthResponse(false, "Please select a valid role!");
        }
        
        String email = signInRequest.getEmail().toLowerCase();
        String role = signInRequest.getRole().toUpperCase();
        
        try {
            switch (role) {
                case "CITIZEN":
                    Optional<Citizen> citizenOpt = citizenRepository.findByEmail(email);
                    if (citizenOpt.isEmpty()) {
                        return new AuthResponse(false, "No citizen account found with this email!");
                    }
                    Citizen citizen = citizenOpt.get();
                    if (!passwordEncoder.matches(signInRequest.getPassword(), citizen.getPassword())) {
                        return new AuthResponse(false, "Incorrect password!");
                    }
                    
                    // Set authentication
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    String citizenToken = jwtUtil.generateToken(citizen.getCitizenId(), citizen.getEmail(), "CITIZEN");

                    return new AuthResponse(
                        true,
                        "Citizen login successful! Welcome back, " + citizen.getName(),
                        citizen.getCitizenId(),
                        citizen.getName(),
                        citizen.getEmail(),
                        "CITIZEN",
                        citizenToken
                    );

                case "TECHNICIAN":
                    Optional<Technician> technicianOpt = technicianRepository.findByEmail(email);
                    if (technicianOpt.isEmpty()) {
                        return new AuthResponse(false, "No technician account found with this email!");
                    }
                    Technician technician = technicianOpt.get();
                    if (!passwordEncoder.matches(signInRequest.getPassword(), technician.getPassword())) {
                        return new AuthResponse(false, "Incorrect password!");
                    }
                    
                    // Set authentication
                    userDetails = userDetailsService.loadUserByUsername(email);
                    authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    String technicianToken = jwtUtil.generateToken(technician.getTechnicianId(), technician.getEmail(), "TECHNICIAN");

                    return new AuthResponse(
                        true,
                        "Technician login successful! Welcome back, " + technician.getName(),
                        technician.getTechnicianId(),
                        technician.getName(),
                        technician.getEmail(),
                        "TECHNICIAN",
                        technicianToken
                    );

                case "USER_ADMIN":
                    Optional<UserAdmin> userAdminOpt = userAdminRepository.findByEmail(email);
                    if (userAdminOpt.isEmpty()) {
                        return new AuthResponse(false, "No user admin account found with this email!");
                    }
                    UserAdmin userAdmin = userAdminOpt.get();
                    if (!passwordEncoder.matches(signInRequest.getPassword(), userAdmin.getPassword())) {
                        return new AuthResponse(false, "Incorrect password!");
                    }
                    if (!Boolean.TRUE.equals(userAdmin.getActive())) {
                        return new AuthResponse(false, "This admin account is deactivated!");
                    }
                    
                    // Set authentication
                    userDetails = userDetailsService.loadUserByUsername(email);
                    authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    String userAdminToken = jwtUtil.generateToken(userAdmin.getUserAdminId(), userAdmin.getEmail(), "USER_ADMIN");

                    return new AuthResponse(
                        true,
                        "User Admin login successful! Welcome back, " + userAdmin.getName(),
                        userAdmin.getUserAdminId(),
                        userAdmin.getName(),
                        userAdmin.getEmail(),
                        "USER_ADMIN",
                        userAdminToken
                    );

                case "MAINTENANCE_ADMIN":
                    Optional<MaintenanceAdmin> maintenanceAdminOpt = maintenanceAdminRepository.findByEmail(email);
                    if (maintenanceAdminOpt.isEmpty()) {
                        return new AuthResponse(false, "No maintenance admin account found with this email!");
                    }
                    MaintenanceAdmin maintenanceAdmin = maintenanceAdminOpt.get();
                    if (!passwordEncoder.matches(signInRequest.getPassword(), maintenanceAdmin.getPassword())) {
                        return new AuthResponse(false, "Incorrect password!");
                    }
                    if (!Boolean.TRUE.equals(maintenanceAdmin.getActive())) {
                        return new AuthResponse(false, "This admin account is deactivated!");
                    }
                    
                    // Set authentication
                    userDetails = userDetailsService.loadUserByUsername(email);
                    authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    String maintenanceAdminToken = jwtUtil.generateToken(
                        maintenanceAdmin.getMaintenanceAdminId(), 
                        maintenanceAdmin.getEmail(), 
                        "MAINTENANCE_ADMIN"
                    );

                    return new AuthResponse(
                        true,
                        "Maintenance Admin login successful! Welcome back, " + maintenanceAdmin.getName(),
                        maintenanceAdmin.getMaintenanceAdminId(),
                        maintenanceAdmin.getName(),
                        maintenanceAdmin.getEmail(),
                        "MAINTENANCE_ADMIN",
                        maintenanceAdminToken
                    );

                default:
                    return new AuthResponse(false, "Invalid role specified!");
            }
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return new AuthResponse(false, "Login failed: " + e.getMessage());
        }
    }
}