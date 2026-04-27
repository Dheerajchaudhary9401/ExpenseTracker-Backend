package com.spendsmart.auth.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {

    private String fullName;
    private String bio;
    private String avatarUrl;
    private String timezone;
}