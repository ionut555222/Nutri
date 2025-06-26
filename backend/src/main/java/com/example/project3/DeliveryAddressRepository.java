package com.example.project3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    
    // Find addresses by customer
    List<DeliveryAddress> findByCustomerIdOrderByIsDefaultDescCreatedAtAsc(Long customerId);
    
    // Find customer's default address
    Optional<DeliveryAddress> findByCustomerIdAndIsDefaultTrue(Long customerId);
    
    // Find addresses by location
    List<DeliveryAddress> findByZipCodeAndCityIgnoreCase(String zipCode, String city);
    
    List<DeliveryAddress> findByCityIgnoreCaseOrderByZipCode(String city);
    
    List<DeliveryAddress> findByZipCodeOrderByCreatedAtDesc(String zipCode);
    
    // Find addresses that need geocoding
    List<DeliveryAddress> findByIsGeocodedFalseAndIsValidatedTrue();
    
    List<DeliveryAddress> findByIsValidatedFalse();
    
    // Find addresses within a geographic area (requires coordinates)
    @Query("SELECT da FROM DeliveryAddress da WHERE da.isValidated = true AND " +
           "da.latitude BETWEEN :minLat AND :maxLat AND " +
           "da.longitude BETWEEN :minLon AND :maxLon")
    List<DeliveryAddress> findAddressesInBounds(@Param("minLat") Double minLatitude,
                                                @Param("maxLat") Double maxLatitude,
                                                @Param("minLon") Double minLongitude,
                                                @Param("maxLon") Double maxLongitude);
    
    // Find addresses by delivery characteristics
    List<DeliveryAddress> findByIsResidentialAndDeliveryDifficultyScoreLessThanEqual(Boolean isResidential, Integer maxDifficulty);
    
    List<DeliveryAddress> findByRequiresSignatureTrue();
    
    List<DeliveryAddress> findByFloorNumberGreaterThanAndHasElevatorFalse(Integer floorNumber);
    
    // Customer address management
    @Query("SELECT COUNT(da) FROM DeliveryAddress da WHERE da.customer.id = :customerId")
    Long countAddressesByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT da FROM DeliveryAddress da WHERE da.customer.id = :customerId AND " +
           "(LOWER(da.addressNickname) LIKE LOWER(CONCAT('%', :nickname, '%')) OR " +
           "LOWER(da.streetAddress) LIKE LOWER(CONCAT('%', :address, '%')))")
    List<DeliveryAddress> findCustomerAddressByNicknameOrAddress(@Param("customerId") Long customerId,
                                                                 @Param("nickname") String nickname,
                                                                 @Param("address") String address);
    
    // Analytics and statistics
    @Query("SELECT da.city, COUNT(da) as addressCount FROM DeliveryAddress da " +
           "WHERE da.isValidated = true " +
           "GROUP BY da.city " +
           "ORDER BY addressCount DESC")
    List<Object[]> getCityStatistics();
    
    @Query("SELECT da.zipCode, COUNT(da) as addressCount FROM DeliveryAddress da " +
           "WHERE da.isValidated = true " +
           "GROUP BY da.zipCode " +
           "ORDER BY addressCount DESC")
    List<Object[]> getZipCodeStatistics();
    
    @Query("SELECT AVG(da.deliveryDifficultyScore) FROM DeliveryAddress da WHERE da.isValidated = true")
    Double getAverageDeliveryDifficulty();
    
    @Query("SELECT da.isResidential, COUNT(da) FROM DeliveryAddress da " +
           "WHERE da.isValidated = true " +
           "GROUP BY da.isResidential")
    List<Object[]> getResidentialVsCommercialStats();
    
    // Data quality queries
    @Query("SELECT COUNT(da) FROM DeliveryAddress da WHERE da.latitude IS NULL OR da.longitude IS NULL")
    Long countAddressesWithoutCoordinates();
    
    @Query("SELECT COUNT(da) FROM DeliveryAddress da WHERE da.isValidated = false")
    Long countUnvalidatedAddresses();
    
    // Find duplicate addresses
    @Query("SELECT da.streetAddress, da.city, da.zipCode, COUNT(da) as duplicateCount " +
           "FROM DeliveryAddress da " +
           "GROUP BY da.streetAddress, da.city, da.zipCode " +
           "HAVING COUNT(da) > 1 " +
           "ORDER BY duplicateCount DESC")
    List<Object[]> findPotentialDuplicateAddresses();
} 