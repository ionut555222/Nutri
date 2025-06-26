package com.example.project3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<StaffMember, Long> {
    
    Optional<StaffMember> findByUsername(String username);
    
    Optional<StaffMember> findByEmail(String email);
    
    Optional<StaffMember> findByEmployeeId(String employeeId);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    Boolean existsByEmployeeId(String employeeId);
    
    List<StaffMember> findByIsActiveTrue();
    
    List<StaffMember> findByIsActiveFalse();
    
    List<StaffMember> findByDepartment(String department);
    
    @Query("SELECT s FROM StaffMember s JOIN s.roles r WHERE r.name = ?1")
    List<StaffMember> findByRole(RoleName roleName);
    
    @Query("SELECT s FROM StaffMember s JOIN s.roles r WHERE r.name = 'ROLE_OWNER'")
    List<StaffMember> findOwners();
    
    @Query("SELECT s FROM StaffMember s JOIN s.roles r WHERE r.name = 'ROLE_ADMIN'")
    List<StaffMember> findAdmins();
    
    @Query("SELECT s FROM StaffMember s JOIN s.roles r WHERE r.name = 'ROLE_EMPLOYEE'")
    List<StaffMember> findEmployees();
    
    @Query("SELECT s FROM StaffMember s WHERE s.createdBy = ?1")
    List<StaffMember> findByCreatedBy(Long createdBy);
} 