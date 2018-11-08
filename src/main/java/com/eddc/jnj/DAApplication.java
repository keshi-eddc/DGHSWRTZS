package com.eddc.jnj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.eddc.jnj.dao")
@EnableScheduling
public class DAApplication {

	public static void main(String[] args) {
		SpringApplication.run(DAApplication.class, args);
	}
}
