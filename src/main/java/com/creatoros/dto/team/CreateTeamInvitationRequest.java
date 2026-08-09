package com.creatoros.dto.team;

import com.creatoros.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTeamInvitationRequest(

        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address") @Size(max = 255) String email,

        @NotNull(message = "Role is required") Role role) {
}
