package com.project.helpdesk.dto.request;

import com.project.helpdesk.entity.TicketCategory;
import com.project.helpdesk.entity.TicketPriority;
import com.project.helpdesk.entity.TicketStatus;
import jakarta.validation.constraints.Size;

public record TicketUpdateRequest(
        @Size(max = 100)
        String title,
        @Size(max = 1000)
        String description,
        TicketStatus status,
        TicketPriority priority,
        TicketCategory category
) {
}
