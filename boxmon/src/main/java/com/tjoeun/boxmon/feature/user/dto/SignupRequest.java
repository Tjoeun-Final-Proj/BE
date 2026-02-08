package com.tjoeun.boxmon.feature.user.dto;

import com.tjoeun.boxmon.feature.user.domain.UserType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class SignupRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @NotNull
    private LocalDate birth;

    @NotNull
    private UserType userType;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public LocalDate getBirth() { return birth; }
    public UserType getUserType() { return userType; }


}
