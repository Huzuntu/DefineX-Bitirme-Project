package com.definex.task_management.security.jwt;

import com.definex.task_management.entity.User;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.security.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;
    private UserDetails userDetails;
    private static final String VALID_TOKEN = "valid.jwt.token";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .role(UserRole.PROJECT_MANAGER)
                .department("IT")
                .build();

        userDetails = new CustomUserDetails(testUser);
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_TOKEN, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testUser.getEmail(), SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNoAuthHeader_ShouldNotAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_WithInvalidAuthHeaderFormat_ShouldNotAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat " + VALID_TOKEN);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_TOKEN, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithExpiredToken_ShouldNotAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtService.extractUsername(VALID_TOKEN)).thenThrow(
            new ExpiredJwtException(
                mock(Header.class),
                mock(Claims.class),
                "Token has expired"
            )
        );
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setContentType("application/json");
        writer.flush();
        assertTrue(stringWriter.toString().contains("JWT expired"));
        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(filterChain);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_WithExistingAuthentication_ShouldNotReauthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(testUser.getEmail());
        
        SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            )
        );

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }
} 