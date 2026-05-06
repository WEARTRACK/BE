package com.weartrack.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * WearTrack 백엔드 애플리케이션 시작점이다.
 */
@EnableRetry
@SpringBootApplication
public class WeartrackBackendApplication {

	/**
	 * 스프링 부트 애플리케이션을 실행한다.
	 */
	public static void main(String[] args) {
		SpringApplication.run(WeartrackBackendApplication.class, args);
	}
}
