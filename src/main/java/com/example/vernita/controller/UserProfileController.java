package com.example.vernita.controller;

import com.example.vernita.dto.UserProfileResponse;
import com.example.vernita.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.example.vernita.dto.UserProfileUpdateRequest;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserProfileResponse response =
                userProfileService.getCurrentUserProfile(
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(response);
    }
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request) {

        UserProfileResponse response =
                userProfileService.updateCurrentUserProfile(
                        userDetails.getUsername(),
                        request
                );

        return ResponseEntity.ok(response);
    }
}