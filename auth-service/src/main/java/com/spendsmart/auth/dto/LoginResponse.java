package com.spendsmart.auth.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private int userId;
    private String fullName;
    private String email;
    private String currency;
    private String role;
}