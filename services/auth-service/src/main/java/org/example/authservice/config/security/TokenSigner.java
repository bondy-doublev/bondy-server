package org.example.authservice.config.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.authservice.entity.User;
import org.example.authservice.property.PropsConfig;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenSigner {
    String secret;
    String issuer;
    long accessTtlSec;

    public TokenSigner(PropsConfig props) {
        this.secret = props.getJwt().getSecret();
        this.issuer = props.getJwt().getIssuer();
        this.accessTtlSec = props.getJwt().getAccessTtlSec();
    }

    private SecretKey getKey() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .claims()
                .add("role", user.getRole())
                .add("email", user.getEmail())
                .subject(String.valueOf(user.getId()))
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSec)))
                .and()
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();
    }
}
