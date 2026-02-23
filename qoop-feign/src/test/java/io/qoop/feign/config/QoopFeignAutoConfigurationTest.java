package io.qoop.feign.config;

import feign.codec.ErrorDecoder;
import io.qoop.feign.QoopErrorDecoder;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class QoopFeignAutoConfigurationTest {

    @Test
    void should_register_beans_correctly() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(QoopFeignAutoConfiguration.class);

        ErrorDecoder errorDecoder = context.getBean(ErrorDecoder.class);

        assertThat(errorDecoder).isNotNull();
        assertThat(errorDecoder).isInstanceOf(QoopErrorDecoder.class);

        context.close();
    }
}
