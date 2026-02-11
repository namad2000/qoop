package io.qoop.utils.core.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.utils.api.jwt.JwtClaims;
import io.qoop.utils.api.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static io.qoop.fault.handler.api.presentation.HttpStatus.UNAUTHORIZED;
import static io.qoop.utils.api.jwt.JwtExceptionCode.INVALID_TOKEN;
import static io.qoop.utils.api.jwt.JwtExceptionCode.TOKEN_EXPIRED;

@Component
public class JwtProviderAdapter implements JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public String generateToken(String subject, Map<String, Object> claims, Long validityMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public JwtClaims parseToken(String token) {
        try {
            Claims standardClaims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return JwtClaims.builder()
                    .subject(standardClaims.getSubject())
                    .expiration(standardClaims.getExpiration())
                    .issuedAt(standardClaims.getIssuedAt())
                    .additionalInfo(standardClaims)
                    .build();
        } catch (ExpiredJwtException ex) {
            throw DomainException.of(TOKEN_EXPIRED, UNAUTHORIZED);
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SecurityException ex) {
            throw DomainException.of(INVALID_TOKEN, UNAUTHORIZED);
        }
    }

    @Override
    public String extractSubject(String token) {
        return parseToken(token).getSubject();
    }

    @Override
    public Object extractClaim(String token, String key) {
        return parseToken(token).getAdditionalInfo().get(key);
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (DomainException e) {
            return true;
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}