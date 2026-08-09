package com.creatoros.dao;

import java.util.List;
import java.util.Optional;

import com.creatoros.entity.TeamInvitation;

public interface TeamInvitationDao {

    TeamInvitation save(TeamInvitation invitation);

    Optional<TeamInvitation> findByInviteToken(String inviteToken);

    Optional<TeamInvitation> findByCreatorIdAndEmailIgnoreCaseAndRevokedFalse(Long creatorId, String email);

    Optional<TeamInvitation> findFirstByEmailIgnoreCaseAndRevokedFalseAndAcceptedAtIsNull(String email);

    Optional<TeamInvitation> findByIdAndCreatorId(Long id, Long creatorId);

    /** Only invitations that are still actionable - not revoked, not accepted, not past their expiry. */
    List<TeamInvitation> findPendingByCreatorIdOrderByCreatedAtDesc(Long creatorId);
}
