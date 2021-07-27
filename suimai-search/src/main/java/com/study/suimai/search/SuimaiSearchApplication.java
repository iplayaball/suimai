package com.study.suimai.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients // 必须加，不然会ProductFeignService找不到这个 Bean
@SpringBootApplication
public class SuimaiSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SuimaiSearchApplication.class, args);
	}

}
