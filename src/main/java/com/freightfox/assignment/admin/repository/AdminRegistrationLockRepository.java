package com.freightfox.assignment.admin.repository;

import com.freightfox.assignment.admin.entity.AdminRegistrationLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRegistrationLockRepository
        extends JpaRepository<AdminRegistrationLock, Integer> {

    @Query(value = """
            SELECT id
            FROM admin_registration_lock
            WHERE id = 1
            FOR UPDATE
            """, nativeQuery = true)
    Integer lockRegistration();
}