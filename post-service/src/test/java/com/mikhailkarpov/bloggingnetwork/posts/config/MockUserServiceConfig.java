package com.mikhailkarpov.bloggingnetwork.posts.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@TestConfiguration
@LoadBalancerClient("user-service")
public class MockUserServiceConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer firstMockServer() {

        return new WireMockServer(8090);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer secondMockServer() {

        return new WireMockServer(8091);
    }

    @Bean
    public ServiceInstanceListSupplier serviceInstanceListSupplier() {
        return new ServiceInstanceListSupplier() {

            private final String serviceId = "user-service";

            @Override
            public String getServiceId() {
                return this.serviceId;
            }

            @Override
            public Flux<List<ServiceInstance>> get() {
                List<ServiceInstance> instances = Arrays.asList(
                        new DefaultServiceInstance(serviceId + 1, serviceId, "localhost", 8090, false),
                        new DefaultServiceInstance(serviceId + 2, serviceId, "localhost", 8091, false)
                );
                return Flux.just(instances);
            }
        };
    }
}