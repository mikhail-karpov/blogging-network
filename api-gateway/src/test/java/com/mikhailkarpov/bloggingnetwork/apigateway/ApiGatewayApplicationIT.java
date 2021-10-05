package com.mikhailkarpov.bloggingnetwork.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationIT {

    @Container
    public static GenericContainer EUREKA = new GenericContainer("springcloud/eureka")
            .withExposedPorts(8761);

    @DynamicPropertySource
    static void configureEureka(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.serviceUrl.defaultZone", ApiGatewayApplicationIT::eurekaDefaultZone);
    }

    private static String eurekaDefaultZone() {
        return String.format("%s:%d/eureka/", EUREKA.getHost(), EUREKA.getFirstMappedPort());
    }

    @Test
    void contextLoads() {
    }

}
