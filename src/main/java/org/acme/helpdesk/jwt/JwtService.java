package org.acme.helpdesk.jwt;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.helpdesk.entity.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class JwtService {

    private static final Duration TOKEN_VALIDITY = Duration.ofHours(24);

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    private String issuer;

    public String generateToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.username)
                .groups(Set.of(user.role.name()))
                .claim("userId", user.id)
                .expiresIn(Duration.ofHours(24))
                .sign();
    }

    public long getTokenValiditySeconds() {
        return TOKEN_VALIDITY.toSeconds();
    }
}