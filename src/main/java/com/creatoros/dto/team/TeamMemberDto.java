package com.creatoros.dto.team;

import java.sql.Timestamp;
import java.util.Set;

import com.creatoros.enums.PermissionKey;
import com.creatoros.enums.Role;

public record TeamMemberDto(

        String id,

        String creatorId,

        String memberCreatorId,

        String email,

        String name,

        Role role,

        boolean active,

        Set<PermissionKey> permissions,

        Timestamp createdAt,

        Timestamp updatedAt) {
}
