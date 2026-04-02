package com.accountia.auth.repository;

import com.accountia.auth.model.RefreshToken;
import com.accountia.auth.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<RefreshToken> findByToken(String token);
    
    int deleteByUser(User user);
    
    int deleteByToken(String token);
}
