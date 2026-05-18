package com.digitalwallet.service;

import com.digitalwallet.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(Long userId);

    // More user management methods (update, deactivate, etc.) can be added here in future phases
}
