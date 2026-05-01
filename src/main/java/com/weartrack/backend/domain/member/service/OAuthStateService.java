package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.constant.AuthClientType;
import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.OAuthStatePayload;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OAuthStateService {

    private final Map<String, StoredOAuthState> states = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final Clock clock;

    public OAuthStateService(
            @Value("${app.oauth.state-ttl-seconds:300}") long stateTtlSeconds
    ) {
        this(Duration.ofSeconds(stateTtlSeconds), Clock.systemUTC());
    }

    OAuthStateService(Duration ttl, Clock clock) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("app.oauth.state-ttl-seconds must be positive");
        }
        this.ttl = ttl;
        this.clock = clock;
    }

    public String create(AuthProvider provider, AuthClientType clientType) {
        purgeExpired();

        String state = UUID.randomUUID().toString();
        states.put(state, new StoredOAuthState(
                new OAuthStatePayload(provider, clientType),
                clock.instant().plus(ttl)
        ));
        return state;
    }

    public Optional<OAuthStatePayload> consumeIfPresent(AuthProvider provider, String state) {
        purgeExpired();

        StoredOAuthState storedState = states.remove(state);
        if (storedState == null || storedState.isExpired(clock.instant())) {
            return Optional.empty();
        }

        if (storedState.payload().provider() != provider) {
            throw new GeneralException(AuthErrorCode.INVALID_OAUTH_STATE);
        }

        return Optional.of(storedState.payload());
    }

    private void purgeExpired() {
        Instant now = clock.instant();
        states.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private record StoredOAuthState(
            OAuthStatePayload payload,
            Instant expiresAt
    ) {
        private boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }
    }
}
