package io.qoop.utils.core.jwt;

import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.utils.api.jwt.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderAdapterTest {

    @InjectMocks
    private JwtProviderAdapter jwtProviderAdapter;

    private final String secret = "ThisIsAVeryStrongSecretKeyForTestingPurpose12345678";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jwtProviderAdapter, "secret", secret);
    }

    @Test
    void generateToken_ValidInput_ReturnsToken() {
        String subject = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        Long validityMs = 3600000L;

        String token = jwtProviderAdapter.generateToken(subject, claims, validityMs);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void parseToken_ValidToken_ReturnsJwtClaims() {
        String subject = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        String token = jwtProviderAdapter.generateToken(subject, claims, 3600000L);

        // تغییر: نوع خروجی به JwtClaims
        JwtClaims parsedClaims = jwtProviderAdapter.parseToken(token);

        assertNotNull(parsedClaims);
        assertEquals(subject, parsedClaims.getSubject());
        // تغییر: دسترسی به نقشه اضافی برای گرفتن role
        assertEquals("ADMIN", parsedClaims.getAdditionalInfo().get("role"));
    }

    @Test
    void extractSubject_ValidToken_ReturnsCorrectSubject() {
        String subject = "user123";
        String token = jwtProviderAdapter.generateToken(subject, new HashMap<>(), 3600000L);

        String extractedSubject = jwtProviderAdapter.extractSubject(token);

        assertEquals(subject, extractedSubject);
    }

    @Test
    void extractClaim_ValidToken_ReturnsCorrectClaim() {
        String key = "role";
        String value = "USER";
        Map<String, Object> claims = new HashMap<>();
        claims.put(key, value);
        String token = jwtProviderAdapter.generateToken("user", claims, 3600000L);

        Object extractedClaim = jwtProviderAdapter.extractClaim(token, key);

        assertEquals(value, extractedClaim);
    }

    @Test
    void isTokenExpired_ValidToken_ReturnsFalse() {
        String token = jwtProviderAdapter.generateToken("user", new HashMap<>(), 3600000L);
        boolean isExpired = jwtProviderAdapter.isTokenExpired(token);
        assertFalse(isExpired);
    }

    @Test
    void isTokenExpired_ExpiredToken_ReturnsTrue() {
        String token = jwtProviderAdapter.generateToken("user", new HashMap<>(), -1000L);
        boolean isExpired = jwtProviderAdapter.isTokenExpired(token);
        assertTrue(isExpired);
    }

    @Test
    void parseToken_InvalidToken_ThrowsException() {
        String invalidToken = "invalid.token.string";
        assertThrows(DomainException.class, () -> {
            jwtProviderAdapter.parseToken(invalidToken);
        });
    }
}