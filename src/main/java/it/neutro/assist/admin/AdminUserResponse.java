package it.neutro.assist.admin;

import java.util.List;

public record AdminUserResponse(
        Long id,
        String fullName,
        String email,
        List<String> roles,
        String status,
        String createdAt,
        String updatedAt
) {
}
