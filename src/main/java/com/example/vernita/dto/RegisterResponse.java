
package com.example.vernita.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {

    private Long id;
    private String fullName;
    private String email;
    private String message;
}
