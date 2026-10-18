package com.example.vernita.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String specialization;
    private String careerTarget;
}