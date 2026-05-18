package com.digitalwallet.service.impl;

import com.digitalwallet.domain.entity.Role;
import com.digitalwallet.domain.entity.User;
import com.digitalwallet.domain.entity.Wallet;
import com.digitalwallet.domain.enums.UserStatus;
import com.digitalwallet.domain.enums.WalletStatus;
import com.digitalwallet.dto.request.LoginRequest;
import com.digitalwallet.dto.request.RegisterRequest;
import com.digitalwallet.dto.response.AuthResponse;
import com.digitalwallet.exception.DuplicateResourceException;
import com.digitalwallet.exception.ResourceNotFoundException;
import com.digitalwallet.repository.RoleRepository;
import com.digitalwallet.repository.UserRepository;
import com.digitalwallet.security.JwtTokenProvider;
import com.digitalwallet.security.UserPrincipal;
import com.digitalwallet.service.AuthService;
import com.digitalwallet.util.WalletNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository       userRepository;
    private final RoleRepository       roleRepository;
    private final PasswordEncoder      passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider     tokenProvider;
    private final WalletNumberGenerator walletNumberGenerator;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check for duplicates before doing any writes
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("An account with this phone number already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));

        // Build and persist the user
        User user = User.builder()
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
            .phoneNumber(request.phoneNumber())
            .passwordHash(passwordEncoder.encode(request.password()))
            .status(UserStatus.ACTIVE)  // skip email verification for now; add in a future phase
            .build();

        user.addRole(userRole);

        // Every new user gets a default USD wallet on signup
        Wallet defaultWallet = Wallet.builder()
            .walletNumber(walletNumberGenerator.generate())
            .balance(BigDecimal.ZERO)
            .currency("USD")
            .status(WalletStatus.ACTIVE)
            .build();

        user.addWallet(defaultWallet);
        User savedUser = userRepository.save(user);

        log.info("New user registered: {} (id={})", savedUser.getEmail(), savedUser.getId());

        // Log them in straight away — no need for a separate login call after register
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        return buildAuthResponse(auth, savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // AuthenticationManager throws BadCredentialsException on failure,
        // which our GlobalExceptionHandler converts to a 401
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User user = userRepository.findByEmailWithRoles(principal.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", principal.getEmail()));

        return buildAuthResponse(auth, user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new SecurityException("Refresh token is invalid or expired");
        }

        String email = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmailWithRoles(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        UserPrincipal principal = UserPrincipal.from(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal, null, principal.getAuthorities()
        );

        return buildAuthResponse(auth, user);
    }

    // Shared helper — builds the AuthResponse from an authenticated principal + user entity
    private AuthResponse buildAuthResponse(Authentication auth, User user) {
        String accessToken  = tokenProvider.generateAccessToken(auth);
        String refreshToken = tokenProvider.generateRefreshToken(auth);

        Set<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

        AuthResponse.UserSummary summary = new AuthResponse.UserSummary(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            roleNames,
            user.getCreatedAt()
        );

        return AuthResponse.of(accessToken, refreshToken, jwtExpirationMs, summary);
    }
}
