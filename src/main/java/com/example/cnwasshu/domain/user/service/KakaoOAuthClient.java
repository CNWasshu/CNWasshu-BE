package com.example.cnwasshu.domain.user.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;
import com.example.cnwasshu.domain.user.dto.kakao.KakaoTokenResponse;
import com.example.cnwasshu.domain.user.dto.kakao.KakaoUserInfoResponse;
import com.example.cnwasshu.domain.user.dto.response.KakaoUserInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    private final KakaoProperties kakaoProperties;
    private final RestClient restClient = RestClient.create();

    public String exchangeToken(String authorizationCode, String redirectUri) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoProperties.clientId());
        formData.add("redirect_uri", redirectUri != null ? redirectUri : kakaoProperties.redirectUri());
        formData.add("code", authorizationCode);
        if (kakaoProperties.clientSecret() != null && !kakaoProperties.clientSecret().isBlank()) {
            formData.add("client_secret", kakaoProperties.clientSecret());
        }

        try {
            KakaoTokenResponse response = restClient.post()
                    .uri(TOKEN_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(KakaoTokenResponse.class);

            if (response == null) {
                throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
            }
            return response.accessToken();
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    public KakaoUserInfo fetchUserInfo(String kakaoAccessToken) {
        try {
            KakaoUserInfoResponse response = restClient.get()
                    .uri(USER_INFO_URI)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .body(KakaoUserInfoResponse.class);

            if (response == null) {
                throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
            }
            return KakaoUserInfo.from(response);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
        }
    }
}
