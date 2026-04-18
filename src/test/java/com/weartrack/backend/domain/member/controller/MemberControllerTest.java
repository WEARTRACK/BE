package com.weartrack.backend.domain.member.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weartrack.backend.domain.member.dto.request.NicknameSetReqDto;
import com.weartrack.backend.domain.member.dto.response.NicknameAvailabilityCheckResDto;
import com.weartrack.backend.domain.member.dto.response.NicknameSetResDto;
import com.weartrack.backend.domain.member.service.MemberService;
import com.weartrack.backend.global.exception.GlobalExceptionHandler;
import com.weartrack.backend.global.security.JwtAuthenticationFilter;
import com.weartrack.backend.global.security.JwtPrincipal;
import com.weartrack.backend.global.security.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MemberController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("닉네임 중복 확인 요청이 성공하면 사용 가능 여부를 반환한다.")
    void checkNicknameAvailability() throws Exception {
        given(memberService.checkNicknameAvailability("weartrack"))
                .willReturn(new NicknameAvailabilityCheckResDto("weartrack", true));

        mockMvc.perform(get("/api/members/nickname/check")
                        .param("nickname", "weartrack"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.available").value(true));
    }

    @Test
    @DisplayName("닉네임 중복 확인에서 빈 문자열이 들어오면 검증 오류를 반환한다.")
    void checkNicknameAvailabilityValidationFail() throws Exception {
        mockMvc.perform(get("/api/members/nickname/check")
                        .param("nickname", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("닉네임 설정 요청이 성공하면 변경된 닉네임을 반환한다.")
    void setNickname() throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                new JwtPrincipal(1L),
                null,
                List.of()
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);

        given(memberService.setNickname(1L, new NicknameSetReqDto("weartrack")))
                .willReturn(new NicknameSetResDto(1L, "weartrack", true));

        try {
            mockMvc.perform(patch("/api/members/me/nickname")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new NicknameSetReqDto("weartrack"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.nickname").value("weartrack"))
                    .andExpect(jsonPath("$.result.profileCompleted").value(true));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
