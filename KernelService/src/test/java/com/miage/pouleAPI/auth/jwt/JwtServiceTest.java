package com.miage.pouleAPI.auth.jwt;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private final String testSecret = "votreTresLongSecretQuiFaitPlusDe32CaracteresPourJWT256bits12345";
    private String validToken;
    private String expiredToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L); // 1 heure

        // Générer un token valide
        validToken = Jwts.builder()
                .subject("123")
                .claim("email", "test@example.com")
                .claim("roles", List.of("ATHLETE"))
                .issuedAt(new Date(System.currentTimeMillis() - 1000))
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(jwtService.getSigningKey())
                .compact();

        // Générer un token expiré
        expiredToken = Jwts.builder()
                .subject("456")
                .claim("email", "expired@example.com")
                .claim("roles", List.of("ADMIN"))
                .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 heures
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // Expiré il y a 1 heure
                .signWith(jwtService.getSigningKey())
                .compact();
    }

    @Test
    void testGenerateToken() {
        // Act
        String token = jwtService.generateToken(123, "test@example.com", "ATHLETE");

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Vérifier le contenu
        String email = jwtService.extractEmail(token);
        List<String> roles = jwtService.extractRoles(token);

        assertEquals("test@example.com", email);
        assertTrue(roles.contains("ATHLETE"));
    }

    @Test
    void testExtractEmail() {
        // Act
        String email = jwtService.extractEmail(validToken);

        // Assert
        assertEquals("test@example.com", email);
    }

    @Test
    void testExtractUsername() {
        // Act
        String username = jwtService.extractUsername(validToken);

        // Assert
        assertEquals("test@example.com", username);
    }

    @Test
    void testExtractRoles() {
        // Act
        List<String> roles = jwtService.extractRoles(validToken);

        // Assert
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ATHLETE", roles.get(0));
    }

    @Test
    void testExtractRoles_EmptyRoles() {
        // Arrange
        String tokenWithoutRoles = Jwts.builder()
                .subject("123")
                .claim("email", "test@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(jwtService.getSigningKey())
                .compact();

        // Act
        List<String> roles = jwtService.extractRoles(tokenWithoutRoles);

        // Assert
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    void testIsTokenValid_ValidToken() {
        // Act
        boolean isValid = jwtService.isTokenValid(validToken);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_ExpiredToken() {
        // Act
        boolean isValid = jwtService.isTokenValid(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.isTokenValid(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid_MalformedToken() {
        // Arrange
        String malformedToken = "header.payload.signature";

        // Act
        boolean isValid = jwtService.isTokenValid(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid_NullToken() {
        // Act
        boolean isValid = jwtService.isTokenValid(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid_EmptyToken() {
        // Act
        boolean isValid = jwtService.isTokenValid("");

        // Assert
        assertFalse(isValid);
    }
}