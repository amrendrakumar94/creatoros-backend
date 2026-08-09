package com.creatoros.dto.team;

import java.util.Set;

import com.creatoros.enums.PermissionKey;
import com.creatoros.enums.Role;

import jakarta.validation.constraints.NotNull;

public record UpdateTeamMemberRequest(

        @NotNull(message = "Role is required") Role role,

        Set<PermissionKey> permissions,

        boolean active) {
}
