package com.newwork.employeeprofile.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    // Only start Testcontainers if not running in CI (CI uses service containers)
    private static final boolean IS_CI = System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null;

    static PostgreSQLContainer<?> postgres;
    static MongoDBContainer mongodb;
    static GenericContainer<?> redis;

    static {
        if (!IS_CI) {
            // Local development: start Testcontainers
            postgres = new PostgreSQLContainer<>(
                    DockerImageName.parse("postgres:15-alpine"))
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

            mongodb = new MongoDBContainer(
                    DockerImageName.parse("mongo:7.0"))
                    .withExposedPorts(27017);

            redis = new GenericContainer<>(
                    DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

            postgres.start();
            mongodb.start();
            redis.start();
        }
        // In CI: containers are provided by GitHub Actions service containers
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (!IS_CI && postgres != null) {
            // Local: use Testcontainers
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
            registry.add("spring.redis.host", redis::getHost);
            registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
        }
        // In CI: environment variables from workflow are used via application-test.yml
    }
}
