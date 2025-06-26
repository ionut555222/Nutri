package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffDetailsService implements UserDetailsService {
    
    @Autowired
    StaffRepository staffRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        StaffMember staff = staffRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Staff member not found with username: " + username));

        if (!staff.getIsActive()) {
            throw new UsernameNotFoundException("Staff member is inactive: " + username);
        }

        return StaffDetailsImpl.build(staff);
    }
} 