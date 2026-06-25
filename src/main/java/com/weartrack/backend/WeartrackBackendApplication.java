package com.weartrack.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * WearTrack 백엔드 애플리케이션 시작점이다.
 */
@EnableAsync
@EnableRetry
@EnableScheduling
@SpringBootApplication
public class WeartrackBackendApplication {

	/**
	 * 스프링 부트 애플리케이션을 실행한다.
	 */
	public static void main(String[] args) {
		SpringApplication.run(WeartrackBackendApplication.class, args);
	}
}
