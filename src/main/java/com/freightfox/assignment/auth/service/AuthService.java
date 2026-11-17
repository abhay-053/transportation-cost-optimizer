package com.freightfox.assignment.auth.service;

import com.freightfox.assignment.admin.entity.AdminUser;
import com.freightfox.assignment.admin.repository.AdminUserRepository;
import com.freightfox.assignment.admin.repository.AdminRegistrationLockRepository;
import com.freightfox.assignment.auth.dto.AdminProfileResponse;
import com.freightfox.assignment.auth.dto.LoginRequest;
import com.freightfox.assignment.auth.dto.LoginResponse;
import com.freightfox.assignment.auth.dto.RegisterRequest;
import com.freightfox.assignment.exception.EmailAlreadyRegisteredException;
import com.freightfox.assignment.exception.InvalidBootstrapSecretException;
import com.freightfox.assignment.exception.RegistrationClosedException;
import com.freightfox.assignment.exception.ResourceNotFoundException;
import com.freightfox.assignment.security.CustomUserDetailsService;
import com.freightfox.assignment.security.JwtTokenProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

@Service
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final AdminRegistrationLockRepository registrationLockRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final String bootstrapSecret;
    private final long jwtExpirationMs;

    public AuthService(
            AdminUserRepository adminUserRepository,
            AdminRegistrationLockRepository registrationLockRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtTokenProvider jwtTokenProvider,
            @Value("${security.bootstrap-secret}") String bootstrapSecret,
            @Value("${security.jwt.expiration-ms}") long jwtExpirationMs) {

        this.adminUserRepository = adminUserRepository;
        this.registrationLockRepository = registrationLockRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.bootstrapSecret = bootstrapSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @Transactional
    public void registerAdmin(RegisterRequest request, String providedSecret) {

        if (providedSecret == null || !MessageDigest.isEqual(
                bootstrapSecret.getBytes(StandardCharsets.UTF_8),
                providedSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidBootstrapSecretException();
        }

        // Lock the registration row until this transaction completes.
        registrationLockRepository.lockRegistration();

        if (adminUserRepository.count() > 0) {
            throw new RegistrationClosedException();
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (adminUserRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        AdminUser admin = new AdminUser();
        admin.setName(request.name().trim());
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        admin.setActive(true);

        adminUserRepository.save(admin);
    }

    public LoginResponse login(LoginRequest request) {

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                )
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        String token = jwtTokenProvider.generateToken(userDetails);

        return new LoginResponse(
                token,
                "Bearer",
                jwtExpirationMs / 1000
        );
    }

    @Transactional(readOnly = true)
    public AdminProfileResponse getCurrentAdmin(String email) {

        AdminUser admin = adminUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Admin not found"));

        return new AdminProfileResponse(
                admin.getId(),
                admin.getName(),
                admin.getEmail()
        );
    }
}