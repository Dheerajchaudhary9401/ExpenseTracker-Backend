package com.spendsmart.auth.dto;

import lombok.Data;

@Data
public class GoogleAuthRequest {
    private String idToken; // Google ID token sent from frontend
}