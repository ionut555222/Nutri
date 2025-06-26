package com.example.project3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByUsername(String username);
    
    Optional<Customer> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    List<Customer> findByIsActiveTrue();
    
    List<Customer> findByIsActiveFalse();
    
    List<Customer> findByEmailVerifiedFalse();
    
    @Query("SELECT c FROM Customer c WHERE c.createdAt >= ?1")
    List<Customer> findNewCustomers(LocalDateTime since);
    
    @Query("SELECT c FROM Customer c WHERE c.lastLogin < ?1")
    List<Customer> findInactiveCustomers(LocalDateTime since);
    
    @Query("SELECT c FROM Customer c WHERE c.profile.customerSegment = ?1")
    List<Customer> findByCustomerSegment(String segment);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt >= ?1")
    Long countNewCustomers(LocalDateTime since);
    
    @Query("SELECT c FROM Customer c ORDER BY c.createdAt DESC")
    List<Customer> findAllOrderByCreatedAtDesc();
} 