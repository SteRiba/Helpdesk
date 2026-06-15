package com.project.helpdesk.controller;

import com.project.helpdesk.dto.request.TicketCreateRequest;
import com.project.helpdesk.dto.request.TicketStatusUpdateRequest;
import com.project.helpdesk.dto.response.TicketResponse;
import com.project.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketResponse>> findAllMyTickets() {
        List<TicketResponse> response = ticketService.findAllMyTickets();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/tickets")
    public ResponseEntity<List<TicketResponse>> findAllTickets() {
        List<TicketResponse> tickets = ticketService.findAll();

        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/tickets")
    public ResponseEntity<TicketResponse> openNewTicket(@RequestBody @Valid TicketCreateRequest request) {
        TicketResponse response = ticketService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/agent/tickets/{ticketId}/assign-to-me")
    public ResponseEntity<TicketResponse> assignToMe(@PathVariable @Positive Integer ticketId) {
        TicketResponse response = ticketService.assignToMe(ticketId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/agent/tickets/{ticketId}/change-status")
    public ResponseEntity<TicketResponse> changeStatus(@PathVariable @Positive Integer ticketId,
                                                       @RequestBody @Valid TicketStatusUpdateRequest request) {
        TicketResponse response = ticketService.changeStatus(ticketId, request);

        return ResponseEntity.ok(response);
    }

    // TODO
    // add methods to update category and priority
}
