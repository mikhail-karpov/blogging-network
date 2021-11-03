package com.mikhailkarpov.bloggingnetwork.feed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserFeedServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserFeedServiceApplication.class, args);
	}

}
