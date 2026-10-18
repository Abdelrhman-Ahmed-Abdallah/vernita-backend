
package com.example.vernita.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private Long id;
    private String fullName;
    private String email;
    private String message;

    private String accessToken;
    private String tokenType;
}
