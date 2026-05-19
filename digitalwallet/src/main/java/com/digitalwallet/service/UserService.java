package com.digitalwallet.service;

import com.digitalwallet.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(Long userId);
}
