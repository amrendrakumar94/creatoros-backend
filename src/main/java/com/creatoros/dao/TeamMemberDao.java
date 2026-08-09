package com.creatoros.dao;

import java.util.List;
import java.util.Optional;

import com.creatoros.entity.TeamMember;

public interface TeamMemberDao {

    TeamMember save(TeamMember member);

    Optional<TeamMember> findByIdAndCreatorId(Long id, Long creatorId);

    Optional<TeamMember> findByCreatorIdAndMemberCreatorId(Long creatorId, Long memberCreatorId);

    Optional<TeamMember> findByMemberCreatorIdAndActiveTrue(Long memberCreatorId);

    List<TeamMember> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
}
