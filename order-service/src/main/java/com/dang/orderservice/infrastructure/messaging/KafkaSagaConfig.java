package com.dang.orderservice.infrastructure.messaging;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaSagaTopicsProperties.class)
public class KafkaSagaConfig {
}
