package com.example.hrms.repository;

import com.example.hrms.entity.Employee;
import com.example.hrms.entity.Role;
import com.example.hrms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE " +
           "(u.username = :login OR u.email = :login)")
    Optional<User> findByUsernameOrEmail(@Param("login") String login);
    
    Optional<User> findByEmployee(Employee employee);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:roleId IS NULL OR u.role.roleId = :roleId)")
    Page<User> searchUsers(@Param("search") String search, 
                           @Param("roleId") Integer roleId, 
                           Pageable pageable);
    
    Page<User> findByRole(Role role, Pageable pageable);
    
    Optional<User> findByResetToken(String resetToken);
    Optional<User> findByUsernameIgnoreCase(String username);
}
