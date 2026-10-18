package com.example.vernita.service;

import com.example.vernita.dto.UserProfileResponse;
import com.example.vernita.entity.User;
import com.example.vernita.entity.UserProfile;
import com.example.vernita.repository.UserProfileRepository;
import com.example.vernita.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.vernita.dto.UserProfileUpdateRequest;


@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        UserProfile profile = userProfileRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User profile not found"));

        return UserProfileResponse.builder()
                .userId(user.getId())
                .fullName(profile.getFullName())
                .email(user.getEmail())
                .phone(profile.getPhone())
                .avatarUrl(profile.getAvatarUrl())
                .specialization(profile.getSpecialization())
                .careerTarget(profile.getCareerTarget())
                .build();
    }
    @Transactional
    public UserProfileResponse updateCurrentUserProfile(
            String email,
            UserProfileUpdateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        UserProfile profile = userProfileRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User profile not found"));

        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setSpecialization(request.getSpecialization());
        profile.setCareerTarget(request.getCareerTarget());

        UserProfile savedProfile = userProfileRepository.save(profile);

        return UserProfileResponse.builder()
                .userId(user.getId())
                .fullName(savedProfile.getFullName())
                .email(user.getEmail())
                .phone(savedProfile.getPhone())
                .avatarUrl(savedProfile.getAvatarUrl())
                .specialization(savedProfile.getSpecialization())
                .careerTarget(savedProfile.getCareerTarget())
                .build();
    }
}