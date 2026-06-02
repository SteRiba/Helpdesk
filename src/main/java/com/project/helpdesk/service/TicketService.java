package com.project.helpdesk.service;

import com.project.helpdesk.dto.request.TicketCreateRequest;
import com.project.helpdesk.dto.request.TicketUpdateRequest;
import com.project.helpdesk.dto.response.TicketResponse;
import com.project.helpdesk.entity.Ticket;
import com.project.helpdesk.entity.TicketStatus;
import com.project.helpdesk.entity.User;
import com.project.helpdesk.entity.UserRole;
import com.project.helpdesk.exception.ForbiddenActionException;
import com.project.helpdesk.repository.TicketRepository;
import com.project.helpdesk.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository, CurrentUserService currentUserService, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    public List<TicketResponse> findAll() {
        return ticketRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TicketResponse findById(Integer id) {
        validateId(id);

        Ticket existing = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + id));

        return toResponse(existing);
    }

    public List<TicketResponse> findAllByUserEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email can't be null");
        }

        User existing = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        List<Ticket> tickets = ticketRepository.findAllByAuthor_Email(email);

        return tickets.stream().map(this::toResponse).toList();
    }

    public TicketResponse create(TicketCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("TicketCreateRequest can't be null");
        }

        User author = currentUserService.getCurrentUser();

        Ticket ticket = toEntity(request);
        ticket.setAuthor(author);
        Ticket saved = ticketRepository.save(ticket);

        return toResponse(saved);
    }

    public TicketResponse update(TicketUpdateRequest request, Integer id) {
        if (request == null) {
            throw new IllegalArgumentException("TicketUpdateRequest can't be null");
        }

        validateId(id);

        User user = currentUserService.getCurrentUser();

        Ticket existing = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + id));

        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        boolean isAuthor = user.getId().equals(existing.getAuthor().getId());

        if (!isAdmin) {
            if (!isAuthor) {
                throw new ForbiddenActionException("You can't update this ticket");
            }

            if (existing.getStatus() != TicketStatus.OPEN) {
                throw new ForbiddenActionException("You can't update this ticket");
            }
        }

        if (request.title() != null) {
            existing.setTitle(request.title());
        }

        if (request.description() != null) {
            existing.setDescription(request.description());
        }

        if (request.priority() != null) {
            existing.setPriority(request.priority());
        }

        if (request.category() != null) {
            existing.setCategory(request.category());
        }

        Ticket updated = ticketRepository.save(existing);

        return toResponse(updated);
    }

    public TicketResponse assignToMe(Integer ticketId) {
        validateId(ticketId);

        User agent = currentUserService.getCurrentUser();

        if (agent.getRole() != UserRole.AGENT && agent.getRole() != UserRole.ADMIN) {
            throw new ForbiddenActionException("You don't have permission to complete this action");
        }

        Ticket existing = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        if (existing.getAgent() != null) {
            throw new ForbiddenActionException("Ticket is already assigned");
        }

        if (existing.getStatus() != TicketStatus.OPEN) {
            throw new ForbiddenActionException("Only open tickets can be assigned");
        }

        existing.setAgent(agent);
        existing.setStatus(TicketStatus.IN_PROGRESS);

        Ticket updated = ticketRepository.save(existing);

        return toResponse(updated);
    }

    public TicketResponse changeStatus(Integer id, TicketStatus status) {
        validateId(id);

        if (status == null) {
            throw new IllegalArgumentException("TicketStatus can't be null");
        }

        Ticket existing = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + id));

        User user = currentUserService.getCurrentUser();

        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        boolean isAssignedAgent = existing.getAgent() != null
                && existing.getAgent().getId().equals(user.getId());

        if (!isAdmin && !isAssignedAgent) {
            throw new ForbiddenActionException("You don't have permission to complete this action");
        }

        existing.setStatus(status);

        if (status == TicketStatus.CLOSED) {
            existing.setClosedAt(LocalDateTime.now());
        } else {
            existing.setClosedAt(null);
        }

        Ticket updated = ticketRepository.save(existing);

        return toResponse(updated);
    }


    //    Helpers
    private TicketResponse toResponse(Ticket ticket) {
        String authorFullName = ticket.getAuthor()
                .getFirstName()
                .concat(" ")
                .concat(ticket.getAuthor().getLastName());

        Integer agentId = ticket.getAgent() != null ? ticket.getAgent().getId() : null;
        String agentFullName = ticket.getAgent() != null
                ? ticket.getAgent().getFirstName().concat(" ").concat(ticket.getAgent().getLastName())
                : null;

        return new TicketResponse(ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt(),
                ticket.getCategory(),
                ticket.getAuthor().getId(),
                authorFullName,
                agentId,
                agentFullName);
    }

    private Ticket toEntity(TicketCreateRequest request) {
        return Ticket.builder().title(request.title()).description(request.description()).priority(request.priority()).category(request.category()).build();
    }

    private void validateId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID can't be null");
        }
    }
}
