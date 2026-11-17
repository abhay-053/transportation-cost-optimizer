
package com.freightfox.assignment.auth.controller;

import com.freightfox.assignment.auth.dto.AdminProfileResponse;
import com.freightfox.assignment.auth.dto.LoginRequest;
import com.freightfox.assignment.auth.dto.LoginResponse;
import com.freightfox.assignment.auth.dto.RegisterRequest;
import com.freightfox.assignment.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Authentication",
        description = "Initial admin registration, login, and profile APIs"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Register the initial admin",
            description = """
                    Registers the first admin account for the application.

                    This endpoint requires the X-Admin-Bootstrap-Secret header.
                    Registration is allowed only once; after the initial admin
                    is created, further registrations are closed.

                    This endpoint does not require a JWT.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Initial admin registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration details"),
            @ApiResponse(responseCode = "401", description = "Invalid bootstrap secret"),
            @ApiResponse(responseCode = "403", description = "Admin registration is already closed"),
            @ApiResponse(responseCode = "409", description = "Email address is already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request,
            @Parameter(
                    description = "Bootstrap secret configured by the application administrator",
                    required = true
            )
            @RequestHeader("X-Admin-Bootstrap-Secret") String bootstrapSecret) {

        authService.registerAdmin(request, bootstrapSecret);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Admin login",
            description = """
                    Authenticates an admin using their registered email and password.

                    Returns a JWT access token that must be supplied as a
                    Bearer token when calling protected endpoints.

                    This endpoint does not require a JWT.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful; JWT returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request format"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Get current admin profile",
            description = """
                    Returns the profile of the admin associated with
                    the currently authenticated JWT.

                    Requires a valid Bearer token.
                    """,
            security = {
                    @SecurityRequirement(name = "Bearer Authentication")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired JWT")
    })
    @GetMapping("/me")
    public ResponseEntity<AdminProfileResponse> getCurrentAdmin(
            Authentication authentication) {

        return ResponseEntity.ok(
                authService.getCurrentAdmin(authentication.getName())
        );
    }
}
