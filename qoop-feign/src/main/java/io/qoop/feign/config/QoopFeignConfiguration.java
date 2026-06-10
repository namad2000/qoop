package io.qoop.feign.config;

import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import io.qoop.feign.QoopErrorDecoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QoopFeignConfiguration {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new QoopErrorDecoder();
    }

    @Bean
    public Encoder feignEncoder(ObjectProvider<FeignHttpMessageConverters> converters) {
        return new SpringEncoder(converters);
    }
}