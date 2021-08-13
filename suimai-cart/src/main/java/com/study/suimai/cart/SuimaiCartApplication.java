package com.study.suimai.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class SuimaiCartApplication {

	public static void main(String[] args) {
		SpringApplication.run(SuimaiCartApplication.class, args);
	}

}
