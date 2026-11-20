
package com.taskflow.service;

import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Invalid email or password"
                        )
                );

        // Google-only accounts do not have local passwords.
        // Generate an unusable dummy password so Spring Security
        // never receives a null password.
        String storedPassword = user.getPassword();

        if (storedPassword == null) {
            storedPassword = passwordEncoder.encode(
                    UUID.randomUUID().toString()
            );
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(storedPassword)
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}
