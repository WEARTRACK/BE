package com.weartrack.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 기능을 활성화한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
