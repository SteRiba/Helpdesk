package com.project.helpdesk.dto.request;

import com.project.helpdesk.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TicketStatusUpdateRequest(
        @NotNull
        TicketStatus status
) {
}
