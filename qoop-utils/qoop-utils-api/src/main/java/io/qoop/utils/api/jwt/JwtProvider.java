package io.qoop.utils.api.jwt;

import java.util.Map;

public interface JwtProvider {
    /**
     * Generate a JWT token.
     *
     * @param subject    The unique identifier (e.g., User ID)
     * @param claims     Additional data (e.g., roles, userId)
     * @param validityMs Expiration time in milliseconds
     * @return String JWT
     */
    String generateToken(String subject, Map<String, Object> claims, Long validityMs);

    /**
     * Parse and validate the token, returning all claims.
     * Throws DomainException if token is invalid or expired.
     *
     * @param token The JWT string
     * @return JwtClaims object containing token data
     */
    JwtClaims parseToken(String token);

    /**
     * Extract the Subject (e.g., User ID) from token.
     *
     * @param token The JWT string
     * @return String subject
     */
    String extractSubject(String token);

    /**
     * Extract a specific claim by key.
     *
     * @param token The JWT string
     * @param key   The claim key
     * @return Object claim value
     */
    Object extractClaim(String token, String key);

    /**
     * Check if token is expired.
     *
     * @param token The JWT string
     * @return boolean true if expired, false otherwise
     */
    boolean isTokenExpired(String token);
}