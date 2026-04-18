package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.dto.request.SocialLoginReqDto;
import com.weartrack.backend.domain.member.dto.response.SocialLoginResDto;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.entity.SocialAccount;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.domain.member.repository.SocialAccountRepository;
import com.weartrack.backend.global.exception.GeneralException;
import com.weartrack.backend.global.security.JwtTokenProvider;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final Map<AuthProvider, SocialLoginProviderClient> providerClients;
    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            List<SocialLoginProviderClient> providerClients,
            MemberRepository memberRepository,
            SocialAccountRepository socialAccountRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.providerClients = mapProviderClients(providerClients);
        this.memberRepository = memberRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    /**
     * 인가 코드를 provider access token으로 교환한 뒤 회원을 조회하거나 생성합니다.
     */
    public SocialLoginResDto login(SocialLoginReqDto request) {
        return login(request.provider(), request.authorizationCode(), request.state());
    }

    @Transactional
    /**
     * 백엔드 callback 또는 내부 요청에서 전달된 provider/code 조합으로 로그인 처리를 수행합니다.
     */
    public SocialLoginResDto login(AuthProvider provider, String authorizationCode, String state) {
        SocialLoginProviderClient providerClient = providerClients.get(provider);
        if (providerClient == null) {
            throw new GeneralException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        }

        SocialUserInfo socialUserInfo = providerClient.getUserInfo(authorizationCode, state);

        Member member = socialAccountRepository
                .findByProviderAndProviderUserId(socialUserInfo.provider(), socialUserInfo.providerUserId())
                .map(SocialAccount::getMember)
                .orElseGet(() -> registerNewMember(socialUserInfo));

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
    private Member registerNewMember(SocialUserInfo socialUserInfo) {
        Member member = memberRepository.save(Member.createPendingProfile());
        socialAccountRepository.save(SocialAccount.of(
                member,
                socialUserInfo.provider(),
                socialUserInfo.providerUserId(),
                socialUserInfo.email()
        ));
        return member;
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
