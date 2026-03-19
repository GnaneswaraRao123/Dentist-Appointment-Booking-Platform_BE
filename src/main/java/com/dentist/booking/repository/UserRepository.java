package com.dentist.booking.repository;

import com.dentist.booking.entity.User;
import com.dentist.booking.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    
    @Query("SELECT u FROM User u ORDER BY " +
           "CASE u.role WHEN 'ADMIN' THEN 1 WHEN 'DENTIST' THEN 2 WHEN 'CUSTOMER' THEN 3 END, u.id")
    List<User> findAllOrderByRoleThenId();
}
