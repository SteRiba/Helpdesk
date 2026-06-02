package com.project.helpdesk.dto.request;

import jakarta.validation.constraints.Email;

public record UserUpdateRequest(
        String firstName,
        String lastName,

        @Email
        String email
) {
}
