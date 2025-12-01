package com.smartcity.security;

import com.smartcity.model.Citizen;
import com.smartcity.model.Technician;
import com.smartcity.repository.CitizenRepository;
import com.smartcity.repository.TechnicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

// NOTE: We assume email is the 'username' for Spring Security purposes.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    // This method is called by the JWT filter to validate the token's subject (email)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Check Citizen repository
        Optional<Citizen> citizenOpt = citizenRepository.findByEmail(email);
        if (citizenOpt.isPresent()) {
            Citizen citizen = citizenOpt.get();
            return new User(
                citizen.getEmail(),
                citizen.getPassword(), // Encoded password
                mapRolesToAuthorities("CITIZEN")
            );
        }

        // 2. Check Technician repository
        Optional<Technician> technicianOpt = technicianRepository.findByEmail(email);
        if (technicianOpt.isPresent()) {
            Technician technician = technicianOpt.get();
            return new User(
                technician.getEmail(),
                technician.getPassword(), // Encoded password
                mapRolesToAuthorities("TECHNICIAN")
            );
        }

        // 3. User not found in either repository
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(String role) {
        // Spring Security roles must start with "ROLE_" convention
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }
}