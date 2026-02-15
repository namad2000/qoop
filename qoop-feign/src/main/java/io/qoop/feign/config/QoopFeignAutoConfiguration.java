package io.qoop.feign.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import io.qoop.feign.CorrelationInterceptor;
import io.qoop.feign.QoopErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QoopFeignAutoConfiguration {

    @Bean
    public RequestInterceptor correlationInterceptor() {
        return new CorrelationInterceptor();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new QoopErrorDecoder();
    }
}