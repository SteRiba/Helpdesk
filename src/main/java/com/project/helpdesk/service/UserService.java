package com.project.helpdesk.service;

import com.project.helpdesk.dto.request.AdminUserCreateRequest;
import com.project.helpdesk.dto.request.ChangePasswordRequest;
import com.project.helpdesk.dto.request.UserCreateRequest;
import com.project.helpdesk.dto.request.UserUpdateRequest;
import com.project.helpdesk.dto.response.UserResponse;
import com.project.helpdesk.entity.User;
import com.project.helpdesk.entity.UserRole;
import com.project.helpdesk.entity.UserStatus;
import com.project.helpdesk.exception.DuplicatedResourceException;
import com.project.helpdesk.exception.ForbiddenActionException;
import com.project.helpdesk.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, ActivityLogService activityLogService, CurrentUserService currentUserService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse findById(Integer id) {
        validateId(id);

        User existing = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return toResponse(existing);
    }

    public UserResponse findCurrentUserProfile() {
        User current = currentUserService.getCurrentUser();
        return toResponse(current);
    }

    public UserResponse create(UserCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("UserCreateRequest can't be null");
        }

        String email = request.email().toLowerCase().trim();

        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            throw new DuplicatedResourceException("Email already in use");
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords don't match");
        }

        User user = toEntity(request, email);

        User saved = userRepository.save(user);

        return toResponse(saved);
    }

    public UserResponse createByAdmin(AdminUserCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AdminUserCreateRequest can't be null");
        }

        String email = request.email().toLowerCase().trim();

        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            throw new DuplicatedResourceException("Email already in use");
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords don't match");
        }

        User author = currentUserService.getCurrentUser();

        if (author.getRole() != UserRole.ADMIN) {
            throw new ForbiddenActionException("You don't have permission to complete this action");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(email)
                .role(request.role() == null ? UserRole.CUSTOMER : request.role())
                .status(request.status() == null ? UserStatus.ACTIVE : request.status())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        User saved = userRepository.save(user);

        activityLogService.logUserCreated(saved, author);

        return toResponse(saved);
    }

    public UserResponse updateProfile(UserUpdateRequest request) {
        validateUpdateRequest(request);

        User currentUser = currentUserService.getCurrentUser();

        User existing = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + currentUser.getId()));

        if (request.firstName() != null) {
            existing.setFirstName(request.firstName());
        }

        if (request.lastName() != null) {
            existing.setLastName(request.lastName());
        }

        if (request.email() != null) {
            String emailRequest = request.email().toLowerCase().trim();

            Optional<User> optional = userRepository.findByEmail(emailRequest);

            if (optional.isPresent() && !optional.get().getId().equals(existing.getId())) {
                throw new DuplicatedResourceException("Email already in use");
            }

            existing.setEmail(emailRequest);
        }

        User updated = userRepository.save(existing);

        return toResponse(updated);
    }

    public UserResponse updatePassword(ChangePasswordRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ChangePasswordRequest can't be null");
        }

        User current = currentUserService.getCurrentUser();

        if (!passwordEncoder.matches(request.currentPassword(), current.getPasswordHash())) {
            throw new ForbiddenActionException("Current password is not correct");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords don't match");
        }

        current.setPasswordHash(passwordEncoder.encode(request.newPassword()));

        User updated = userRepository.save(current);

        return toResponse(updated);
    }

    public UserResponse activateUser(Integer id) {
        validateId(id);

        User current = currentUserService.getCurrentUser();

        if (current.getRole() != UserRole.ADMIN) {
            throw new ForbiddenActionException("You don't have permission to complete this action");
        }

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        if (existing.getStatus() == UserStatus.ACTIVE) {
            return toResponse(existing);
        }

        existing.setStatus(UserStatus.ACTIVE);

        User activated = userRepository.save(existing);

        activityLogService.logUserActivated(activated, current);

        return toResponse(activated);
    }

    public UserResponse disableUser(Integer id) {
        validateId(id);

        User current = currentUserService.getCurrentUser();

        if (current.getRole() != UserRole.ADMIN) {
            throw new ForbiddenActionException("You don't have permission to complete this action");
        }

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        if (current.getId().equals(existing.getId())) {
            throw new ForbiddenActionException("You can't disable yourself");
        }

        if (existing.getStatus() == UserStatus.DISABLED) {
            return toResponse(existing);
        }

        existing.setStatus(UserStatus.DISABLED);

        User disabled = userRepository.save(existing);

        activityLogService.logUserDisabled(disabled, current);

        return toResponse(disabled);
    }

    //    Helpers
    private User toEntity(UserCreateRequest request, String email) {
        return User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
    }

    private UserResponse toResponse(User user) {
        String fullName = user.getFirstName().concat(" ").concat(user.getLastName());
        return new UserResponse(user.getId(), fullName, user.getEmail(), user.getRole(), user.getStatus(), user.getCreatedAt());
    }

    private void validateId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID can't be null");
        }
    }

    private void validateUpdateRequest(UserUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("UserUpdateRequest can't be null");
        }
    }

}
