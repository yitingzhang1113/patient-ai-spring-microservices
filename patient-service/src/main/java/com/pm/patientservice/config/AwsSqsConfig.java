package com.pm.patientservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Configuration for AWS SQS. This bean supplies a singleton {@link SqsClient}
 * that reads its region from the {@code aws.region} application property. The
 * actual AWS credentials are resolved from the standard AWS SDK credential
 * provider chain (environment variables, system properties, EC2/ECS
 * instance metadata, etc.).
 */
@Configuration
public class AwsSqsConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    /**
     * Creates a synchronous SQS client configured for the given AWS region.
     * Only enabled when aws.enabled property is true.
     *
     * @return a configured {@link SqsClient}
     */
    @Bean
    @ConditionalOnProperty(name = "aws.enabled", havingValue = "true", matchIfMissing = false)
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(region))
                .build();
    }
}