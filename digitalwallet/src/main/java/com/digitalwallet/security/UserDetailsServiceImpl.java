package com.digitalwallet.security;

import com.digitalwallet.domain.entity.User;
import com.digitalwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security calls this during form-login and token validation to load
 * the user by their identifier (email in our case).
 *
 * The @Transactional here is important — User.roles is EAGER but we still
 * want a clean session boundary when this gets called from the JWT filter.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "No user found with email: " + email
            ));
        return UserPrincipal.from(user);
    }
}
