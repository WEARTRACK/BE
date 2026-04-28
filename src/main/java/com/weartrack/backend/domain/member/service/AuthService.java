package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.dto.request.SocialLoginReqDto;
import com.weartrack.backend.domain.member.dto.response.SocialLoginResDto;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.entity.SocialAccount;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.domain.member.repository.SocialAccountRepository;
import com.weartrack.backend.global.exception.GeneralException;
import com.weartrack.backend.global.security.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 소셜 로그인과 신규 회원 등록 흐름을 처리한다.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final Map<AuthProvider, SocialLoginProviderClient> providerClients;
    private final SocialAccountRepository socialAccountRepository;
    private final AuthRegistrationService authRegistrationService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            List<SocialLoginProviderClient> providerClients,
            SocialAccountRepository socialAccountRepository,
            AuthRegistrationService authRegistrationService,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.providerClients = mapProviderClients(providerClients);
        this.socialAccountRepository = socialAccountRepository;
        this.authRegistrationService = authRegistrationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 인가 코드를 provider access token으로 교환한 뒤 회원을 조회하거나 생성합니다.
     */
    @Transactional
    public SocialLoginResDto login(SocialLoginReqDto request, String expectedState) {
        return login(request.provider(), request.authorizationCode(), request.state(), expectedState);
    }

    /**
     * 백엔드 callback 또는 내부 요청에서 전달된 provider/code 조합으로 로그인 처리를 수행합니다.
     */
    @Transactional
    public SocialLoginResDto login(
            AuthProvider provider,
            String authorizationCode,
            String state,
            String expectedState
    ) {
        validateState(state, expectedState);

        SocialLoginProviderClient providerClient = providerClients.get(provider);
        if (providerClient == null) {
            throw new GeneralException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        }

        SocialUserInfo socialUserInfo = providerClient.getUserInfo(authorizationCode, state);

        Member member = socialAccountRepository
                .findByProviderAndProviderUserId(socialUserInfo.provider(), socialUserInfo.providerUserId())
                .map(SocialAccount::getMember)
                .orElseGet(() -> findOrRegisterMember(socialUserInfo));

        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getMemberId());

        return new SocialLoginResDto(
                member.getMemberId(),
                member.getNickname(),
                member.hasNickname(),
                accessToken,
                refreshToken
        );
    }

    /**
     * 최초 로그인 사용자는 닉네임 없이 member와 social_account를 함께 생성합니다.
     */
    private Member findOrRegisterMember(SocialUserInfo socialUserInfo) {
        try {
            return authRegistrationService.registerNewMember(socialUserInfo);
        } catch (DataIntegrityViolationException e) {
            return socialAccountRepository
                    .findByProviderAndProviderUserId(socialUserInfo.provider(), socialUserInfo.providerUserId())
                    .map(SocialAccount::getMember)
                    .orElseThrow(() -> e);
        }
    }

    /**
     * OAuth 인가 요청과 콜백이 같은 브라우저 흐름인지 확인한다.
     */
    private void validateState(String state, String expectedState) {
        if (!StringUtils.hasText(state) || !StringUtils.hasText(expectedState)) {
            throw new GeneralException(AuthErrorCode.INVALID_OAUTH_STATE);
        }

        boolean matches = MessageDigest.isEqual(
                state.getBytes(StandardCharsets.UTF_8),
                expectedState.getBytes(StandardCharsets.UTF_8)
        );

        if (!matches) {
            throw new GeneralException(AuthErrorCode.INVALID_OAUTH_STATE);
        }
    }

    /**
     * provider enum을 key로 사용해 각 소셜 로그인 구현체를 빠르게 조회합니다.
     */
    private Map<AuthProvider, SocialLoginProviderClient> mapProviderClients(List<SocialLoginProviderClient> providerClients) {
        Map<AuthProvider, SocialLoginProviderClient> mappedClients = new EnumMap<>(AuthProvider.class);
        for (SocialLoginProviderClient providerClient : providerClients) {
            mappedClients.put(providerClient.supports(), providerClient);
        }
        return mappedClients;
    }
}
