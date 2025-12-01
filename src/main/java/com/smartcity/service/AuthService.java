package com.smartcity.service;

import com.smartcity.dto.SignInRequest;
import com.smartcity.dto.SignUpRequest;
import com.smartcity.dto.AuthResponse;
import com.smartcity.model.Citizen;
import com.smartcity.model.Technician;
import com.smartcity.repository.CitizenRepository;
import com.smartcity.repository.TechnicianRepository;
import com.smartcity.security.CustomUserDetailsService; // NEW Import
import com.smartcity.util.JwtUtil;
import com.smartcity.util.CustomPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // NEW Import
import org.springframework.security.core.context.SecurityContextHolder; // NEW Import
import org.springframework.security.core.userdetails.UserDetails; // NEW Import
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomPasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userDetailsService; // NEW

    // ... (Your existing signUp method remains unchanged)

    public AuthResponse signUp(SignUpRequest signUpRequest) {
        // Validate input
        if (signUpRequest.getEmail() == null || signUpRequest.getEmail().trim().isEmpty()) {
            return new AuthResponse(false, "Email is required!");
        }

        if (signUpRequest.getPassword() == null || signUpRequest.getPassword().length() < 6) {
            return new AuthResponse(false, "Password must be at least 6 characters long!");
        }

        if (signUpRequest.getRole() == null || (!signUpRequest.getRole().equalsIgnoreCase("CITIZEN") &&
                                                !signUpRequest.getRole().equalsIgnoreCase("TECHNICIAN"))) {
            return new AuthResponse(false, "Role must be either CITIZEN or TECHNICIAN!");
        }

        String email = signUpRequest.getEmail().toLowerCase();

        // Check if email exists in the CORRECT collection based on role
        if ("CITIZEN".equalsIgnoreCase(signUpRequest.getRole())) {
            if (citizenRepository.existsByEmail(email)) {
                return new AuthResponse(false, "This email is already registered as a Citizen!");
            }
        } else {
            if (technicianRepository.existsByEmail(email)) {
                return new AuthResponse(false, "This email is already registered as a Technician!");
            }
        }

        try {
            String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());

            if ("CITIZEN".equalsIgnoreCase(signUpRequest.getRole())) {
                Citizen citizen = new Citizen(
                    signUpRequest.getName(),
                    email,
                    signUpRequest.getPhone(),
                    encodedPassword,
                    signUpRequest.getPhotoUrl()
                );
                Citizen savedCitizen = citizenRepository.save(citizen);

                String token = jwtUtil.generateToken(
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
                    token
                );

            } else if ("TECHNICIAN".equalsIgnoreCase(signUpRequest.getRole())) {
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

                String token = jwtUtil.generateToken(
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
                    token
                );
            }
        } catch (Exception e) {
            return new AuthResponse(false, "Registration failed: " + e.getMessage());
        }

        return new AuthResponse(false, "Unexpected error during registration");
    }

    // 🛑 UPDATED signIn METHOD
    public AuthResponse signIn(SignInRequest signInRequest) {
        // ... (Input validation unchanged)
        if (signInRequest.getEmail() == null || signInRequest.getEmail().trim().isEmpty()) {
            return new AuthResponse(false, "Email is required!");
        }

        if (signInRequest.getPassword() == null || signInRequest.getPassword().isEmpty()) {
            return new AuthResponse(false, "Password is required!");
        }

        if (signInRequest.getRole() == null || (!signInRequest.getRole().equalsIgnoreCase("CITIZEN") &&
                                                !signInRequest.getRole().equalsIgnoreCase("TECHNICIAN"))) {
            return new AuthResponse(false, "Please select a valid role (CITIZEN or TECHNICIAN)!");
        }
        
        String email = signInRequest.getEmail().toLowerCase();
        
        try {
            
            if ("CITIZEN".equalsIgnoreCase(signInRequest.getRole())) {
                Optional<Citizen> citizenOpt = citizenRepository.findByEmail(email);
                if (citizenOpt.isEmpty()) {
                    return new AuthResponse(false, "No citizen account found with this email!");
                }

                Citizen citizen = citizenOpt.get();
                if (!passwordEncoder.matches(signInRequest.getPassword(), citizen.getPassword())) {
                    return new AuthResponse(false, "Incorrect password for citizen account!");
                }

                // 🔑 SUCCESS: Manually authenticate and set SecurityContext
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                String token = jwtUtil.generateToken(citizen.getCitizenId(), citizen.getEmail(), "CITIZEN");

                return new AuthResponse(
                    true,
                    "Citizen login successful! Welcome back, " + citizen.getName(),
                    citizen.getCitizenId(),
                    citizen.getName(),
                    citizen.getEmail(),
                    "CITIZEN",
                    token
                );

            } else if ("TECHNICIAN".equalsIgnoreCase(signInRequest.getRole())) {
                Optional<Technician> technicianOpt = technicianRepository.findByEmail(email);
                if (technicianOpt.isEmpty()) {
                    return new AuthResponse(false, "No technician account found with this email!");
                }

                Technician technician = technicianOpt.get();
                if (!passwordEncoder.matches(signInRequest.getPassword(), technician.getPassword())) {
                    return new AuthResponse(false, "Incorrect password for technician account!");
                }
                
                // 🔑 SUCCESS: Manually authenticate and set SecurityContext
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String token = jwtUtil.generateToken(technician.getTechnicianId(), technician.getEmail(), "TECHNICIAN");

                return new AuthResponse(
                    true,
                    "Technician login successful! Welcome back, " + technician.getName(),
                    technician.getTechnicianId(),
                    technician.getName(),
                    technician.getEmail(),
                    "TECHNICIAN",
                    token
                );
            }
        } catch (Exception e) {
            // Catch UsernameNotFoundException from userDetailsService or other runtime errors
            return new AuthResponse(false, "Login failed: " + e.getMessage());
        }

        return new AuthResponse(false, "Unexpected error during login");
    }
}