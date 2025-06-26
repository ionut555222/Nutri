package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gdpr")
public class GDPRController {
    
    @Autowired
    private GDPRService gdprService;
    
    /**
     * Data Subject Access Request (Article 15)
     * GET /api/gdpr/data-export/{customerId}
     */
    @GetMapping("/data-export/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<GDPRDataExport> requestDataExport(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "User requested data export") String reason) {
        
        try {
            GDPRDataExport export = gdprService.handleDataAccessRequest(customerId, reason);
            return ResponseEntity.ok(export);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Data Portability Request (Article 20)
     * GET /api/gdpr/data-portability/{customerId}
     */
    @GetMapping("/data-portability/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> requestDataPortability(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "json") String format,
            @RequestParam(defaultValue = "User requested data portability") String reason) {
        
        try {
            byte[] data = gdprService.handleDataPortabilityRequest(customerId, format, reason);
            
            HttpHeaders headers = new HttpHeaders();
            if ("zip".equals(format.toLowerCase())) {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "customer_data_" + customerId + ".zip");
            } else {
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setContentDispositionFormData("attachment", "customer_data_" + customerId + ".json");
            }
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Data Rectification Request (Article 16)
     * PUT /api/gdpr/rectify/{customerId}
     */
    @PutMapping("/rectify/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<String> requestDataRectification(
            @PathVariable Long customerId,
            @RequestBody Map<String, Object> corrections,
            @RequestParam(defaultValue = "User requested data correction") String reason) {
        
        try {
            gdprService.handleDataRectificationRequest(customerId, corrections, reason);
            return ResponseEntity.ok("Data rectification completed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process data rectification: " + e.getMessage());
        }
    }
    
    /**
     * Right to Erasure Request (Article 17)
     * DELETE /api/gdpr/erase/{customerId}
     */
    @DeleteMapping("/erase/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<String> requestDataErasure(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "false") String hardDelete,
            @RequestParam(defaultValue = "User requested data deletion") String reason) {
        
        try {
            boolean isHardDelete = Boolean.parseBoolean(hardDelete);
            gdprService.handleDataErasureRequest(customerId, reason, isHardDelete);
            String message = isHardDelete ? 
                "All customer data has been permanently deleted" : 
                "Customer data has been anonymized";
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process data erasure: " + e.getMessage());
        }
    }
    
    /**
     * Check Processing Lawfulness
     * GET /api/gdpr/processing-lawful/{customerId}
     */
    @GetMapping("/processing-lawful/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkProcessingLawfulness(
            @PathVariable Long customerId,
            @RequestParam List<String> purposes) {
        
        try {
            Map<String, Boolean> results = new java.util.HashMap<>();
            for (String purpose : purposes) {
                results.put(purpose, gdprService.isProcessingLawful(customerId, purpose));
            }
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * GDPR Compliance Status
     * GET /api/gdpr/status
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getGDPRStatus() {
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("gdpr_compliant", true);
        status.put("privacy_policy_version", "1.0");
        status.put("last_updated", java.time.LocalDateTime.now());
        status.put("data_retention_period_months", 24);
        status.put("available_rights", java.util.Arrays.asList(
            "Right to Access (Article 15)",
            "Right to Rectification (Article 16)", 
            "Right to Erasure (Article 17)",
            "Right to Data Portability (Article 20)"
        ));
        
        return ResponseEntity.ok(status);
    }
} 