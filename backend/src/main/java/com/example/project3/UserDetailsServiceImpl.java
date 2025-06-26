package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    CustomerRepository customerRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First, try to find in staff/user repository
        var userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return UserDetailsImpl.build(userOptional.get());
        }
        
        // If not found in staff, try to find in customer repository
        var customerOptional = customerRepository.findByUsername(username);
        if (customerOptional.isPresent()) {
            return CustomerDetailsImpl.build(customerOptional.get());
        }
        
        // Also try to find customer by email (as customers can login with email)
        var customerByEmailOptional = customerRepository.findByEmail(username);
        if (customerByEmailOptional.isPresent()) {
            return CustomerDetailsImpl.build(customerByEmailOptional.get());
        }

        throw new UsernameNotFoundException("User Not Found with username: " + username);
    }
} 