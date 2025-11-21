package com.example.hrms.repository;

import com.example.hrms.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleKey(String roleKey);
    
    @Query("SELECT r FROM Role r WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(r.roleKey) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.roleName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Role> searchRoles(@Param("search") String search, Pageable pageable);
}
