package com.creatoros.entity;

import java.sql.Timestamp;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.creatoros.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "team_invitation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long      id;

    @Column(name = "creator_id", nullable = false)
    private Long      creatorId;

    @Column(nullable = false, length = 255)
    private String    email;

    @Column(name = "invite_token", nullable = false, unique = true, length = 100)
    private String    inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role      role;

    @Column(name = "expires_on", nullable = false)
    private LocalDate  expiresOn;

    @Column(name = "accepted_at")
    private Timestamp acceptedAt;

    @Column(name = "accepted_by_creator_id")
    private Long      acceptedByCreatorId;

    @Column(nullable = false)
    @Builder.Default
    private boolean   revoked = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
}
