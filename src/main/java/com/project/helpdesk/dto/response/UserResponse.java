package com.project.helpdesk.dto.response;

import com.project.helpdesk.entity.UserRole;
import com.project.helpdesk.entity.UserStatus;

import java.time.LocalDateTime;

public record UserResponse(
        Integer id,
        String fullName,
        String email,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt
) {
}
