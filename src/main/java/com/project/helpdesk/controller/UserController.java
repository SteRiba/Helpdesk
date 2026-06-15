package com.project.helpdesk.controller;

import com.project.helpdesk.dto.request.AdminUserCreateRequest;
import com.project.helpdesk.dto.request.ChangePasswordRequest;
import com.project.helpdesk.dto.request.UserUpdateRequest;
import com.project.helpdesk.dto.response.UserResponse;
import com.project.helpdesk.service.UserService;
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
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        UserResponse response = userService.findCurrentUserProfile();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/me/change-password")
    public ResponseEntity<UserResponse> changeMyPassword(@RequestBody @Valid ChangePasswordRequest request) {
        UserResponse response = userService.updatePassword(request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/me/update-profile")
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody @Valid UserUpdateRequest request) {
        UserResponse response = userService.updateProfile(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> response = userService.findAll();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/users")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid AdminUserCreateRequest request) {
        UserResponse response = userService.createByAdmin(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/users/{id}/disable")
    public ResponseEntity<UserResponse> disableUser(@PathVariable @Positive Integer id) {
        UserResponse response = userService.disableUser(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/users/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable @Positive Integer id) {
        UserResponse response = userService.activateUser(id);

        return ResponseEntity.ok(response);
    }

}
