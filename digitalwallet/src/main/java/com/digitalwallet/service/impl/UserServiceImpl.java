package com.digitalwallet.service.impl;

import com.digitalwallet.dto.response.UserProfileResponse;
import com.digitalwallet.exception.ResourceNotFoundException;
import com.digitalwallet.repository.UserRepository;
import com.digitalwallet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        return userRepository.findById(userId)
            .map(UserProfileResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
