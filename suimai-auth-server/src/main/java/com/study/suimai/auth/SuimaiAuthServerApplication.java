package com.study.suimai.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableFeignClients
@SpringBootApplication
public class SuimaiAuthServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SuimaiAuthServerApplication.class, args);
	}

}
