package com.mikhailkarpov.bloggingnetwork.posts.config;

import com.mikhailkarpov.bloggingnetwork.posts.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

public abstract class AbstractIT {

    static GenericContainer EUREKA;

    static {
        EUREKA = new GenericContainer("springcloud/eureka")
                .withExposedPorts(8761)
                .withReuse(true);

        EUREKA.start();
    }

    @DynamicPropertySource
    static void configureEureka(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.serviceUrl.defaultZone", AbstractIT::eurekaDefaultZone);
    }

    private static String eurekaDefaultZone() {
        return String.format("%s:%d/eureka/", EUREKA.getHost(), EUREKA.getFirstMappedPort());
    }

    @Autowired
    private PostRepository postRepository;

    @AfterEach
    void cleanRepository() {
        postRepository.deleteAll();
    }
}
