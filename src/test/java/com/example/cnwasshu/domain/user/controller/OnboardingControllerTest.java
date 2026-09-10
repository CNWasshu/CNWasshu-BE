package com.example.cnwasshu.domain.user.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cnwasshu.common.exception.GlobalExceptionHandler;
import com.example.cnwasshu.common.security.CustomUserPrincipal;
import com.example.cnwasshu.domain.user.dto.request.OnboardingCompleteRequest;
import com.example.cnwasshu.domain.user.dto.response.OnboardingStatusResponse;
import com.example.cnwasshu.domain.user.entity.OnboardingStatus;
import com.example.cnwasshu.domain.user.service.OnboardingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class OnboardingControllerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private OnboardingService onboardingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        OnboardingController controller = new OnboardingController(onboardingService);
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getsOnboardingStatus() throws Exception {
        when(onboardingService.getStatus(USER_ID)).thenReturn(
                new OnboardingStatusResponse(OnboardingStatus.NOT_STARTED, null)
        );

        mockMvc.perform(get("/api/users/me/onboarding"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onboardingStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.onboardingCompletedAt").doesNotExist());
    }

    @Test
    void completesOnboarding() throws Exception {
        LocalDateTime completedAt = LocalDateTime.of(2026, 9, 4, 10, 0);
        when(onboardingService.complete(
                eq(USER_ID),
                eq(new OnboardingCompleteRequest(OnboardingCompleteRequest.CompletionType.COMPLETED))
        )).thenReturn(new OnboardingStatusResponse(OnboardingStatus.COMPLETED, completedAt));

        mockMvc.perform(patch("/api/users/me/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completionType\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onboardingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.onboardingCompletedAt").value("2026-09-04T10:00:00"));
    }

    @Test
    void rejectsMissingCompletionType() throws Exception {
        mockMvc.perform(patch("/api/users/me/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private static class AuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && parameter.getParameterType().equals(CustomUserPrincipal.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return new CustomUserPrincipal(USER_ID);
        }
    }
}
