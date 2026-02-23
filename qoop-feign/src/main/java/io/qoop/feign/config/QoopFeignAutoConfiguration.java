package io.qoop.feign.config;

import feign.codec.ErrorDecoder;
import io.qoop.feign.QoopErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QoopFeignAutoConfiguration {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new QoopErrorDecoder();
    }
}