package com.taskflow.repository;

import com.taskflow.entity.AuthProvider;
import com.taskflow.entity.User;
import com.taskflow.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    Optional<User> findByProviderSubject(String providerSubject);

    Optional<User> findByAuthProviderAndProviderSubject(
            AuthProvider authProvider,
            String providerSubject
    );
    long countByRole(UserRole role);


}