package com.berrie.gamerental.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtAuthServiceTest {

    private static final String USERNAME = "berrieUser";
    private static final String SECRET_KEY = "5468576D5A7134743777217A25432A462D4A614E645267556B58703272357538";

    @Mock
    private UserDetails userDetails;
    @InjectMocks
    private JwtAuthService jwtAuthService;
    private String token;

    @BeforeEach()
    public void setup() {
        token = Jwts.builder()
                .setSubject(USERNAME)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 300000))
                .signWith(getSignInKey())
                .compact();
    }

    @Test
    public void extractUsername_withToken_returnsUsername() {
        // when
        String actual = jwtAuthService.extractUsername(token);
        // then
        assertThat(actual).isEqualTo(USERNAME);
    }

    @Test
    public void generateToken_withUserDetails_returnsJwtToken() {
        // given
        when(userDetails.getUsername()).thenReturn(USERNAME);
        // when
        String actual = jwtAuthService.generateToken(userDetails);
        // then
        assertTokenIsJsonWebToken(actual);
    }

    @Test
    public void isTokenValid_withValidToken_returnsTrue() {
        // given
        when(userDetails.getUsername()).thenReturn(USERNAME);
        // then
        assertThat(jwtAuthService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    public void isTokenValid_withInvalidUsername_returnsFalse() {
        // given
        when(userDetails.getUsername()).thenReturn("invalidUser");
        // then
        assertThat(jwtAuthService.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    public void isTokenValid_withExpiredToken_returnsFalse() {
        // given
        String invalidToken = Jwts.builder()
                .setSubject(USERNAME)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 100))
                .signWith(getSignInKey())
                .compact();

        // allowed clock skew is 0 seconds, thus an exception will be thrown in the test
        assertThatThrownBy(() -> jwtAuthService.isTokenValid(invalidToken, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }

    private void assertTokenIsJsonWebToken(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build();
            parser.parseClaimsJws(token);
        } catch (JwtException e) {
            fail("token is not a Json Web Token");
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
