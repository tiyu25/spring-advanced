package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    public void 회원가입_시_이미_존재하는_이메일() {
        // given
        SignupRequest request = new SignupRequest("test@naver.com", "test", "USER");

        // 이메일이 이미 존재한다고 가정
        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when & then
        InvalidRequestException exception = Assertions.assertThrows(
                InvalidRequestException.class,
                () -> authService.signup(request)
        );

        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    };

    @Test
    public void 회원가입_성공() {
        // given
        SignupRequest request = new SignupRequest("test@naver.com", "123", "USER");
        User user = new User("test@naver.com", "123", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.existsByEmail(anyString())).willReturn(false); //이메일 중복X
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword"); //패스워드
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtUtil.createToken(anyLong(), anyString(), any(UserRole.class))).willReturn("bearer");

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertEquals("bearer", response.getBearerToken());
    }

    @Test
    public void 로그인_시_가입_된_유저가_없을_때() {
        //given
        SigninRequest request = new SigninRequest("test@naver.com", "test");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception2 = assertThrows(
            InvalidRequestException.class, () -> authService.signin(request)
        );

        assertEquals("가입되지 않은 유저입니다.", exception2.getMessage());
    }

    @Test
    public void 로그인_시_이메일과_비밀번호가_일치하지_않을_때() {
        //given
        User user = new User("test@naver.com", "123", UserRole.USER);

        SigninRequest request = new SigninRequest("test@naver.com", "123");
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(new User()));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

        // when & then
        AuthException exception = assertThrows(
                AuthException.class, () -> authService.signin(request)
        );

        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    public void 로그인_성공() {
        //given
        User user = new User("test@naver.com", "123", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        SigninRequest request = new SigninRequest("test@naver.com", "123");
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole())).willReturn("bearer");

        // when
        SigninResponse response = authService.signin(request);

        // then
        assertEquals("bearer", response.getBearerToken());
    }

}
