package com.dentist.booking.repository;

import com.dentist.booking.entity.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DentistRepository extends JpaRepository<Dentist, Long> {
    Optional<Dentist> findByUserId(Long userId);
    
    @Query("SELECT d FROM Dentist d WHERE LOWER(d.clinicName) LIKE LOWER(CONCAT('%', :location, '%')) OR LOWER(d.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Dentist> findByLocation(@Param("location") String location);
    
    List<Dentist> findAllByOrderByIdDesc();
}
