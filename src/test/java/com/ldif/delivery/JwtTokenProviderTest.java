package com.ldif.delivery;

import com.ldif.delivery.global.infrastructure.config.security.JwtTokenProvider;
import com.ldif.delivery.user.domain.entity.UserRoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secretKey = "7Iuc7YGs66at7YKk64qU7J2066of67O064uk66as67mE7Iqk7YWM7Iqk7Yq47Jqp7Iuc7YGs66at7YKk7J6F64uI64ukLg=="; // Base64 encoded

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // ReflectionTestUtils를 사용하여 @Value 필드 주입
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", secretKey);
        // @PostConstruct 메서드 직접 호출
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("토큰 생성 및 정보 추출 성공")
    void createToken_And_GetUserInfo_Success() {
        // given
        String username = "testuser";
        UserRoleEnum role = UserRoleEnum.CUSTOMER;

        // when
        String token = jwtTokenProvider.createToken(username, role);
        String pureToken = jwtTokenProvider.substringToken(token);
        Claims claims = jwtTokenProvider.getUserInfoFromToken(pureToken);

        // then
        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "));
        assertEquals(username, claims.getSubject());
        assertEquals(role.name(), claims.get(JwtTokenProvider.AUTHORIZATION_KEY));
    }

    @Test
    @DisplayName("토큰 가공(substring) 성공")
    void substringToken_Success() {
        // given
        String tokenWithBearer = "Bearer test-token-value";

        // when
        String result = jwtTokenProvider.substringToken(tokenWithBearer);

        // then
        assertEquals("test-token-value", result);
    }

    @Test
    @DisplayName("헤더에서 토큰 추출 성공")
    void getTokenFromHeader_Success() {
        // given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        given(request.getHeader(JwtTokenProvider.AUTHORIZATION_HEADER)).willReturn("Bearer some-token");

        // when
        String result = jwtTokenProvider.getTokenFromHeader(request);

        // then
        assertEquals("Bearer some-token", result);
    }

    @Test
    @DisplayName("토큰 검증 성공 - 유효한 토큰")
    void validateToken_Success() {
        // given
        String token = jwtTokenProvider.createToken("testuser", UserRoleEnum.CUSTOMER);
        String pureToken = jwtTokenProvider.substringToken(token);

        // when
        boolean isValid = jwtTokenProvider.validateToken(pureToken);

        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("토큰 검증 실패 - 잘못된 서명")
    void validateToken_Fail_InvalidSignature() {
        // given
        // 다른 키로 생성된 토큰 시뮬레이션
        Key anotherKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String invalidToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("testuser")
                .signWith(anotherKey)
                .compact();

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("토큰 가공 실패 - 잘못된 접두사")
    void substringToken_Fail_InvalidPrefix() {
        // given
        String invalidToken = "InvalidPrefix token-value";

        // when & then
        assertThrows(NullPointerException.class, () -> {
            jwtTokenProvider.substringToken(invalidToken);
        });
    }
}
