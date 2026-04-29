package com.ldif.delivery;

import com.ldif.delivery.global.infrastructure.config.security.UserDetailsImpl;
import com.ldif.delivery.user.application.service.UserServiceV1;
import com.ldif.delivery.user.domain.entity.UserEntity;
import com.ldif.delivery.user.domain.entity.UserRoleEnum;
import com.ldif.delivery.user.domain.repository.UserRepository;
import com.ldif.delivery.user.presentation.dto.request.ReqUserDto;
import com.ldif.delivery.user.presentation.dto.request.ReqUserRoleDto;
import com.ldif.delivery.user.presentation.dto.response.ResUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceV1Test {

    @InjectMocks
    private UserServiceV1 userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserEntity userEntity;
    private UserDetailsImpl loginUser;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity("testuser", "tester", "test@example.com", "password", UserRoleEnum.CUSTOMER);
        loginUser = mock(UserDetailsImpl.class);
    }

    @Test
    @DisplayName("사용자 목록 조회 성공 - 허용된 사이즈")
    void getUsers_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 30);
        Page<UserEntity> userPage = new PageImpl<>(List.of(userEntity));
        given(userRepository.searchUsers(any(), any(), eq(pageable))).willReturn(userPage);

        // when
        Page<ResUserDto> result = userService.getUsers("keyword", UserRoleEnum.CUSTOMER, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).searchUsers(eq("keyword"), eq(UserRoleEnum.CUSTOMER), eq(pageable));
    }

    @Test
    @DisplayName("사용자 목록 조회 - 허용되지 않은 사이즈 시 기본값 10으로 변경")
    void getUsers_FallbackSize() {
        // given
        Pageable pageable = PageRequest.of(0, 20); // 20 is not in (10, 30, 50)
        given(userRepository.searchUsers(any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        // when
        userService.getUsers(null, null, pageable);

        // then
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).searchUsers(any(), any(), pageableCaptor.capture());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    @Test
    @DisplayName("사용자 단건 조회 성공")
    void getUserInfo_Success() {
        // given
        given(loginUser.hasPermission("testuser")).willReturn(true);
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(userEntity));

        // when
        ResUserDto result = userService.getUserInfo("testuser", loginUser);

        // then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("사용자 단건 조회 실패 - 권한 없음")
    void getUserInfo_Fail_AccessDenied() {
        // given
        given(loginUser.hasPermission("testuser")).willReturn(false);

        // when & then
        assertThrows(AccessDeniedException.class, () -> {
            userService.getUserInfo("testuser", loginUser);
        });
    }

    @Test
    @DisplayName("사용자 단건 조회 실패 - 이미 탈퇴한 사용자")
    void getUserInfo_Fail_DeletedUser() {
        // given
        given(loginUser.hasPermission("testuser")).willReturn(true);
        userEntity.softDelete("admin");
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(userEntity));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserInfo("testuser", loginUser);
        });
        assertTrue(exception.getMessage().contains("이미 탈퇴 처리된 사용자입니다."));
    }

    @Test
    @DisplayName("사용자 정보 수정 성공")
    void updateUserInfo_Success() {
        // given
        ReqUserDto reqDto = mock(ReqUserDto.class);
        given(reqDto.getNickname()).willReturn("newNickname");
        given(reqDto.getEmail()).willReturn("new@example.com");
        given(reqDto.getIsPublic()).willReturn(false);
        given(reqDto.getPassword()).willReturn("newPassword");

        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(userEntity));
        given(loginUser.getUsername()).willReturn("testuser");
        given(passwordEncoder.encode("newPassword")).willReturn("encodedNewPassword");

        // when
        ResUserDto result = userService.updateUserInfo("testuser", reqDto, loginUser);

        // then
        assertEquals("newNickname", userEntity.getNickname());
        assertEquals("new@example.com", userEntity.getEmail());
        assertFalse(userEntity.getIsPublic());
        assertEquals("encodedNewPassword", userEntity.getPassword());
    }

    @Test
    @DisplayName("사용자 정보 수정 실패 - 타인의 비밀번호 수정 시도")
    void updateUserInfo_Fail_OtherPassword() {
        // given
        ReqUserDto reqDto = mock(ReqUserDto.class);
        given(reqDto.getPassword()).willReturn("newPassword");

        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(userEntity));
        given(loginUser.getUsername()).willReturn("otherUser");

        // when & then
        assertThrows(AccessDeniedException.class, () -> {
            userService.updateUserInfo("testuser", reqDto, loginUser);
        });
    }

    @Test
    @DisplayName("사용자 권한 수정 성공")
    void updateUserRole_Success() {
        // given
        ReqUserRoleDto reqDto = mock(ReqUserRoleDto.class);
        given(reqDto.getRole()).willReturn(UserRoleEnum.MANAGER);
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(userEntity));

        // when
        userService.updateUserRole("testuser", reqDto);

        // then
        assertEquals(UserRoleEnum.MANAGER, userEntity.getRole());
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteUser_Success() {
        // given
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(userEntity));
        given(loginUser.getUsername()).willReturn("admin");

        // when
        userService.deleteUser("testuser", loginUser);

        // then
        assertNotNull(userEntity.getDeletedAt());
        assertEquals("admin", userEntity.getDeletedBy());
    }
}
