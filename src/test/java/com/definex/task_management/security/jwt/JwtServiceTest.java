package com.definex.task_management.security.jwt;

import com.definex.task_management.entity.User;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.security.CustomUserDetails;
import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;
    private UserDetails userDetails;
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long JWT_EXPIRATION = 86400000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);

        user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.PROJECT_MANAGER)
                .department("IT")
                .build();

        userDetails = new CustomUserDetails(user);
    }

    @Test
    void generateToken_Success() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void generateToken_WithAdditionalClaims_Success() {
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("customClaim", "customValue");

        String token = jwtService.generateToken(userDetails, additionalClaims);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertEquals("customValue", jwtService.extractClaim(token, claims -> claims.get("customClaim")));
    }

    @Test
    void extractUsername_Success() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals(user.getEmail(), username);
    }

    @Test
    void extractClaims_Success() {
        String token = jwtService.generateToken(userDetails);

        assertEquals(user.getEmail(), jwtService.extractClaim(token, Claims::getSubject));
        assertEquals(user.getRole().name(), jwtService.extractClaim(token, claims -> claims.get("role")));
        assertNotNull(jwtService.extractClaim(token, claims -> claims.get("permissions")));
    }

    @Test
    void isTokenValid_WithValidToken_ReturnsTrue() {
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_WithExpiredToken_ReturnsFalse() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -10000); 
        String token = jwtService.generateToken(userDetails);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION); 

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }


    @Test
    void isTokenValid_WithDifferentUser_ReturnsFalse() {
        String token = jwtService.generateToken(userDetails);
        
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .name("Different User")
                .email("different@example.com")
                .role(UserRole.TEAM_MEMBER)
                .department("HR")
                .build();
        UserDetails differentUserDetails = new CustomUserDetails(differentUser);

        assertFalse(jwtService.isTokenValid(token, differentUserDetails));
    }

    @Test
    void extractExpiration_Success() {
        String token = jwtService.generateToken(userDetails);

        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date(System.currentTimeMillis())));
        assertTrue(expiration.before(new Date(System.currentTimeMillis() + JWT_EXPIRATION + 1000)));
    }
} 