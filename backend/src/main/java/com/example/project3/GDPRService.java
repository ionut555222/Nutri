package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class GDPRService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;
    
    @Autowired
    private CustomerProfileRepository customerProfileRepository;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Handle data subject access request (Article 15)
     * Provides complete data export for a customer
     */
    public GDPRDataExport handleDataAccessRequest(Long customerId, String requestReason) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            
            GDPRDataExport export = new GDPRDataExport();
            export.setCustomerId(customerId);
            export.setExportDate(LocalDateTime.now());
            export.setRequestReason(requestReason);
            
            // Personal Information
            export.setPersonalData(buildPersonalDataExport(customer));
            
            // Order History
            export.setOrderHistory(buildOrderHistoryExport(customerId));
            
            // Delivery Addresses
            export.setDeliveryAddresses(buildDeliveryAddressExport(customerId));
            
            // Customer Profile
            export.setCustomerProfile(buildCustomerProfileExport(customerId));
            
            // Chat History
            export.setChatHistory(buildChatHistoryExport(customerId));
            
            return export;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to process data access request", e);
        }
    }
    
    /**
     * Handle right to rectification (Article 16)
     */
    public void handleDataRectificationRequest(Long customerId, Map<String, Object> corrections, String requestReason) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            
            // Apply corrections to customer data
            if (corrections.containsKey("email")) {
                customer.setEmail((String) corrections.get("email"));
            }
            if (corrections.containsKey("firstName")) {
                customer.setFirstName((String) corrections.get("firstName"));
            }
            if (corrections.containsKey("lastName")) {
                customer.setLastName((String) corrections.get("lastName"));
            }
            if (corrections.containsKey("phoneNumber")) {
                customer.setPhoneNumber((String) corrections.get("phoneNumber"));
            }
            
            customerRepository.save(customer);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to process data rectification request", e);
        }
    }
    
    /**
     * Handle right to erasure (Article 17) - "Right to be forgotten"
     */
    public void handleDataErasureRequest(Long customerId, String requestReason, boolean hardDelete) {
        try {
            if (hardDelete) {
                performHardDelete(customerId);
            } else {
                performSoftDelete(customerId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process data erasure request", e);
        }
    }
    
    /**
     * Handle data portability request (Article 20)
     */
    public byte[] handleDataPortabilityRequest(Long customerId, String format, String requestReason) {
        try {
            GDPRDataExport export = handleDataAccessRequest(customerId, requestReason);
            
            if ("json".equals(format.toLowerCase())) {
                return objectMapper.writeValueAsBytes(export);
            } else if ("zip".equals(format.toLowerCase())) {
                return createZipExport(export);
            } else {
                throw new IllegalArgumentException("Unsupported format: " + format);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to process data portability request", e);
        }
    }
    
    /**
     * Check if processing is lawful under GDPR
     */
    public boolean isProcessingLawful(Long customerId, String processingPurpose) {
        try {
            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer == null) return false;
            
            // For this simplified version, we assume basic processing is lawful
            // In a real implementation, this would check consent status
            switch (processingPurpose.toLowerCase()) {
                case "order_processing":
                case "delivery":
                    return true; // Contract basis
                case "marketing":
                case "analytics":
                case "profiling":
                    return true; // Would check consent in real implementation
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    // Private helper methods
    
    private void performHardDelete(Long customerId) {
        // Delete in order to maintain referential integrity
        // Note: In a real implementation, you'd need to add deleteByCustomerId methods to repositories
        customerRepository.deleteById(customerId);
    }
    
    private void performSoftDelete(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null) {
            // Anonymize personal data
            customer.setEmail("deleted_user_" + customerId + "@anonymized.local");
            customer.setFirstName("Deleted");
            customer.setLastName("User");
            customer.setPhoneNumber(null);
            
            customerRepository.save(customer);
        }
    }
    
    private PersonalDataExport buildPersonalDataExport(Customer customer) {
        PersonalDataExport data = new PersonalDataExport();
        data.setCustomerId(customer.getId());
        data.setEmail(customer.getEmail());
        data.setFirstName(customer.getFirstName());
        data.setLastName(customer.getLastName());
        data.setPhoneNumber(customer.getPhoneNumber());
        data.setRegistrationDate(customer.getCreatedAt());
        return data;
    }
    
    private List<OrderExport> buildOrderHistoryExport(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(order -> {
                    OrderExport export = new OrderExport();
                    export.setOrderId(order.getId());
                    export.setOrderDate(order.getOrderDate());
                    export.setTotalAmount(order.getTotalAmount());
                    export.setFulfilled(order.isFulfilled());
                    return export;
                })
                .collect(Collectors.toList());
    }
    
    private List<AddressExport> buildDeliveryAddressExport(Long customerId) {
        return deliveryAddressRepository.findByCustomerIdOrderByIsDefaultDescCreatedAtAsc(customerId).stream()
                .map(address -> {
                    AddressExport export = new AddressExport();
                    export.setAddressId(address.getId());
                    export.setStreetAddress(address.getStreetAddress());
                    export.setCity(address.getCity());
                    export.setZipCode(address.getZipCode());
                    export.setCountry(address.getCountry());
                    export.setDeliveryInstructions(address.getDeliveryInstructions());
                    export.setIsDefault(address.getIsDefault());
                    export.setCreatedAt(address.getCreatedAt());
                    return export;
                })
                .collect(Collectors.toList());
    }
    
    private CustomerProfileExport buildCustomerProfileExport(Long customerId) {
        Optional<CustomerProfile> profile = customerProfileRepository.findByCustomerId(customerId);
        if (profile.isPresent()) {
            CustomerProfile p = profile.get();
            CustomerProfileExport export = new CustomerProfileExport();
            export.setTotalSpent(p.getTotalSpent());
            export.setAverageOrderValue(p.getAverageOrderValue());
            export.setLastOrderDate(p.getLastOrderDate());
            return export;
        }
        return null;
    }
    
    private List<ChatExport> buildChatHistoryExport(Long customerId) {
        // Simplified - in real implementation would need findByCustomerId method
        return new ArrayList<>();
    }
    
    private byte[] createZipExport(GDPRDataExport export) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
                
                // Add personal data
                zos.putNextEntry(new java.util.zip.ZipEntry("personal_data.json"));
                zos.write(objectMapper.writeValueAsBytes(export.getPersonalData()));
                zos.closeEntry();
                
                // Add order history
                zos.putNextEntry(new java.util.zip.ZipEntry("order_history.json"));
                zos.write(objectMapper.writeValueAsBytes(export.getOrderHistory()));
                zos.closeEntry();
                
                // Add complete export
                zos.putNextEntry(new java.util.zip.ZipEntry("complete_data_export.json"));
                zos.write(objectMapper.writeValueAsBytes(export));
                zos.closeEntry();
            }
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ZIP export", e);
        }
    }
} 