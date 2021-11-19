package com.mikhailkarpov.bloggingnetwork.feed.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

@TestConfiguration
@EnableJpaAuditing
public class PersistenceTestConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres")
                .withDatabaseName("user_feed_service")
                .withUsername("user_feed_service")
                .withPassword("pa55word")
                .withExposedPorts(5432);
    }

    @Bean
    public DataSource datasource(PostgreSQLContainer postgreSQLContainer) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();

        dataSourceBuilder.url(postgreSQLContainer.getJdbcUrl());
        dataSourceBuilder.driverClassName(postgreSQLContainer.getDriverClassName());
        dataSourceBuilder.username(postgreSQLContainer.getUsername());
        dataSourceBuilder.password(postgreSQLContainer.getPassword());

        return dataSourceBuilder.build();
    }
}
