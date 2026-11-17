
package com.freightfox.assignment.security;

import com.freightfox.assignment.admin.entity.AdminUser;
import com.freightfox.assignment.admin.repository.AdminUserRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public CustomUserDetailsService(
            AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        AdminUser admin = adminUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Admin not found"
                        )
                );

        return User.withUsername(admin.getEmail())
                .password(admin.getPasswordHash())
                .authorities("ROLE_ADMIN")
                .disabled(!admin.isActive())
                .build();
    }
}
