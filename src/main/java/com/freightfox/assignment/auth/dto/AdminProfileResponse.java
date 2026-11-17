package com.freightfox.assignment.auth.dto;

public record AdminProfileResponse(
        Long id,
        String name,
        String email
) {
}