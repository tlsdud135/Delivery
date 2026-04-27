package com.ldif.delivery;

import com.ldif.delivery.auth.application.service.AuthServiceV1;
import com.ldif.delivery.user.domain.entity.UserEntity;
import com.ldif.delivery.user.domain.entity.UserRoleEnum;
import com.ldif.delivery.user.domain.repository.UserRepository;
import com.ldif.delivery.user.presentation.dto.request.ReqSignupDto;
import com.ldif.delivery.user.presentation.dto.response.ResUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceV1Test {

    @InjectMocks
    private AuthServiceV1 authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ReqSignupDto reqSignupDto;

    @BeforeEach
    void setUp() {
        reqSignupDto = new ReqSignupDto();
        reqSignupDto.setUsername("testuser");
        reqSignupDto.setPassword("password123");
        reqSignupDto.setNickname("tester");
        reqSignupDto.setEmail("test@example.com");
        reqSignupDto.setRole(UserRoleEnum.CUSTOMER);
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() {
        // given
        given(userRepository.findByUsername(reqSignupDto.getUsername())).willReturn(Optional.empty());
        given(userRepository.findByEmail(reqSignupDto.getEmail())).willReturn(Optional.empty());
        given(passwordEncoder.encode(reqSignupDto.getPassword())).willReturn("encodedPassword");

        UserEntity savedUser = new UserEntity(
                reqSignupDto.getUsername(),
                reqSignupDto.getNickname(),
                reqSignupDto.getEmail(),
                "encodedPassword",
                reqSignupDto.getRole()
        );
        given(userRepository.save(any(UserEntity.class))).willReturn(savedUser);

        // when
        ResUserDto result = authService.signup(reqSignupDto);

        // then
        assertNotNull(result);
        assertEquals(reqSignupDto.getUsername(), result.getUsername());
        assertEquals(reqSignupDto.getNickname(), result.getNickname());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 username")
    void signup_Fail_DuplicateUsername() {
        // given
        given(userRepository.findByUsername(reqSignupDto.getUsername()))
                .willReturn(Optional.of(new UserEntity()));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signup(reqSignupDto);
        });
        assertEquals("중복된 사용자가 존재합니다.", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 email")
    void signup_Fail_DuplicateEmail() {
        // given
        given(userRepository.findByUsername(reqSignupDto.getUsername())).willReturn(Optional.empty());
        given(userRepository.findByEmail(reqSignupDto.getEmail()))
                .willReturn(Optional.of(new UserEntity()));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signup(reqSignupDto);
        });
        assertEquals("중복된 Email 입니다.", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}
