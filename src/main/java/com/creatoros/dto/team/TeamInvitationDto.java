package com.creatoros.dto.team;

import java.sql.Timestamp;
import java.time.LocalDate;

import com.creatoros.enums.Role;

public record TeamInvitationDto(

        String id,

        String creatorId,

        String email,

        String inviteToken,

        Role role,

        LocalDate expiresOn,

        Timestamp acceptedAt,

        String acceptedByCreatorId,

        boolean revoked,

        Timestamp createdAt,

        Timestamp updatedAt) {
}
