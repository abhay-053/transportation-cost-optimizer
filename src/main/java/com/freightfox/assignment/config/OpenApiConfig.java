
package com.freightfox.assignment.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FreightFox Transporter Assignment API",
                version = "1.0.0",
                description = """
                        API for managing transportation lanes, transporter quotes,
                        and cost-optimized transporter assignments.

                        **Core capabilities**
                        - Secure admin registration and JWT authentication.
                        - Upload and manage lane and transporter datasets.
                        - Optimize transporter selection and lane assignments.
                        - Retrieve assignment results and historical runs.

                        **Optimization objectives**
                        1. Maximize the number of distinct transporters used,
                           subject to the configured transporter limit.
                        2. Minimize the total assignment cost while covering
                           every lane.

                        **Typical workflow**
                        1. Register the initial admin account.
                        2. Log in to obtain a JWT access token.
                        3. Authorize Swagger using the JWT.
                        4. Upload lane and transporter quote data.
                        5. Submit an assignment optimization request.
                        6. Retrieve assignment results and history.

                        Protected endpoints require a valid JWT Bearer token.
                        """,
                contact = @Contact(
                        name = "FreightFox Transporter Assignment"
                ),
                license = @License(
                        name = "Proprietary"
                )
        ),
        security = {
                @SecurityRequirement(name = "Bearer Authentication")
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
