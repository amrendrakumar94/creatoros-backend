package com.creatoros.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatoros.dto.CreatorProfileDto;
import com.creatoros.dto.CurrentUserResponse;
import com.creatoros.dto.UpdateCreatorProfileRequest;
import com.creatoros.security.SecurityUtils;
import com.creatoros.service.CreatorProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class ProfileController {

    private final CreatorProfileService creatorProfileService;

    @GetMapping
    public ResponseEntity<CurrentUserResponse> currentUser() {
        return ResponseEntity.ok(creatorProfileService.getCurrentUser(SecurityUtils.currentCreatorId()));
    }

    @GetMapping("/profile")
    public ResponseEntity<CreatorProfileDto> getProfile() {
        return ResponseEntity.ok(creatorProfileService.getProfile(SecurityUtils.currentCreatorId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<CreatorProfileDto> updateProfile(@Valid @RequestBody UpdateCreatorProfileRequest request) {
        return ResponseEntity.ok(creatorProfileService.updateProfile(SecurityUtils.currentCreatorId(), request));
    }
}
