
package com.taskflow.repository;

import com.taskflow.entity.OAuthLoginCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OAuthLoginCodeRepository
        extends JpaRepository<OAuthLoginCode, Long> {

    Optional<OAuthLoginCode> findByCodeHash(String codeHash);

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            UPDATE OAuthLoginCode c
            SET c.consumedAt = :now
            WHERE c.codeHash = :codeHash
            AND c.consumedAt IS NULL
            AND c.expiresAt > :now
            """)
    int consumeCode(
            @Param("codeHash") String codeHash,
            @Param("now") LocalDateTime now
    );
}
