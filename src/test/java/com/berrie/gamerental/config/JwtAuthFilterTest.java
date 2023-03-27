package com.berrie.gamerental.config;

import com.berrie.gamerental.service.JwtAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {

    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String USERNAME = "berrieUser";
    private static final String PASSWORD = "password";

    @Mock
    private JwtAuthService jwtAuthService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_tokenPresentAndValid_authenticatesUser() throws Exception {
        // given
        String token = "valid.token";
        String authHeader = "Bearer " + token;
        UserDetails userDetails = new User(USERNAME, PASSWORD, Collections.emptyList());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContext initialContext = SecurityContextHolder.getContext();

        when(request.getHeader(AUTH_HEADER_NAME)).thenReturn(authHeader);
        when(jwtAuthService.extractUsername(token)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtAuthService.isTokenValid(token, userDetails)).thenReturn(true);

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(request).getHeader(AUTH_HEADER_NAME);
        verify(jwtAuthService).extractUsername(token);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(jwtAuthService).isTokenValid(token, userDetails);
        verify(filterChain).doFilter(request, response);

        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isEqualTo(authToken);
        // reset the context
        SecurityContextHolder.setContext(initialContext);
    }

    @Test
    void doFilterInternal_tokenNotPresent_doesNotAuthenticateUser() throws Exception {
        // given
        String authHeader = null;
        when(request.getHeader(AUTH_HEADER_NAME)).thenReturn(authHeader);

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(request).getHeader(AUTH_HEADER_NAME);
        verify(jwtAuthService, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtAuthService, never()).isTokenValid(anyString(), any(UserDetails.class));
        verify(filterChain).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();;
    }

    @Test
    void doFilterInternal_tokenPresentAndInvalid_doesNotAuthenticateUser() throws Exception {
        // given
        String token = "invalid.token";
        String authHeader = "Bearer " + token;
        UserDetails userDetails = new User(USERNAME, PASSWORD, Collections.emptyList());
        when(request.getHeader(AUTH_HEADER_NAME)).thenReturn(authHeader);
        when(jwtAuthService.extractUsername(token)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtAuthService.isTokenValid(token, userDetails)).thenReturn(false);

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(request).getHeader(AUTH_HEADER_NAME);
        verify(jwtAuthService).extractUsername(token);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(jwtAuthService).isTokenValid(token, userDetails);
        verify(filterChain).doFilter(request, response);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();;
    }
}
