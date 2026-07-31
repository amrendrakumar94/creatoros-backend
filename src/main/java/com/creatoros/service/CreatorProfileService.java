package com.creatoros.service;

import com.creatoros.dto.CreatorProfileDto;
import com.creatoros.dto.CurrentUserResponse;
import com.creatoros.dto.UpdateCreatorProfileRequest;

public interface CreatorProfileService {

    CreatorProfileDto getProfile(Long creatorId);

    CurrentUserResponse getCurrentUser(Long creatorId);

    /**
     * Applies a partial update - null fields on the request are left untouched.
     */
    CreatorProfileDto updateProfile(Long creatorId, UpdateCreatorProfileRequest request);
}
