package com.dentist.booking.dto;

import com.dentist.booking.entity.Role;
import lombok.Data;

@Data
public class AdminUserUpdateRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private Role role;
    private Boolean profileCompleted;
}
