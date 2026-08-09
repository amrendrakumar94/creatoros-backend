package com.creatoros.entity;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.creatoros.enums.PermissionKey;
import com.creatoros.enums.Role;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "team_member")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long              id;

    @Column(name = "creator_id", nullable = false)
    private Long              creatorId;

    @Column(name = "member_creator_id", nullable = false)
    private Long              memberCreatorId;

    @Column(nullable = false, length = 255)
    private String            email;

    @Column(nullable = false, length = 150)
    private String            name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role              role;

    @Column(nullable = false)
    @Builder.Default
    private boolean           active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "team_member_permission", joinColumns = @JoinColumn(name = "team_member_id"))
    @Column(name = "permission", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<PermissionKey> permissions = new LinkedHashSet<>();

    @Column(name = "invited_by_creator_id", nullable = false)
    private Long              invitedByCreatorId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Timestamp         createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Timestamp         updatedAt;
}
