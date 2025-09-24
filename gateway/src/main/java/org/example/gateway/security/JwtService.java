package org.example.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;

@Component
public class JwtService {
    private final String secret;
    private final String issuer;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.issuer}") String issuer) {
        this.secret = secret;
        this.issuer = issuer;
    }

    private SecretKey getKey() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Jws<Claims> validate(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .requireIssuer(issuer)
                .clockSkewSeconds(60)
                .build()
                .parseSignedClaims(token);
    }

    public static String stripBearer(String auth) {
        return (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
    }
}
