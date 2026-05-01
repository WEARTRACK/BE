package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.OAuthHandoffPayload;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OAuthHandoffService {

    private final Map<String, StoredOAuthHandoff> handoffs = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final Clock clock;

    public OAuthHandoffService(
            @Value("${app.oauth.mobile.handoff-ttl-seconds:180}") long handoffTtlSeconds
    ) {
        this(Duration.ofSeconds(handoffTtlSeconds), Clock.systemUTC());
    }

    OAuthHandoffService(Duration ttl, Clock clock) {
        this.ttl = ttl;
        this.clock = clock;
    }

    public String create(AuthProvider provider, String authorizationCode, String state) {
        purgeExpired();

        String token = UUID.randomUUID().toString();
        Instant expiresAt = clock.instant().plus(ttl);
        handoffs.put(token, new StoredOAuthHandoff(
                new OAuthHandoffPayload(provider, authorizationCode, state),
                expiresAt
        ));
        return token;
    }

    public OAuthHandoffPayload consume(AuthProvider provider, String token) {
        purgeExpired();

        StoredOAuthHandoff handoff = handoffs.remove(token);
        if (handoff == null || handoff.isExpired(clock.instant())) {
            throw new GeneralException(AuthErrorCode.INVALID_OAUTH_HANDOFF);
        }

        if (handoff.payload().provider() != provider) {
            throw new GeneralException(AuthErrorCode.INVALID_OAUTH_HANDOFF);
        }

        return handoff.payload();
    }

    private void purgeExpired() {
        Instant now = clock.instant();
        handoffs.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private record StoredOAuthHandoff(
            OAuthHandoffPayload payload,
            Instant expiresAt
    ) {
        private boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }
    }
}
